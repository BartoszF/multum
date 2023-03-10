package pl.felis.multum.plugins

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.felis.multum.domain.service.serviceRouting

fun Application.configureRouting() {
    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            call.application.log.error("", cause)
        }
    }
    routing {
        get("/") {
            call.respond("HELO")
        }

        serviceRouting()
    }
}
