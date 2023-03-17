package pl.felis.multum.domain.serviceDiscovery.prometheus

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import pl.felis.multum.util.apiPort

fun Route.prometheusDiscoveryRoute() {
    val prometheusDiscoveryService: PrometheusDiscoveryService by inject()

    apiPort {
        get("/serviceDiscovery/prometheus") {
            prometheusDiscoveryService.handleRequest(call)
        }
    }
}
