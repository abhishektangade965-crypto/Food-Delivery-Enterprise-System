plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

dependencies {
    implementation(project(":services:order-service:order-domain"))
    implementation(project(":services:order-service:order-application"))
    implementation(project(":services:order-service:order-dataaccess"))
    implementation(project(":services:order-service:order-messaging"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.bootJar {
    archiveFileName.set("order-service.jar")
}
