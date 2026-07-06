plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

dependencies {
    implementation(project(":services:notification-service:notification-domain"))
    implementation(project(":services:notification-service:notification-application"))
    implementation(project(":services:notification-service:notification-dataaccess"))
    implementation(project(":services:notification-service:notification-messaging"))
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
    archiveFileName.set("notification-service.jar")
}
