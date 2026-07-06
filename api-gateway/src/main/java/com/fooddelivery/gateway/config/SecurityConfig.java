package com.fooddelivery.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Production-grade reactive security configuration for the API Gateway.
 *
 * <p>Security Strategy:
 * <ul>
 *   <li>Stateless JWT authentication via Spring Security OAuth2 Resource Server</li>
 *   <li>RS256 token validation against JWK Set URI from the auth service</li>
 *   <li>Role-based authorization for admin and operator routes</li>
 *   <li>Public endpoint whitelist for registration, login, and health checks</li>
 *   <li>Reactive CORS configuration aligned with gateway global CORS settings</li>
 *   <li>CSRF disabled (REST API; CORS + JWT is the protection boundary)</li>
 * </ul>
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://auth.fooddelivery.com}")
    private String issuerUri;

    /**
     * Ordered list of public paths that do NOT require authentication.
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/users/register",
            "/api/v1/users/login",
            "/api/v1/users/refresh-token",
            "/api/v1/users/forgot-password",
            "/api/v1/users/reset-password",
            "/api/v1/auth/**",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**",
            "/favicon.ico",
            "/fallback/**",
            "/ws/**"
    };

    /**
     * Admin-only paths requiring ROLE_ADMIN or ROLE_SUPER_ADMIN.
     */
    private static final String[] ADMIN_PATHS = {
            "/api/v1/admin/**",
            "/api/v1/audit/**",
            "/api/v1/fraud/**"
    };

    /**
     * Operator paths requiring ROLE_OPERATOR or ROLE_ADMIN.
     */
    private static final String[] OPERATOR_PATHS = {
            "/api/v1/reports/**",
            "/api/v1/analytics/**"
    };

    @Bean
    @Order(1)
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // ── CSRF: disabled for stateless API (JWT + CORS protect adequately) ──
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // ── CORS: delegate to CorsConfigurationSource bean ─────────────────
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))

                // ── Security Context: stateless - no session ───────────────────────
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // ── Authorization rules ────────────────────────────────────────────
                .authorizeExchange(exchanges -> exchanges
                        // Permit pre-flight OPTIONS globally
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints - no authentication required
                        .pathMatchers(PUBLIC_PATHS).permitAll()

                        // Admin-only endpoints
                        .pathMatchers(ADMIN_PATHS)
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Operator-level endpoints
                        .pathMatchers(OPERATOR_PATHS)
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "OPERATOR")

                        // Driver-specific endpoints
                        .pathMatchers("/api/v1/drivers/me/**", "/api/v1/deliveries/driver/**")
                        .hasAnyRole("DRIVER", "ADMIN")

                        // All remaining endpoints require authentication
                        .anyExchange().authenticated()
                )

                // ── OAuth2 Resource Server (JWT RS256) ─────────────────────────────
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(reactiveJwtDecoder())
                                .jwtAuthenticationConverter(reactiveJwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // ── Exception Handling ─────────────────────────────────────────────
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .build();
    }

    /**
     * Reactive JWT decoder backed by the JWKS endpoint of the user/auth service.
     * The NimbusReactiveJwtDecoder caches the JWK set and rotates keys automatically.
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();
    }

    /**
     * Converts JWT claims to Spring Security GrantedAuthorities.
     * <p>
     * Expected JWT structure:
     * <pre>
     * {
     *   "sub": "user-uuid",
     *   "email": "user@example.com",
     *   "roles": ["ROLE_USER", "ROLE_PREMIUM"],
     *   "user_id": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * </pre>
     */
    @Bean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Our JWT stores roles in "roles" claim as array of strings (e.g., "ROLE_USER")
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        // No prefix added - roles already contain ROLE_ prefix in our JWT
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(grantedAuthoritiesConverter)
        );

        return jwtAuthenticationConverter;
    }

    /**
     * CORS configuration matching the global gateway CORS settings in application.yml.
     * This bean is required for Spring Security's CORS integration at the filter chain level.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://*.fooddelivery.com",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setExposedHeaders(Arrays.asList(
                "X-Correlation-ID",
                "X-Request-ID",
                "X-RateLimit-Remaining",
                "X-RateLimit-Burst-Capacity",
                "Authorization",
                "Content-Disposition"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Custom 401 Unauthorized response handler.
     * Returns structured JSON error body instead of Spring's default HTML redirect.
     */
    private ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {
            log.warn("Authentication failed for request [{}]: {}",
                    exchange.getRequest().getPath(), ex.getMessage());

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            exchange.getResponse().getHeaders().add("WWW-Authenticate",
                    "Bearer realm=\"food-delivery\", error=\"invalid_token\"");

            String body = """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Authentication required. Please provide a valid Bearer token.",
                      "path": "%s"
                    }
                    """.formatted(exchange.getRequest().getPath().value());

            var bufferFactory = exchange.getResponse().bufferFactory();
            var buffer = bufferFactory.wrap(body.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        };
    }

    /**
     * Custom 403 Forbidden response handler.
     * Returns structured JSON error body for insufficient permissions.
     */
    private ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> {
            log.warn("Access denied for request [{}]: {}",
                    exchange.getRequest().getPath(), ex.getMessage());

            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

            String body = """
                    {
                      "status": 403,
                      "error": "Forbidden",
                      "message": "You do not have permission to access this resource.",
                      "path": "%s"
                    }
                    """.formatted(exchange.getRequest().getPath().value());

            var bufferFactory = exchange.getResponse().bufferFactory();
            var buffer = bufferFactory.wrap(body.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        };
    }
}
