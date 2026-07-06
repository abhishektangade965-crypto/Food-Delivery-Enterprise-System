plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:search-service:search-domain"))
    implementation(project(":services:search-service:search-application"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.kafka:spring-kafka")
    annotationProcessor("org.projectlombok:lombok")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
