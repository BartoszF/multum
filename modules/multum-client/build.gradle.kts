val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

group = "pl.felis"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-client-jetty:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
