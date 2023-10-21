package pl.felis.multum.domain.routing

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import pl.felis.multum.common.ssl.SslSettings
import pl.felis.multum.domain.discovery.DiscoveryService
import pl.felis.multum.plugins.appMicrometerRegistry
import java.net.ConnectException

val gatewayClient = HttpClient(Java) {
    engine {
        config {
            sslContext(SslSettings.getSslContext())
        }
        threadsCount = 8
        pipelining = true
        protocolVersion = java.net.http.HttpClient.Version.HTTP_2
    }
    install(ContentNegotiation) {
        json()
    }
    install(ContentEncoding) {
        deflate(1.0F)
        gzip(0.9F)
    }
}

fun Headers.appendFiltered(block: (Map.Entry<String, List<String>>) -> Unit) {
    this.toMap().filterKeys {
        it.lowercase() !in listOf(
            HttpHeaders.ContentType.lowercase(),
            HttpHeaders.ContentLength.lowercase(),
            HttpHeaders.TransferEncoding.lowercase(),
            HttpHeaders.ContentEncoding.lowercase(),
            HttpHeaders.Upgrade.lowercase(),
        )
    }.forEach(block)
}

@Single
class RoutingController(private val service: DiscoveryService) {

    suspend fun routeToNode(call: ApplicationCall) {
        call.application.log.info(
            "Handling call ${call.request.path()}, method ${call.request.httpMethod}, headers ${call.request.headers.toMap()}",
        )

        val (serviceName, path) = call.request.path().split("/", limit = 3).drop(1)

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
            appMicrometerRegistry.timer(
                "multum_node_request",
                Tags.of(Tag.of("service", serviceName), Tag.of("node", node.getKey())),
            ).record<HttpResponse> {
                runBlocking {
                    gatewayClient.request {
                        this.method = method
                        if (!body.isNullOrEmpty()) setBody(body)
                        this.host = serviceName
                        this.headers.apply {
                            call.request.headers.appendFiltered {
                                it.value.forEach { v ->
                                    append(it.key, v)
                                }
                            }
                        }
                        this.url.set(null, node.ip, node.port, "/$path")
                        call.request.accept()?.split(", ")?.map { ContentType.parse(it) }?.forEach { this.accept(it) }
                            ?: this.accept(ContentType.Application.Json)

                        this.contentType(call.request.contentType())
                    }
                }
            }
        } catch (e: ConnectException) {
            service.setNodeAsInactive(node, e)
            routeToNode(call)
            return
        }

        call.application.log.info("Response type ${response.contentType()}")

        response.headers.appendFiltered {
            it.value.forEach { v ->
                call.response.headers.append(it.key, v)
            }
        }

        val responseBody = response.bodyAsText()

        call.respondText(responseBody, response.contentType() ?: ContentType.Application.Json, response.status)
    }
}
