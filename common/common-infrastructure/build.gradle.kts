plugins {
    id("java-library")
}

dependencies {
    api(project(":common:common-domain"))
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.springframework.kafka:spring-kafka")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("io.opentelemetry:opentelemetry-exporter-otlp")
    api("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    api("org.mapstruct:mapstruct")
    annotationProcessor("org.mapstruct:mapstruct-processor")
    api("io.github.resilience4j:resilience4j-spring-boot3")
    api("com.github.ben-manes.caffeine:caffeine")
    api("org.redisson:redisson-spring-boot-starter:3.32.0")
    api("org.springframework.boot:spring-boot-starter-quartz")
    api("io.confluent:kafka-avro-serializer:7.6.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
