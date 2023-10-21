val multumVersion: String by project

plugins {
    kotlin("jvm") version "1.8.10"
//    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("maven-publish")
}

group = "pl.felis"
version = multumVersion

repositories {
    mavenCentral()
}

allprojects {
    version = multumVersion
}
