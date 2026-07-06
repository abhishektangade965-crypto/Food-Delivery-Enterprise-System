plugins {
    id("java-library")
}

dependencies {
    api(project(":common:common-domain"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.postgresql:postgresql")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
