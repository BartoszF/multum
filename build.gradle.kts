val multumVersion: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("io.ktor.plugin") version "3.0.3"
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
