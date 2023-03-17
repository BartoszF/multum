val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "pl.felis"
            artifactId = "multum-common"
            version = version

            from(components["java"])
        }
    }
}
