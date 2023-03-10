package pl.felis.multum.domain.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.koin.ktor.ext.inject
import pl.felis.multum.plugins.SslSettings

@Resource("/service")
class ServiceResource {
    @Resource("{name}")
    data class Service(val name: String, val resource: ServiceResource = ServiceResource()) {
        @Resource("register")
        data class Register(val service: Service)
    }
}

val gatewayClient = HttpClient(Jetty) {
    engine {
        sslContextFactory = SslContextFactory.Client(true).apply {
            sslContext = SslSettings.getSslContext()
        }
        clientCacheSize = 12
    }
    install(ContentNegotiation) {
        json()
    }
}

@Serializable
data class RegisterData(val port: Int)

fun Route.serviceRouting() {
    val service: ServiceService by inject()

    get<ServiceResource> {
        call.respond(service.getServices())
    }

    get<ServiceResource.Service> { serviceName ->
        val nodes = service.getNodes(serviceName.name)
        call.respond(nodes)
    }

    post<ServiceResource.Service.Register> { register ->
        val data = call.receive<RegisterData>()
        val ip = call.request.origin.remoteAddress
        service.register(ServiceNodeEntry(register.service.name, data.port, ip))
        call.application.log.info("Register service with name \"${register.service.name}\" on $ip:${data.port}")
        call.respondText("Register service with name \"${register.service.name}\" on $ip:${data.port}")
    }

    // host(service.getServices()) {
    route("{...}") {
        handle {
            call.application.log.info("Handling service ${call.request.host()}, method ${this.call.request.httpMethod}, headers ${call.request.headers}")

            val serviceName = call.request.host()
            val method = call.request.httpMethod
            val body: Any? =
                if (method in listOf(Post, Put, Patch)) call.receive() else null

            val nodes = service.getNodes(serviceName)

            if (nodes.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Service $serviceName not found")
                return@handle
            }

            val response = gatewayClient.request {
                this.method = method
                if (body != null) setBody(body)
                this.host = serviceName
                this.url.set("https", nodes[0].ip, nodes[0].port, call.request.path())
                this.accept(
                    call.request.accept()?.let { it1 -> ContentType.parse(it1) }
                        ?: ContentType.Application.Json
                )
            }

            call.respondText(response.body(), response.contentType() ?: ContentType.Application.Json)
        }
    }
    // }
}
