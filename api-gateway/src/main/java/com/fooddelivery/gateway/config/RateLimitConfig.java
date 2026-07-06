package com.fooddelivery.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.ReactiveRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

/**
 * Rate Limiting configuration for the API Gateway.
 *
 * <p>Three rate-limiting tiers are defined:
 * <ul>
 *   <li><b>Anonymous (IP-based)</b>: 10 req/s replenish, 20 burst — for unauthenticated traffic</li>
 *   <li><b>Authenticated (user-based)</b>: 100 req/s replenish, 200 burst — standard users</li>
 *   <li><b>Premium (user-based)</b>: 1000 req/s replenish, 2000 burst — premium/partner users</li>
 * </ul>
 *
 * <p>Redis Token Bucket algorithm is used. Each Redis key tracks the token bucket state
 * for the resolved key (IP or user ID). This is fully reactive and compatible with
 * Spring Cloud Gateway's RequestRateLimiter filter.
 *
 * <p>Key Resolvers:
 * <ul>
 *   <li>{@code ipKeyResolver} — extracts client IP from {@code X-Forwarded-For} or remote address</li>
 *   <li>{@code userKeyResolver} — extracts authenticated user subject (UUID) from JWT principal</li>
 *   <li>{@code compositeKeyResolver} — user key when authenticated, falls back to IP</li>
 * </ul>
 */
@Configuration
@Slf4j
public class RateLimitConfig {

    // ─── Tier properties from application.yml ────────────────────────────────

    @Value("${gateway.rate-limit.anonymous.replenish-rate:10}")
    private int anonymousReplenishRate;

    @Value("${gateway.rate-limit.anonymous.burst-capacity:20}")
    private int anonymousBurstCapacity;

    @Value("${gateway.rate-limit.authenticated.replenish-rate:100}")
    private int authenticatedReplenishRate;

    @Value("${gateway.rate-limit.authenticated.burst-capacity:200}")
    private int authenticatedBurstCapacity;

    @Value("${gateway.rate-limit.premium.replenish-rate:1000}")
    private int premiumReplenishRate;

    @Value("${gateway.rate-limit.premium.burst-capacity:2000}")
    private int premiumBurstCapacity;

    // ─── KeyResolver Beans ────────────────────────────────────────────────────

    /**
     * IP-based key resolver for anonymous traffic.
     *
     * <p>Respects {@code X-Forwarded-For} header to correctly identify the real client IP
     * when the gateway sits behind a load balancer or ingress controller.
     * Takes the leftmost (originating client) IP from the header chain.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String forwardedFor = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");

            if (forwardedFor != null && !forwardedFor.isBlank()) {
                // X-Forwarded-For may contain: "client, proxy1, proxy2"
                String clientIp = forwardedFor.split(",")[0].trim();
                log.debug("Rate limiting by IP (X-Forwarded-For): {}", clientIp);
                return Mono.just("ip:" + clientIp);
            }

            String remoteAddress = exchange.getRequest()
                    .getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";

            log.debug("Rate limiting by remote address: {}", remoteAddress);
            return Mono.just("ip:" + remoteAddress);
        };
    }

    /**
     * User-based key resolver for authenticated requests.
     *
     * <p>Extracts the authenticated user's subject claim (UUID) from the JWT principal.
     * Falls back to IP-based resolution if authentication is not available.
     * This ensures per-user rate limiting across multiple client devices.
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .map(userId -> {
                    log.debug("Rate limiting by user ID: {}", userId);
                    return "user:" + userId;
                })
                .switchIfEmpty(
                        // Fall back to IP-based limiting for anonymous requests
                        Mono.fromCallable(() -> {
                            String forwardedFor = exchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Forwarded-For");
                            if (forwardedFor != null && !forwardedFor.isBlank()) {
                                return "ip:" + forwardedFor.split(",")[0].trim();
                            }
                            return "ip:" + (exchange.getRequest().getRemoteAddress() != null
                                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                    : "unknown");
                        })
                );
    }

    /**
     * Path-based key resolver for per-endpoint rate limiting.
     *
     * <p>Combines user identity with the API path segment to allow
     * tighter limits on specific expensive endpoints (e.g., /search, /recommendations).
     */
    @Bean
    public KeyResolver pathUserKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            // Extract first 3 path segments to group by service area
            String pathKey = extractPathKey(path);

            return exchange.getPrincipal()
                    .map(principal -> "user-path:" + principal.getName() + ":" + pathKey)
                    .switchIfEmpty(Mono.just("ip-path:anonymous:" + pathKey));
        };
    }

    // ─── RedisRateLimiter Beans ───────────────────────────────────────────────

    /**
     * Default RedisRateLimiter for standard authenticated users.
     * 100 requests/second replenish rate, 200 burst capacity.
     * Tokens requested per exchange: 1.
     */
    @Bean(name = "defaultRedisRateLimiter")
    @Primary
    public RedisRateLimiter defaultRedisRateLimiter() {
        RedisRateLimiter rateLimiter = new RedisRateLimiter(
                authenticatedReplenishRate,
                authenticatedBurstCapacity,
                1
        );
        rateLimiter.setIncludeHeaders(true);
        return rateLimiter;
    }

    /**
     * RedisRateLimiter for anonymous / unauthenticated traffic.
     * 10 requests/second replenish rate, 20 burst capacity.
     */
    @Bean(name = "anonymousRedisRateLimiter")
    public RedisRateLimiter anonymousRedisRateLimiter() {
        RedisRateLimiter rateLimiter = new RedisRateLimiter(
                anonymousReplenishRate,
                anonymousBurstCapacity,
                1
        );
        rateLimiter.setIncludeHeaders(true);
        return rateLimiter;
    }

    /**
     * RedisRateLimiter for premium / partner users.
     * 1000 requests/second replenish rate, 2000 burst capacity.
     */
    @Bean(name = "premiumRedisRateLimiter")
    public RedisRateLimiter premiumRedisRateLimiter() {
        RedisRateLimiter rateLimiter = new RedisRateLimiter(
                premiumReplenishRate,
                premiumBurstCapacity,
                1
        );
        rateLimiter.setIncludeHeaders(true);
        return rateLimiter;
    }

    /**
     * Strict RedisRateLimiter for authentication endpoints (login, register).
     * 20 requests/second replenish rate, 40 burst capacity — prevents credential stuffing.
     */
    @Bean(name = "authEndpointRedisRateLimiter")
    public RedisRateLimiter authEndpointRedisRateLimiter() {
        RedisRateLimiter rateLimiter = new RedisRateLimiter(20, 40, 1);
        rateLimiter.setIncludeHeaders(true);
        return rateLimiter;
    }

    /**
     * Very strict RedisRateLimiter for payment endpoints.
     * 10 requests/second replenish rate, 20 burst capacity.
     */
    @Bean(name = "paymentEndpointRedisRateLimiter")
    public RedisRateLimiter paymentEndpointRedisRateLimiter() {
        RedisRateLimiter rateLimiter = new RedisRateLimiter(10, 20, 1);
        rateLimiter.setIncludeHeaders(true);
        return rateLimiter;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Extracts a normalized path key from the request path for per-path rate limiting.
     * Strips UUIDs and numeric IDs for grouping purposes.
     */
    private String extractPathKey(String path) {
        if (path == null || path.isBlank()) {
            return "root";
        }
        // Remove trailing slash, then take first 3 segments
        String[] segments = path.replaceAll("/$", "").split("/");
        StringBuilder key = new StringBuilder();
        int count = 0;
        for (String segment : segments) {
            if (segment.isBlank()) continue;
            // Skip UUIDs (36 chars with dashes) and numeric IDs
            if (segment.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                key.append("/:id");
            } else if (segment.matches("\\d+")) {
                key.append("/:n");
            } else {
                key.append("/").append(segment);
            }
            if (++count >= 3) break;
        }
        return key.toString();
    }
}
