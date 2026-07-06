plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:delivery-service:delivery-domain"))
    implementation(project(":services:delivery-service:delivery-application"))
    implementation(project(":common:common-infrastructure"))
    implementation("org.springframework.kafka:spring-kafka")
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
