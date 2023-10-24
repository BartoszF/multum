package pl.felis.multum.domain.discovery

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.core.annotation.Single
import pl.felis.multum.common.dao.ByeData
import pl.felis.multum.common.dao.HeartbeatData
import pl.felis.multum.common.dao.RegisterData

@Single
class DiscoveryController(private val service: DiscoveryService) {

    suspend fun register(register: ServiceResource.Service.Register, call: ApplicationCall) {
        val data = call.receive<RegisterData>()
        val ip = call.request.origin.remoteAddress
        val node = service.register(ServiceNodeEntryQuery(register.service.name, data.port, ip), data)
        call.application.log.info("Registered service ${node.getKey()}")
        call.respondText("OK")
    }

    suspend fun heartbeat(heartbeat: ServiceResource.Service.Heartbeat, call: ApplicationCall) {
        val data = call.receive<HeartbeatData>()
        val ip = call.request.origin.remoteAddress
        val node = ServiceNodeEntryQuery(heartbeat.service.name, data.port, ip)
        service.heartbeat(node)
        call.application.log.debug("Heartbeat for ${node.getKey()}...")
        call.respondText("OK")
    }

    suspend fun getServices(call: ApplicationCall) {
        call.respond(service.getServices())
    }

    suspend fun getNodes(serviceName: String, call: ApplicationCall) {
        call.respond(service.getNodes(serviceName))
    }

    suspend fun bye(serviceName: String, call: ApplicationCall) {
        val data = call.receive<ByeData>()
        val ip = call.request.origin.remoteAddress
        val node = ServiceNodeEntryQuery(serviceName, data.port, ip)
        service.remove(node)
        call.application.log.info("Removed ${node.getKey()}")
        call.respondText("BYE")
    }
}
