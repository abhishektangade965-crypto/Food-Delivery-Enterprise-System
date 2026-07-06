package com.fooddelivery.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ResponseTransformationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            
            // Remove sensitive headers exposed by downstream servers
            response.getHeaders().remove("Server");
            response.getHeaders().remove("X-Powered-By");
            
            // Inject tracing and transaction identifiers to user response
            String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
            if (correlationId != null) {
                response.getHeaders().set("X-Correlation-ID", correlationId);
            }
            
            String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
            if (requestId != null) {
                response.getHeaders().set("X-Request-ID", requestId);
            }
            
            // Enforce response secure standards
            response.getHeaders().set("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            response.getHeaders().set("X-Content-Type-Options", "nosniff");
            response.getHeaders().set("X-Frame-Options", "DENY");
            response.getHeaders().set("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data: https:;");

            log.info("Transformed response headers for path: {} with status: {}", 
                    exchange.getRequest().getPath().value(), response.getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        // Run late in the filter chain, to process the response
        return Ordered.LOWEST_PRECEDENCE;
    }
}
