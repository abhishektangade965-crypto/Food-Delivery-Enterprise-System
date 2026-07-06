plugins {
    id("java-library")
}

dependencies {
    api(project(":common:common-domain"))
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
