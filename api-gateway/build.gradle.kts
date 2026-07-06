import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

group = "com.fooddelivery"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    // Spring Cloud Gateway (WebFlux-based reactive gateway)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // Spring Boot Actuator for health, metrics, info endpoints
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Security (WebFlux)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // OAuth2 Resource Server for JWT RS256 validation
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Reactive Redis for rate limiting via RequestRateLimiter filter
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Circuit Breaker using Reactor Resilience4j
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // Spring Cloud LoadBalancer for lb:// scheme resolution
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // Micrometer OpenTelemetry tracing bridge
    implementation("io.micrometer:micrometer-tracing-bridge-otel")

    // OpenTelemetry OTLP exporter to send traces to OTEL Collector
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // Micrometer Prometheus registry for scraping
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Jackson for JSON serialization in filters / error handlers
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Apache Commons Lang for utilities
    implementation("org.apache.commons:commons-lang3")

    // Lombok for boilerplate reduction
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Loki4j Logback appender for log shipping to Grafana Loki
    implementation("com.github.loki4j:loki-logback-appender:1.5.2")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.2")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.6.0")
    }
}

tasks.withType<BootJar> {
    archiveFileName.set("api-gateway.jar")
    mainClass.set("com.fooddelivery.gateway.ApiGatewayApplication")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "--enable-preview",
        "-parameters"
    ))
    options.release.set(21)
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}
