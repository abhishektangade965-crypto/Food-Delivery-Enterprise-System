plugins { id("java-library") }
dependencies {
    api(project(":common:common-domain"))
    api("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
