package com.fooddelivery.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Correlation ID Global Filter.
 *
 * <p>Runs at order -200 (before the JWT filter at -100) so that every request,
 * including those that fail authentication, carries a correlation ID.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li><b>Inbound</b>: Reads {@code X-Correlation-ID} from the incoming request.
 *       If absent, generates a new UUID v4.</li>
 *   <li><b>Propagation</b>: Adds {@code X-Correlation-ID} and {@code X-Request-ID}
 *       to the mutated downstream request so all microservices receive the same ID.</li>
 *   <li><b>MDC</b>: Populates {@code correlationId} and {@code requestId} in the
 *       SLF4J MDC (Mapped Diagnostic Context) for structured log correlation.
 *       MDC is cleared after the response to avoid thread-local leaks.</li>
 *   <li><b>Response</b>: Echoes {@code X-Correlation-ID} back to the caller in the response
 *       so clients can trace their requests through the system.</li>
 *   <li><b>OpenTelemetry</b>: The {@code correlationId} in MDC is automatically picked up
 *       by the OTel log correlation bridge (via {@code otel.logs.exporter=otlp}).</li>
 * </ol>
 *
 * <p>Header contract:
 * <pre>
 * Request  →  X-Correlation-ID: [client provided or gateway generated UUID]
 *             X-Request-ID:      [always gateway-generated UUID, unique per request]
 * Response ←  X-Correlation-ID: [same as request, echoed back]
 * </pre>
 */
@Component
@Slf4j
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final int ORDER = -200;

    // Inbound header name that clients may supply to continue a trace
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    // Always-fresh unique ID per gateway request (not carried from clients)
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    // Gateway identifier header so downstream services know the request came through gateway
    public static final String GATEWAY_HEADER = "X-Gateway";
    private static final String GATEWAY_VALUE = "food-delivery-api-gateway";

    // MDC key names for structured logging integration
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ── 1. Determine or generate Correlation ID ───────────────────────────
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = generateCorrelationId();
            log.debug("Generated new Correlation ID: {}", correlationId);
        } else {
            // Validate format to prevent header injection
            if (!isValidCorrelationId(correlationId)) {
                correlationId = generateCorrelationId();
                log.warn("Invalid X-Correlation-ID received; replaced with: {}", correlationId);
            } else {
                log.debug("Using client-provided Correlation ID: {}", correlationId);
            }
        }

        // ── 2. Always generate a fresh Request ID for this hop ────────────────
        String requestId = generateCorrelationId();

        // Capture final values for lambdas (must be effectively final)
        final String finalCorrelationId = correlationId;
        final String finalRequestId = requestId;

        // ── 3. Populate MDC for this reactive pipeline ────────────────────────
        MDC.put(MDC_CORRELATION_ID, finalCorrelationId);
        MDC.put(MDC_REQUEST_ID, finalRequestId);

        // ── 4. Mutate the downstream request to carry correlation headers ─────
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(CORRELATION_ID_HEADER, finalCorrelationId)
                .header(REQUEST_ID_HEADER, finalRequestId)
                .header(GATEWAY_HEADER, GATEWAY_VALUE)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // ── 5. Add correlation headers to the response ────────────────────────
        mutatedExchange.getResponse()
                .getHeaders()
                .add(CORRELATION_ID_HEADER, finalCorrelationId);
        mutatedExchange.getResponse()
                .getHeaders()
                .add(REQUEST_ID_HEADER, finalRequestId);

        // ── 6. Continue the filter chain, clear MDC after completion ──────────
        return chain.filter(mutatedExchange)
                .doFinally(signalType -> {
                    // Clean up MDC to avoid pollution of reused threads
                    MDC.remove(MDC_CORRELATION_ID);
                    MDC.remove(MDC_REQUEST_ID);
                    log.debug("MDC cleared after request [correlationId={}]", finalCorrelationId);
                });
    }

    /**
     * Generates a new UUID v4 as a correlation ID.
     * Format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx (standard UUID)
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Validates that the provided correlation ID is a valid UUID format.
     * This prevents malicious actors from injecting large strings or special characters
     * into the MDC / downstream headers.
     */
    private boolean isValidCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.length() > 36) {
            return false;
        }
        try {
            UUID.fromString(correlationId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
