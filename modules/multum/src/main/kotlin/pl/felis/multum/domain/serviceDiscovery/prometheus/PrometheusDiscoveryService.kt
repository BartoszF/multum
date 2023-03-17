package pl.felis.multum.domain.serviceDiscovery.prometheus

import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import pl.felis.multum.domain.service.ServiceService

@Serializable
data class PrometheusDiscoveryServiceEntry(val targets: List<String>, val labels: Map<String, String>)

@Single
class PrometheusDiscoveryService(private val serviceService: ServiceService) {

    suspend fun handleRequest(call: ApplicationCall) {
        val list: List<PrometheusDiscoveryServiceEntry> = serviceService.getServiceMap()
            ?.map { Pair(it.key, it.value.values.filter { entry -> entry.prometheusMetrics }) }
            ?.map {
                PrometheusDiscoveryServiceEntry(
                    it.second.map { entry -> "${entry.ip}:${entry.port}" },
                    mapOf("service" to it.first)
                )
            }?.toList() ?: emptyList()

        call.respond(list)
    }
}
