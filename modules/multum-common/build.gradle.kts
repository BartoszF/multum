val ktor_version: String by project

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
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
