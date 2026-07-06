package com.fooddelivery.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Authentication Global Filter.
 *
 * <p>Executed at the highest priority (ORDER = -100) so it runs before route filters.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Extracts JWT from the {@code Authorization: Bearer <token>} header</li>
 *   <li>Decodes and validates the token using the injected {@link ReactiveJwtDecoder}
 *       (backed by JWK Set URI from the auth service)</li>
 *   <li>On success: enriches the downstream request with security context headers:
 *       <ul>
 *         <li>{@code X-User-Id} — Subject (UUID) from JWT {@code sub} claim</li>
 *         <li>{@code X-User-Email} — Email from JWT {@code email} claim</li>
 *         <li>{@code X-User-Roles} — Comma-separated roles from JWT {@code roles} claim</li>
 *         <li>{@code X-User-Tier} — User tier from JWT {@code tier} claim (STANDARD/PREMIUM)</li>
 *       </ul>
 *   </li>
 *   <li>On failure: returns structured JSON 401 response without forwarding to downstream</li>
 *   <li>Public paths: skipped entirely (upstream Spring Security handles permit-all)</li>
 * </ol>
 *
 * <p>This filter complements Spring Security's OAuth2 resource server configuration.
 * Spring Security performs the authorization enforcement; this filter extracts and
 * propagates the user context to downstream microservices as trusted headers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final int ORDER = -100;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_TIER = "X-User-Tier";
    private static final String HEADER_USER_NAME = "X-User-Name";

    /**
     * Public endpoints that do not require JWT validation.
     * These must match the SecurityConfig public paths whitelist.
     */
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/v1/users/register",
            "/api/v1/users/login",
            "/api/v1/users/refresh-token",
            "/api/v1/users/forgot-password",
            "/api/v1/users/reset-password",
            "/api/v1/auth/",
            "/actuator/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/webjars/",
            "/fallback/",
            "/favicon.ico",
            "/ws/"
    );

    private final ReactiveJwtDecoder reactiveJwtDecoder;
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            log.debug("Skipping JWT validation for public path: {}", path);
            return chain.filter(exchange);
        }

        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // No Authorization header — Spring Security will enforce access control downstream
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            log.debug("No Authorization header present for path: {}", path);
            return chain.filter(exchange);
        }

        // Not a Bearer token — ignore (could be other scheme)
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization header is not a Bearer token for path: {}", path);
            return chain.filter(exchange);
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        return reactiveJwtDecoder.decode(token)
                .flatMap(jwt -> {
                    // Token is valid — enrich downstream request with security context headers
                    ServerHttpRequest enrichedRequest = buildEnrichedRequest(request, jwt);
                    return chain.filter(exchange.mutate().request(enrichedRequest).build());
                })
                .onErrorResume(JwtValidationException.class, ex -> {
                    log.warn("JWT validation failed for path [{}]: {}", path, ex.getMessage());
                    return handleJwtError(exchange, ex.getMessage(), "token_expired_or_invalid");
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected error during JWT processing for path [{}]: {}",
                            path, ex.getMessage(), ex);
                    return handleJwtError(exchange, "Token processing failed", "token_processing_error");
                });
    }

    /**
     * Builds a mutated request with downstream security context headers appended.
     * These headers allow downstream microservices to trust the user identity
     * without needing their own JWT validation logic.
     */
    private ServerHttpRequest buildEnrichedRequest(ServerHttpRequest request, Jwt jwt) {
        ServerHttpRequest.Builder builder = request.mutate();

        // Subject (user UUID)
        String userId = jwt.getSubject();
        if (userId != null && !userId.isBlank()) {
            builder.header(HEADER_USER_ID, userId);
        }

        // Email claim
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            builder.header(HEADER_USER_EMAIL, email);
        }

        // Roles claim — stored as List<String> in JWT
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && !roles.isEmpty()) {
            builder.header(HEADER_USER_ROLES, String.join(",", roles));
        }

        // User tier (STANDARD, PREMIUM, PARTNER) — drives rate limit selection
        String tier = jwt.getClaimAsString("tier");
        if (tier != null && !tier.isBlank()) {
            builder.header(HEADER_USER_TIER, tier);
        }

        // Full name for personalization
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            builder.header(HEADER_USER_NAME, name);
        }

        // Expiry for downstream services that want to know token freshness
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt != null) {
            builder.header("X-Token-Expires-At", expiresAt.toString());
        }

        // Strip the Authorization header before forwarding to prevent token leakage
        // in internal service-to-service calls that use service accounts instead
        // NOTE: We keep it for services that need raw token (e.g., notification service push auth)
        // builder.headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION));

        return builder.build();
    }

    /**
     * Writes a structured JSON 401 response and terminates the request.
     */
    private Mono<Void> handleJwtError(ServerWebExchange exchange, String message, String errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("WWW-Authenticate",
                "Bearer realm=\"food-delivery\", error=\"" + errorCode + "\", error_description=\"" + message + "\"");

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("status", 401);
        errorBody.put("error", "Unauthorized");
        errorBody.put("error_code", errorCode);
        errorBody.put("message", message);
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("path", exchange.getRequest().getPath().value());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JWT error response", e);
            byte[] fallback = "{\"status\":401,\"error\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * Checks whether the request path is in the public endpoint whitelist.
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith)
                || path.equals("/")
                || path.equals("/favicon.ico");
    }
}
