package com.fooddelivery.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WafFilter implements GlobalFilter, Ordered {

    // Blocked IP ranges
    private static final List<String> BLACKLISTED_IPS = List.of("198.51.100.42", "203.0.113.88");
    
    // Blocked Countries (Simulated via GeoIP header like Cloudflare's CF-IPCountry)
    private static final List<String> BLOCKED_COUNTRIES = List.of("KP", "SY");

    // Bot User-Agents
    private static final List<String> BOT_USER_AGENTS = List.of("nmap", "nikto", "sqlmap", "headless", "selenium", "puppeteer");

    // SQL Injection & XSS Patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union\\s+select|select\\s+.*\\s+from|insert\\s+into|update\\s+.*\\s+set|delete\\s+from|drop\\s+table|or\\s+\\d+=\\d+)"
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script.*?>|javascript:|onload|onerror|alert\\()"
    );

    private static final String SIGNING_SECRET = "delivo-super-platform-signing-secret-123456";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String country = request.getHeaders().getFirst("CF-IPCountry");

        log.info("WAF checking request from IP: {}, Country: {}, Path: {}, User-Agent: {}", 
                clientIp, country, path, userAgent);

        // 1. IP Blacklist check
        if (BLACKLISTED_IPS.contains(clientIp)) {
            log.warn("Blocked request from blacklisted IP: {}", clientIp);
            return handleForbidden(exchange, "Blocked by IP restriction policy.");
        }

        // 2. Geo-blocking check
        if (country != null && BLOCKED_COUNTRIES.contains(country.toUpperCase())) {
            log.warn("Blocked request from country: {}", country);
            return handleForbidden(exchange, "Access denied in your region.");
        }

        // 3. Bot Detection
        if (userAgent != null) {
            String uaLower = userAgent.toLowerCase();
            for (String botPattern : BOT_USER_AGENTS) {
                if (uaLower.contains(botPattern)) {
                    log.warn("Blocked bot request. User-Agent: {}", userAgent);
                    return handleForbidden(exchange, "Bots are not permitted.");
                }
            }
        }

        // 4. SQL Injection and XSS Detection on Query Params
        String query = request.getURI().getQuery();
        if (query != null) {
            if (SQL_INJECTION_PATTERN.matcher(query).find()) {
                log.warn("SQL Injection pattern detected in query: {}", query);
                return handleBadRequest(exchange, "Malicious payload detected.");
            }
            if (XSS_PATTERN.matcher(query).find()) {
                log.warn("XSS pattern detected in query: {}", query);
                return handleBadRequest(exchange, "Malicious payload detected.");
            }
        }

        // 5. Request Signing Validation for payment or critical endpoints
        if (path.startsWith("/api/v1/payments") || path.startsWith("/api/v1/orders")) {
            String signature = request.getHeaders().getFirst("X-Signature");
            String timestamp = request.getHeaders().getFirst("X-Timestamp");
            
            if (signature == null || timestamp == null) {
                log.warn("Missing signature headers on path: {}", path);
                return handleUnauthorized(exchange, "Request signature is missing.");
            }

            // Verify replay protection (timestamp within 5 minutes)
            try {
                long reqTime = Long.parseLong(timestamp);
                long currTime = System.currentTimeMillis();
                if (Math.abs(currTime - reqTime) > 300000) { // 5 minutes
                    log.warn("Request signature expired. Delay: {}ms", currTime - reqTime);
                    return handleUnauthorized(exchange, "Request signature expired (Replay protection).");
                }

                String payloadToSign = path + "|" + timestamp;
                String calculatedSignature = hmacSha256(payloadToSign, SIGNING_SECRET);
                
                if (!calculatedSignature.equals(signature)) {
                    log.warn("Invalid signature. Expected: {}, Got: {}", calculatedSignature, signature);
                    return handleUnauthorized(exchange, "Request signature verification failed.");
                }
            } catch (Exception e) {
                log.error("Failed to validate request signature", e);
                return handleUnauthorized(exchange, "Signature parsing error.");
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run early in the filter chain, before authentication or routing
        return -100;
    }

    private String getClientIp(ServerHttpRequest request) {
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = String.format("{\"error\":\"Forbidden\",\"message\":\"%s\"}", message).getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private Mono<Void> handleBadRequest(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = String.format("{\"error\":\"Bad Request\",\"message\":\"%s\"}", message).getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message).getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private String hmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }
}
