package pl.felis.multum.domain.service

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.core.annotation.Single
import pl.felis.multum.common.dao.ByeData
import pl.felis.multum.common.dao.RegisterData

@Single
class ServiceController(private val service: ServiceService) {

    suspend fun register(register: ServiceResource.Service.Register, call: ApplicationCall) {
        val data = call.receive<RegisterData>()
        val ip = call.request.origin.remoteAddress
        service.register(ServiceNodeEntry(register.service.name, data.port, ip))
        call.application.log.info("Register service with name \"${register.service.name}\" on $ip:${data.port}")
        call.respondText("Register service with name \"${register.service.name}\" on $ip:${data.port}")
    }

    suspend fun heartbeat(heartbeat: ServiceResource.Service.Heartbeat, call: ApplicationCall) {
        call.application.log.info("Heartbeat for ${heartbeat.service.name}...")
        val data = call.receive<RegisterData>()
        val ip = call.request.origin.remoteAddress
        service.heartbeat(ServiceNodeEntryQuery(heartbeat.service.name, data.port, ip))
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
        service.remove(ServiceNodeEntryQuery(serviceName, data.port, ip))
        call.respondText("BYE")
    }
}
