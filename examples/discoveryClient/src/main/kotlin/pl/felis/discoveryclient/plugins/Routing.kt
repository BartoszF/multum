package pl.felis.discoveryclient.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.InetAddress

@Serializable
data class TempData(val msg: String, val num: Int, val hostname: String)

fun Application.configureRouting() {
    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/test") {
            call.respond(
                TempData(
                    "OK",
                    2137,
                    withContext(Dispatchers.IO) {
                        InetAddress.getLocalHost()
                    }.hostName
                )
            )
        }
    }
}
