package pl.felis.multum.client.discovery

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class RegisterData(val port: Int)

fun Application.setupDiscoveryClient(servicePort: Int? = null) {
    val multumEndpoint: String = environment.config.property("multum.discovery.endpoint").getString()
    val serviceName: String = environment.config.property("multum.service.name").getString()
    val port: Int = servicePort ?: environment.config.property("ktor.deployment.sslPort").getString().toInt()

    runBlocking {
        this@setupDiscoveryClient.log.debug("Registering in multum...")
        client.post("$multumEndpoint/service/$serviceName/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterData(port))
        }
        this@setupDiscoveryClient.log.info("Succesfully registered in multum.")
    }
}
