plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:restaurant-service:restaurant-domain"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.mapstruct:mapstruct")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
