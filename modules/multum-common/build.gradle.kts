val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
}
