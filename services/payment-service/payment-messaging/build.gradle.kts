plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:payment-service:payment-domain"))
    implementation(project(":services:payment-service:payment-application"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.kafka:spring-kafka")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
