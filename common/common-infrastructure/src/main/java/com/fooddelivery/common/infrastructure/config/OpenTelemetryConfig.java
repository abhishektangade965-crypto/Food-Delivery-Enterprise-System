package com.fooddelivery.common.infrastructure.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * OpenTelemetry configuration for distributed tracing, metrics, and context propagation.
 * Exports to an OTLP-compatible backend (Jaeger/Tempo for traces, Prometheus/Mimir for metrics).
 */
@Slf4j
@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.service.name:food-delivery-service}")
    private String serviceName;

    @Value("${otel.service.version:1.0.0}")
    private String serviceVersion;

    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;

    @Value("${otel.traces.sampler.ratio:1.0}")
    private double samplerRatio;

    private SdkTracerProvider sdkTracerProvider;
    private SdkMeterProvider sdkMeterProvider;

    @Bean
    public Resource openTelemetryResource() {
        return Resource.getDefault().merge(
                Resource.create(Attributes.builder()
                        .put(ResourceAttributes.SERVICE_NAME, serviceName)
                        .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                        .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, System.getProperty("spring.profiles.active", "default"))
                        .build())
        );
    }

    @Bean
    public OtlpGrpcSpanExporter otlpGrpcSpanExporter() {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .setTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public OtlpGrpcMetricExporter otlpGrpcMetricExporter() {
        return OtlpGrpcMetricExporter.builder()
                .setEndpoint(otlpEndpoint)
                .setTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public SdkTracerProvider sdkTracerProvider(Resource resource, OtlpGrpcSpanExporter spanExporter) {
        this.sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                        .setScheduleDelay(Duration.ofMillis(100))
                        .setMaxExportBatchSize(512)
                        .setMaxQueueSize(2048)
                        .setExporterTimeout(Duration.ofSeconds(30))
                        .build())
                .setResource(resource)
                .setSampler(Sampler.parentBased(Sampler.traceIdRatioBased(samplerRatio)))
                .build();
        return this.sdkTracerProvider;
    }

    @Bean
    public SdkMeterProvider sdkMeterProvider(Resource resource, OtlpGrpcMetricExporter metricExporter) {
        this.sdkMeterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(
                        PeriodicMetricReader.builder(metricExporter)
                                .setInterval(Duration.ofSeconds(30))
                                .build())
                .build();
        return this.sdkMeterProvider;
    }

    @Bean
    public OpenTelemetry openTelemetry(SdkTracerProvider tracerProvider, SdkMeterProvider meterProvider) {
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .setPropagators(ContextPropagators.create(
                        TextMapPropagator.composite(
                                W3CTraceContextPropagator.getInstance(),
                                B3Propagator.injectingMultiHeaders()
                        )))
                .build();
        GlobalOpenTelemetry.resetForTest();
        try {
            GlobalOpenTelemetry.set(openTelemetrySdk);
        } catch (IllegalStateException e) {
            log.warn("GlobalOpenTelemetry already set: {}", e.getMessage());
        }
        log.info("OpenTelemetry configured with service name: {}, endpoint: {}", serviceName, otlpEndpoint);
        return openTelemetrySdk;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, serviceVersion);
    }

    @Bean
    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter(serviceName);
    }

    @PreDestroy
    public void shutdown() {
        if (sdkTracerProvider != null) {
            sdkTracerProvider.shutdown().join(5, TimeUnit.SECONDS);
        }
        if (sdkMeterProvider != null) {
            sdkMeterProvider.shutdown().join(5, TimeUnit.SECONDS);
        }
        log.info("OpenTelemetry SDK shut down");
    }
}
