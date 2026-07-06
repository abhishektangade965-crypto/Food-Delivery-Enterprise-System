plugins {
    id("java-library")
}

dependencies {
    implementation(project(":services:search-service:search-domain"))
    implementation(project(":common:common-dataaccess"))
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    annotationProcessor("org.projectlombok:lombok")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
