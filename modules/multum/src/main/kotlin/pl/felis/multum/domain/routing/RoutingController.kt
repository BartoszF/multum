package pl.felis.multum.domain.routing

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.core.annotation.Single
import pl.felis.multum.domain.service.ServiceService
import java.net.ConnectException

val gatewayClient = HttpClient(Java) {
    engine {
//        config {
//            sslContext(SslSettings.getSslContext())
//        }
        threadsCount = 8
        pipelining = true
//        protocolVersion = java.net.http.HttpClient.Version.HTTP_2
    }
    install(ContentNegotiation) {
        json()
    }
}

@Single
class RoutingController(private val service: ServiceService) {

    suspend fun routeToNode(call: ApplicationCall) {
        call.application.log.info("Handling service ${call.request.host()}, method ${call.request.httpMethod}, headers ${call.request.headers}")

        val serviceName = call.request.host()
        val method = call.request.httpMethod
        val body: String? =
            if (method in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) call.receiveText() else null

        if (!service.serviceExists(serviceName)) {
            call.application.log.error("Service $serviceName not found")
            call.respond(HttpStatusCode.BadRequest, "Service $serviceName not found")
        }

        val node = service.roundRobinNodes(serviceName)

        call.application.log.info("Routing request to ${node.getKey()}")
        val response = try {
            gatewayClient.request {
                this.method = method
                if (!body.isNullOrEmpty()) setBody(body)
                this.host = serviceName
                // TODO: Change to https
                this.url.set("http", node.ip, node.port, call.request.path())
                this.accept(
                    call.request.accept()?.let { it1 -> ContentType.parse(it1) }
                        ?: ContentType.Application.Json
                )
            }
        } catch (e: ConnectException) {
            service.setNodeAsInactive(node, e)
            routeToNode(call)
            return
        }

        call.respondText(response.body(), response.contentType() ?: ContentType.Application.Json)
    }
}
