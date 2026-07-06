package com.fooddelivery.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Request Logging Global Filter.
 *
 * <p>Runs at order -150 (after correlation ID injection at -200, before JWT at -100).
 * This ordering ensures every log line carries the correlation ID set by
 * {@link CorrelationIdFilter}.
 *
 * <p>Emits two structured JSON log events per request:
 * <ol>
 *   <li><b>REQUEST</b>: Logged at the start of request processing (inbound phase)</li>
 *   <li><b>RESPONSE</b>: Logged after the response is written (outbound phase)</li>
 * </ol>
 *
 * <p>The JSON log format (compatible with Loki/Grafana label-value querying):
 * <pre>
 * {
 *   "event": "GATEWAY_REQUEST",
 *   "correlationId": "550e8400-e29b-41d4-a716-446655440000",
 *   "requestId": "660e8400-e29b-41d4-a716-446655440001",
 *   "method": "POST",
 *   "path": "/api/v1/orders",
 *   "userAgent": "FoodDeliveryApp/1.0 iOS/17.0",
 *   "clientIp": "203.0.113.1",
 *   "userId": "user-uuid",
 *   "timestamp": "2026-06-25T09:00:00.000Z"
 * }
 *
 * {
 *   "event": "GATEWAY_RESPONSE",
 *   "correlationId": "550e8400-e29b-41d4-a716-446655440000",
 *   "requestId": "660e8400-e29b-41d4-a716-446655440001",
 *   "method": "POST",
 *   "path": "/api/v1/orders",
 *   "status": 201,
 *   "durationMs": 124,
 *   "userId": "user-uuid",
 *   "timestamp": "2026-06-25T09:00:00.124Z"
 * }
 * </pre>
 *
 * <p>Security note: Request bodies are NOT logged to prevent PII / PAN data exposure.
 * Only safe metadata (method, path, status, duration) is emitted.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final int ORDER = -150;
    private static final String START_TIME_ATTR = "requestStartTime";

    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Record start time for duration calculation
        long startTimeNanos = System.nanoTime();
        exchange.getAttributes().put(START_TIME_ATTR, startTimeNanos);

        // Extract context from headers (set by CorrelationIdFilter at -200)
        String correlationId = getHeader(request, CorrelationIdFilter.CORRELATION_ID_HEADER);
        String requestId = getHeader(request, CorrelationIdFilter.REQUEST_ID_HEADER);
        String userId = getHeader(request, "X-User-Id");
        String clientIp = resolveClientIp(request);

        // ── Log INBOUND request ───────────────────────────────────────────────
        logRequest(request, correlationId, requestId, userId, clientIp);

        // ── Chain then log OUTBOUND response ─────────────────────────────────
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long durationMs = (System.nanoTime() - startTimeNanos) / 1_000_000L;
                    ServerHttpResponse response = exchange.getResponse();
                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value()
                            : 0;

                    logResponse(request, response, correlationId, requestId, userId,
                            statusCode, durationMs);
                });
    }

    /**
     * Emits a structured JSON log for the inbound request.
     */
    private void logRequest(ServerHttpRequest request,
                            String correlationId,
                            String requestId,
                            String userId,
                            String clientIp) {
        try {
            Map<String, Object> logEntry = new LinkedHashMap<>();
            logEntry.put("event", "GATEWAY_REQUEST");
            logEntry.put("correlationId", correlationId);
            logEntry.put("requestId", requestId);
            logEntry.put("method", request.getMethod().name());
            logEntry.put("path", request.getPath().value());
            logEntry.put("query", sanitizeQuery(request.getURI().getQuery()));
            logEntry.put("clientIp", clientIp);
            logEntry.put("userId", userId != null ? userId : "anonymous");
            logEntry.put("userAgent", getHeader(request, "User-Agent"));
            logEntry.put("contentType", getHeader(request, "Content-Type"));
            logEntry.put("timestamp", Instant.now().toString());

            log.info(objectMapper.writeValueAsString(logEntry));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request log entry: {}", e.getMessage());
        }
    }

    /**
     * Emits a structured JSON log for the outbound response.
     */
    private void logResponse(ServerHttpRequest request,
                             ServerHttpResponse response,
                             String correlationId,
                             String requestId,
                             String userId,
                             int statusCode,
                             long durationMs) {
        try {
            Map<String, Object> logEntry = new LinkedHashMap<>();
            logEntry.put("event", "GATEWAY_RESPONSE");
            logEntry.put("correlationId", correlationId);
            logEntry.put("requestId", requestId);
            logEntry.put("method", request.getMethod().name());
            logEntry.put("path", request.getPath().value());
            logEntry.put("status", statusCode);
            logEntry.put("durationMs", durationMs);
            logEntry.put("userId", userId != null ? userId : "anonymous");
            logEntry.put("contentType", getResponseHeader(response, "Content-Type"));
            logEntry.put("contentLength", getResponseHeader(response, "Content-Length"));
            logEntry.put("timestamp", Instant.now().toString());
            logEntry.put("slow", durationMs > 5000);  // Flag slow responses > 5s

            // Log level based on status code range
            if (statusCode >= 500) {
                log.error(objectMapper.writeValueAsString(logEntry));
            } else if (statusCode >= 400) {
                log.warn(objectMapper.writeValueAsString(logEntry));
            } else {
                log.info(objectMapper.writeValueAsString(logEntry));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize response log entry: {}", e.getMessage());
        }
    }

    /**
     * Resolves the real client IP, honoring X-Forwarded-For from trusted proxies.
     * Returns the leftmost (originating) IP to handle proxy chains correctly.
     */
    private String resolveClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    /**
     * Sanitizes query string by removing sensitive parameters before logging.
     * Redacts values for parameters that may carry credentials or tokens.
     */
    private String sanitizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        // Redact sensitive query parameters
        return query.replaceAll("(?i)(token|key|secret|password|credential)=[^&]*", "$1=[REDACTED]");
    }

    private String getHeader(ServerHttpRequest request, String headerName) {
        String value = request.getHeaders().getFirst(headerName);
        return (value != null && !value.isBlank()) ? value : null;
    }

    private String getResponseHeader(ServerHttpResponse response, String headerName) {
        String value = response.getHeaders().getFirst(headerName);
        return (value != null && !value.isBlank()) ? value : null;
    }
}
