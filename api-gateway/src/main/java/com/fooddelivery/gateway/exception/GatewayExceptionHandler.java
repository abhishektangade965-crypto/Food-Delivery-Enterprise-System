package com.fooddelivery.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Global Exception Handler for the API Gateway.
 *
 * <p>Ordered at -1 to take precedence over Spring Boot's DefaultErrorWebExceptionHandler
 * (which is at {@code Integer.MAX_VALUE}).
 *
 * <p>Catches all unhandled exceptions from the gateway filter chain and reactive pipeline,
 * including:
 * <ul>
 *   <li>Service connectivity errors ({@link ConnectException}) → 503 Service Unavailable</li>
 *   <li>Circuit breaker open / fallback failures → 503 Service Unavailable</li>
 *   <li>Request timeouts ({@link TimeoutException}) → 504 Gateway Timeout</li>
 *   <li>Route not found ({@link NotFoundException}) → 404 Not Found</li>
 *   <li>Security authentication errors → 401 Unauthorized</li>
 *   <li>Security authorization errors ({@link AccessDeniedException}) → 403 Forbidden</li>
 *   <li>Generic {@link ResponseStatusException} → preserves the status code</li>
 *   <li>All other exceptions → 500 Internal Server Error</li>
 * </ul>
 *
 * <p>All error responses use the following JSON structure:
 * <pre>
 * {
 *   "status": 503,
 *   "error": "Service Unavailable",
 *   "message": "The upstream service is currently unavailable. Please retry.",
 *   "error_code": "SERVICE_UNAVAILABLE",
 *   "correlationId": "550e8400-e29b-41d4-a716-446655440000",
 *   "path": "/api/v1/orders",
 *   "timestamp": "2026-06-25T09:00:00.000Z"
 * }
 * </pre>
 */
@Component
@Order(-1)
@RequiredArgsConstructor
@Slf4j
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getPath().value();
        String correlationId = exchange.getRequest().getHeaders()
                .getFirst("X-Correlation-ID");

        // Determine HTTP status and user-facing error details
        ErrorDetail errorDetail = resolveErrorDetail(ex, path);

        // Log appropriately based on severity
        logException(ex, errorDetail, path, correlationId);

        response.setStatusCode(errorDetail.httpStatus());
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // Echo correlation ID in error response for client-side debugging
        if (correlationId != null) {
            response.getHeaders().add("X-Correlation-ID", correlationId);
        }

        Map<String, Object> errorBody = buildErrorBody(errorDetail, path, correlationId);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException jsonEx) {
            log.error("Failed to serialize error response body", jsonEx);
            byte[] fallback = buildFallbackResponse(errorDetail.httpStatus().value());
            DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * Maps exceptions to appropriate HTTP status codes and user-facing messages.
     * Internal exception details are NOT exposed in error messages for security.
     */
    private ErrorDetail resolveErrorDetail(Throwable ex, String path) {
        return switch (ex) {
            // ── Service connectivity / routing ───────────────────────────────
            case NotFoundException notFound -> new ErrorDetail(
                    HttpStatus.NOT_FOUND,
                    "The requested resource or route was not found.",
                    "ROUTE_NOT_FOUND"
            );
            case ConnectException connect -> new ErrorDetail(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The upstream service is currently unavailable. Please retry later.",
                    "SERVICE_UNAVAILABLE"
            );

            // ── Timeouts ─────────────────────────────────────────────────────
            case TimeoutException timeout -> new ErrorDetail(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "The upstream service did not respond in time. Please retry.",
                    "GATEWAY_TIMEOUT"
            );

            // ── Security ─────────────────────────────────────────────────────
            case AccessDeniedException accessDenied -> new ErrorDetail(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this resource.",
                    "ACCESS_DENIED"
            );
            case AuthenticationCredentialsNotFoundException authNotFound -> new ErrorDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required. Please provide a valid Bearer token.",
                    "AUTHENTICATION_REQUIRED"
            );
            case OAuth2AuthenticationException oauth2Ex -> new ErrorDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired authentication token.",
                    "INVALID_TOKEN"
            );

            // ── ResponseStatusException (Spring WebFlux standard) ────────────
            case ResponseStatusException rse -> new ErrorDetail(
                    HttpStatus.valueOf(rse.getStatusCode().value()),
                    rse.getReason() != null ? rse.getReason() : rse.getMessage(),
                    "HTTP_" + rse.getStatusCode().value()
            );

            // ── Circuit breaker fallback (io.github.resilience4j) ─────────────
            default -> {
                // Check for circuit breaker open exception by class name
                // (avoids direct resilience4j dependency in exception handler)
                String exClassName = ex.getClass().getSimpleName();
                if (exClassName.contains("CallNotPermitted") ||
                    exClassName.contains("CircuitBreaker")) {
                    yield new ErrorDetail(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "The service is temporarily unavailable due to high error rate. Please retry later.",
                            "CIRCUIT_BREAKER_OPEN"
                    );
                }
                // Generic server error — do NOT expose internal details
                yield new ErrorDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Our team has been notified.",
                        "INTERNAL_SERVER_ERROR"
                );
            }
        };
    }

    /**
     * Builds the structured error response body as a {@link Map} ready for JSON serialization.
     */
    private Map<String, Object> buildErrorBody(ErrorDetail errorDetail,
                                               String path,
                                               String correlationId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", errorDetail.httpStatus().value());
        body.put("error", errorDetail.httpStatus().getReasonPhrase());
        body.put("message", errorDetail.message());
        body.put("error_code", errorDetail.errorCode());
        body.put("path", path);
        if (correlationId != null) {
            body.put("correlationId", correlationId);
        }
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    /**
     * Logs the exception at the appropriate level based on HTTP status.
     */
    private void logException(Throwable ex, ErrorDetail errorDetail, String path, String correlationId) {
        int statusValue = errorDetail.httpStatus().value();
        String logMessage = "Gateway error [path={}, status={}, errorCode={}, correlationId={}]";

        if (statusValue >= 500) {
            log.error(logMessage, path, statusValue, errorDetail.errorCode(), correlationId, ex);
        } else if (statusValue >= 400) {
            log.warn(logMessage, path, statusValue, errorDetail.errorCode(), correlationId);
        } else {
            log.info(logMessage, path, statusValue, errorDetail.errorCode(), correlationId);
        }
    }

    /**
     * Minimal fallback response if Jackson serialization fails.
     */
    private byte[] buildFallbackResponse(int statusCode) {
        String json = String.format(
                "{\"status\":%d,\"error\":\"Gateway Error\",\"timestamp\":\"%s\"}",
                statusCode, Instant.now()
        );
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Internal record holding the resolved error details for a given exception.
     */
    private record ErrorDetail(
            HttpStatus httpStatus,
            String message,
            String errorCode
    ) {}
}
