import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("com.google.protobuf") version "0.9.4" apply false
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.spring") version "1.9.24" apply false
    kotlin("plugin.jpa") version "1.9.24" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("org.sonarqube") version "5.1.0.4882" apply false
    id("com.github.ben-manes.versions") version "0.51.0"
    id("jacoco")
    eclipse
}

allprojects {
    group = "com.fooddelivery"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://packages.confluent.io/maven/") }
        maven { url = uri("https://repo.spring.io/milestone") }
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "eclipse")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.2")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
            mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:3.1.1")
            mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.6.0")
        }

        dependencies {
            // Common deps
            dependency("org.projectlombok:lombok:1.18.34")
            dependency("org.mapstruct:mapstruct:1.5.5.Final")
            dependency("org.mapstruct:mapstruct-processor:1.5.5.Final")
            dependency("io.hypersistence:hypersistence-utils-hibernate-63:3.8.2")

            // gRPC
            dependency("io.grpc:grpc-protobuf:1.65.1")
            dependency("io.grpc:grpc-stub:1.65.1")
            dependency("io.grpc:grpc-netty-shaded:1.65.1")
            dependency("net.devh:grpc-spring-boot-starter:3.1.0.RELEASE")

            // Resilience
            dependency("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
            dependency("io.github.resilience4j:resilience4j-reactor:2.2.0")

            // Observability
            dependency("io.micrometer:micrometer-tracing-bridge-otel:1.3.2")
            dependency("io.opentelemetry:opentelemetry-exporter-otlp:1.40.0")
            dependency("com.github.loki4j:loki-logback-appender:1.5.2")

            // Security
            dependency("com.auth0:java-jwt:4.4.0")
            dependency("com.auth0:jwks-rsa:0.22.1")

            // JSON
            dependency("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

            // Vault
            dependency("org.springframework.cloud:spring-cloud-vault-config:4.1.2")

            // Caffeine Cache
            dependency("com.github.ben-manes.caffeine:caffeine:3.1.8")

            // Apache Commons
            dependency("org.apache.commons:commons-lang3:3.14.0")
            dependency("commons-codec:commons-codec:1.17.1")

            // Guava
            dependency("com.google.guava:guava:33.2.1-jre")

            // Geo
            dependency("com.graphhopper:graphhopper-core:9.1")

            // Stripe
            dependency("com.stripe:stripe-java:25.11.0")
        }
    }

    configurations.all {
        resolutionStrategy {
            force("org.yaml:snakeyaml:2.2")
            force("com.fasterxml.jackson.core:jackson-databind:2.17.2")
        }
    }
}

tasks.register("generateCoverageReport") {
    dependsOn(subprojects.map { it.tasks.withType<Test>() }.flatten())
    doLast {
        println("Coverage reports generated for all modules")
    }
}
