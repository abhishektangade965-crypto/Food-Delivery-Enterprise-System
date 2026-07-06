plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:notification-service:notification-domain"))
    implementation(project(":services:notification-service:notification-application"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.kafka:spring-kafka")
    annotationProcessor("org.projectlombok:lombok")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
