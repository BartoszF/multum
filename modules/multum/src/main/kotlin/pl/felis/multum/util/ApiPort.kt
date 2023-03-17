package pl.felis.multum.util

import io.ktor.server.routing.*

fun Route.apiPort(build: Route.() -> Unit) {
    val discoveryPort =
        application.environment.config.propertyOrNull("multum.dicovery.port")?.getString()?.toInt() ?: 9091

    localPort(discoveryPort) {
        build()
    }
}
