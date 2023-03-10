package pl.felis

import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import pl.felis.plugins.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
