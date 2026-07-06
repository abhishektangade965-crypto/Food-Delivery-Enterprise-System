plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:order-service:order-domain"))
    implementation(project(":services:order-service:order-application"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.kafka:spring-kafka")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
