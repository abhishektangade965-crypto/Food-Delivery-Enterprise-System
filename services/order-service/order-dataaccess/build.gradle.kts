plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:order-service:order-domain"))
    implementation(project(":common:common-dataaccess"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
