plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:restaurant-service:restaurant-domain"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.kafka:spring-kafka")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
