package com.fooddelivery.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * API Gateway Application - Entry Point for the Food Delivery Super Platform.
 *
 * <p>This reactive Spring Cloud Gateway acts as the single entry point for all client traffic.
 * It handles:
 * <ul>
 *   <li>JWT RS256 token validation via Spring Security OAuth2 Resource Server</li>
 *   <li>Redis-backed rate limiting (RequestRateLimiter filter) per user tier</li>
 *   <li>Resilience4j Circuit Breakers per downstream service</li>
 *   <li>Global CORS configuration</li>
 *   <li>Correlation ID propagation for distributed tracing (OpenTelemetry)</li>
 *   <li>Structured JSON request/response logging</li>
 *   <li>Role-based route authorization (admin routes)</li>
 * </ul>
 *
 * <p>Built on Project Reactor / Netty for fully non-blocking, reactive I/O.
 * Virtual threads (Java 21) are enabled for blocking operations via the scheduler.
 */
@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan("com.fooddelivery.gateway")
@EnableScheduling
public class ApiGatewayApplication {

    public static void main(String[] args) {
        // Enable virtual threads for Netty's blocking operations
        System.setProperty("reactor.netty.ioWorkerCount",
                String.valueOf(Runtime.getRuntime().availableProcessors() * 2));
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
