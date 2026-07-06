plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

dependencies {
    implementation(project(":services:delivery-service:delivery-domain"))
    implementation(project(":services:delivery-service:delivery-application"))
    implementation(project(":services:delivery-service:delivery-dataaccess"))
    implementation(project(":services:delivery-service:delivery-messaging"))
    
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
    archiveFileName.set("delivery-service.jar")
}
