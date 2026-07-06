plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:order-service:order-domain"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.mapstruct:mapstruct")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
