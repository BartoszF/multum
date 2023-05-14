package pl.felis.discoveryclient.plugins

import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import pl.felis.multum.client.MultumClient
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

    val multumClient = MultumClient(this)

    routing {
        get("/test") {
            call.respond(
                TempData(
                    "OK",
                    2137,
                    withContext(Dispatchers.IO) {
                        InetAddress.getLocalHost()
                    }.hostName,
                ),
            )
        }

        get("/client") {
            val response = multumClient.call("discovery-client") {
                method = HttpMethod.Get
                path = "/test"
            }

            call.response.status(response.status)
            call.respondText(response.body(), response.contentType() ?: ContentType.Application.Json)
        }
    }
}
