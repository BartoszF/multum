package pl.felis.discoveryclient

import io.ktor.server.application.*
import pl.felis.discoveryclient.plugins.configureHTTP
import pl.felis.discoveryclient.plugins.configureMonitoring
import pl.felis.discoveryclient.plugins.configureRouting
import pl.felis.discoveryclient.plugins.configureSerialization
import pl.felis.discoveryclient.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
