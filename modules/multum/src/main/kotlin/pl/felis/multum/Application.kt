package pl.felis.multum

import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import pl.felis.multum.plugins.*
import pl.felis.multum.plugins.configureHTTP
import pl.felis.multum.plugins.configureMonitoring
import pl.felis.multum.plugins.configureRouting
import pl.felis.multum.plugins.configureSerialization

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureKoin()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
