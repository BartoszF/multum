package pl.felis.multum.client.discovery

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import pl.felis.multum.common.dao.HeartbeatData
import pl.felis.multum.common.dao.RegisterData
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class MultumPluginConfiguration(config: ApplicationConfig) {
    var port: Int? = config.tryGetString("service.port")?.toInt()
    var serviceName: String? = config.tryGetString("service.name")
    var endpoint: String? = config.tryGetString("discovery.endpoint")
    var heartbeatInterval: Long = config.tryGetString("discovery.heartbeatInSeconds")?.toLong() ?: 1L
}

val MultumPlugin = createApplicationPlugin(
    "multum-client",
    "multum",
    { config: ApplicationConfig -> MultumPluginConfiguration(config) }
) {
    val multumEndpoint = pluginConfig.endpoint
    val serviceName = pluginConfig.serviceName
    val heartbeatInterval = pluginConfig.heartbeatInterval
    val port =
        pluginConfig.port ?: application.environment.config.propertyOrNull("ktor.deployment.sslPort")?.getString()
            ?.toInt()

    val serviceEndpoint = "$multumEndpoint/service/$serviceName"

    val client = HttpClient(Java) {
        engine {
//            config {
//                sslContext(SslSettings.getSslContext())
//            }
            threadsCount = 8
            pipelining = true
//            protocolVersion = java.net.http.HttpClient.Version.HTTP_2
        }
        install(ContentNegotiation) {
            json()
        }
    }

    fun heartbeat() {
        runBlocking {
            application.log.debug("Multum heartbeat...")
            client.post("$serviceEndpoint/heartbeat") {
                contentType(ContentType.Application.Json)
                setBody(HeartbeatData(port!!))
            }
        }
    }

    var heartbeatTask: TimerTask? = null

    on(MonitoringEvent(ApplicationStarted)) { application ->
        if (port == null) throw RuntimeException("No port defined for multum service or application not running with SSL.")

        runBlocking {
            application.log.debug("Registering in multum...")
            try {
                client.post("$serviceEndpoint/register") {
                    contentType(ContentType.Application.Json)
                    setBody(RegisterData(port))
                }
                application.log.info("Succesfully registered in multum.")
                val interval = TimeUnit.SECONDS.toMillis(heartbeatInterval)
                heartbeatTask = Timer("multumHeartbeat").schedule(interval, interval) {
                    heartbeat()
                }
            } catch (e: Throwable) {
                application.log.error("ERROR", e)
            }
        }
    }
    on(MonitoringEvent(ApplicationStopped)) { application ->
        heartbeatTask?.cancel()
        // TODO: Endpoint in multum
        /*runBlocking {
            application.log.debug("Say bye to multum...")
            client.post("$multumEndpoint/service/$serviceName/bye") {
                contentType(ContentType.Application.Json)
                setBody(RegisterData(port))
            }
            application.log.info("Goodbye multum")
        }*/
        // Release resources and unsubscribe from events
        application.environment.monitor.unsubscribe(ApplicationStarted) {}
        application.environment.monitor.unsubscribe(ApplicationStopped) {}
    }
}
