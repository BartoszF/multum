plugins {
    kotlin("jvm") version "1.8.10"
//    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("maven-publish")
}

group = "pl.felis"
version = "0.0.3"

repositories {
    mavenCentral()
}
