plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:restaurant-service:restaurant-domain"))
    implementation(project(":common:common-dataaccess"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.mapstruct:mapstruct")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
