plugins {
    id("java-library")
}

dependencies {
    api("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.apache.commons:commons-lang3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
