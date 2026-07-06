plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

dependencies {
    implementation(project(":services:restaurant-service:restaurant-domain"))
    implementation(project(":services:restaurant-service:restaurant-application"))
    implementation(project(":services:restaurant-service:restaurant-dataaccess"))
    implementation(project(":services:restaurant-service:restaurant-messaging"))
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
    archiveFileName.set("restaurant-service.jar")
}
