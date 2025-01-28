import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project
val tcnative_version: String by project
val koin_version: String by project
val koin_ktor_version: String by project
val koin_ksp_version: String by project
val caffeine_version: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

application {
    mainClass.set("pl.felis.multum.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
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
    implementation("io.ktor:ktor-server-caching-headers")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-request-validation")
    implementation("io.ktor:ktor-server-metrics")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-network-tls-certificates")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-java")
    implementation("io.ktor:ktor-client-content-negotiation")

    if (tcnative_classifier != null) {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version:$tcnative_classifier")
    } else {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version")
    }

    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor3:$koin_ktor_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_ktor_version")
    implementation("io.insert-koin:koin-annotations:$koin_ksp_version")

    implementation("com.github.ben-manes.caffeine:caffeine:$caffeine_version")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation(project(":modules:multum-common"))
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-client-encoding")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    ksp("io.insert-koin:koin-ksp-compiler:$koin_ksp_version")
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "pl.felis.multum.ApplicationKt"))
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
