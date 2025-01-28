import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project
val tcnative_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "pl.felis"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val osName = System.getProperty("os.name").toLowerCase()
val tcnative_classifier =
    when {
        osName.contains("win") -> "windows-x86_64"
        osName.contains("linux") -> "linux-x86_64"
        osName.contains("mac") -> "osx-x86_64"
        else -> null
    }

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-forwarded-header")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")

    if (tcnative_classifier != null) {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version:$tcnative_classifier")
    } else {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version")
    }

    implementation(project(":modules:multum-client"))
//    implementation("com.github.BartoszF.multum:multum-client:0.2.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "pl.felis.discoveryClient.ApplicationKt"))
        }
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "21"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "21"
}
