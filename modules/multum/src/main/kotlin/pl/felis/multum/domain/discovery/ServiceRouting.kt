package pl.felis.multum.domain.discovery

import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import pl.felis.multum.common.MultumPaths
import pl.felis.multum.domain.routing.RoutingController
import pl.felis.multum.plugins.appMicrometerRegistry
import pl.felis.multum.util.apiPort

@Resource("/${MultumPaths.Service}")
class ServiceResource {
    @Resource("{name}")
    data class Service(val name: String, val resource: ServiceResource = ServiceResource()) {
        @Resource(MultumPaths.Register)
        data class Register(val service: Service)

        @Resource(MultumPaths.Heartbeat)
        class Heartbeat(val service: Service)

        @Resource(MultumPaths.Bye)
        class Bye(val service: Service)
    }
}

fun Route.serviceRouting() {
    val discoveryController: DiscoveryController by inject()
    val routingController: RoutingController by inject()

    apiPort {
        get<ServiceResource> {
            discoveryController.getServices(call)
        }

        get<ServiceResource.Service> { serviceName ->
            discoveryController.getNodes(serviceName.name, call)
        }

        post<ServiceResource.Service.Register> { register ->
            discoveryController.register(register, call)
        }

        post<ServiceResource.Service.Heartbeat> { heartbeat ->
            discoveryController.heartbeat(heartbeat, call)
        }

        post<ServiceResource.Service.Bye> { bye ->
            discoveryController.bye(bye.service.name, call)
        }
    }

    route("{...}") {
        handle {
            appMicrometerRegistry
                .timer("multum_routing_call", Tags.of(Tag.of("service", call.request.host())))
                .record<Unit> {
                    runBlocking {
                        routingController.routeToNode(call)
                    }
                }
        }
    }
}
