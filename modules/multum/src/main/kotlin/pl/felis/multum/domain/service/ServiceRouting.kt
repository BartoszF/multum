package pl.felis.multum.domain.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
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
import org.koin.ktor.ext.inject
import pl.felis.multum.domain.routing.RoutingController

@Resource("/service")
class ServiceResource {
    @Resource("{name}")
    data class Service(val name: String, val resource: ServiceResource = ServiceResource()) {
        @Resource("register")
        data class Register(val service: Service)

        @Resource("heartbeat")
        class Heartbeat(val service: Service)

        @Resource("bye")
        class Bye(val service: Service)
    }
}

fun Route.serviceRouting() {
    val serviceController: ServiceController by inject()
    val routingController: RoutingController by inject()
    val discoveryPort =
        application.environment.config.propertyOrNull("multum.dicovery.port")?.getString()?.toInt() ?: 9091

    localPort(discoveryPort) {
        get<ServiceResource> {
            serviceController.getServices(call)
        }

        get<ServiceResource.Service> { serviceName ->
            serviceController.getNodes(serviceName.name, call)
        }

        post<ServiceResource.Service.Register> { register ->
            serviceController.register(register, call)
        }

        post<ServiceResource.Service.Heartbeat> { heartbeat ->
            serviceController.heartbeat(heartbeat, call)
        }

        post<ServiceResource.Service.Bye> { bye ->
            serviceController.bye(bye.service.name, call)
        }
    }

    route("{...}") {
        handle {
            routingController.routeToNode(call)
        }
    }
}
