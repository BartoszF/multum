package pl.felis.multum.client.discovery

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.utils.io.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import pl.felis.multum.common.dao.ByeData
import pl.felis.multum.common.dao.HeartbeatData
import pl.felis.multum.common.dao.RegisterData
import java.lang.RuntimeException
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class MultumHandler(config: MultumPluginConfiguration, private val application: Application) {
    private val multumEndpoint = config.endpoint
    private val serviceName = config.serviceName
    private val heartbeatInterval = config.heartbeatInterval
    private val servicePort =
        config.port ?: application.environment.config.propertyOrNull("ktor.deployment.sslPort")?.getString()
            ?.toInt()

    private val serviceEndpoint = "$multumEndpoint/service/$serviceName"

    private var heartbeatTask: TimerTask? = null

    private val client = HttpClient(Java) {
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

    fun initialize() {
        if (servicePort == null) throw RuntimeException("No port defined for multum service or application not running with SSL.")

        runBlocking {
            while (true) {
                application.log.debug("Registering in multum...")
                try {
                    client.post("$serviceEndpoint/register") {
                        contentType(ContentType.Application.Json)
                        setBody(RegisterData(servicePort))
                    }
                    application.log.info("Succesfully registered in multum.")
                    val interval = TimeUnit.SECONDS.toMillis(heartbeatInterval)
                    heartbeatTask = Timer("multumHeartbeat").schedule(interval, interval) {
                        heartbeat()
                    }

                    break
                } catch (e: Throwable) {
                    application.log.error("Failed registering to multum. Will retry in 10 seconds...", e)
                    delay(TimeUnit.SECONDS.toMillis(10))
                }
            }
        }
    }

    private fun heartbeat() {
        runBlocking {
            application.log.debug("Multum heartbeat...")
            try {
                client.post("$serviceEndpoint/heartbeat") {
                    contentType(ContentType.Application.Json)
                    setBody(HeartbeatData(servicePort!!))
                }
            } catch (e: ConnectException) {
                application.log.error("Lost connection to multum. Will try to register again...")
                heartbeatTask?.cancel()
                initialize()
            } catch (e: Throwable) {
                application.log.error("Multum heartbeat failed. Will retry in $heartbeatInterval seconds...")
            }
        }
    }

    fun dispose() {
        heartbeatTask?.cancel()
        runBlocking {
            application.log.debug("Say bye to multum...")
            client.post("$multumEndpoint/service/$serviceName/bye") {
                contentType(ContentType.Application.Json)
                setBody(ByeData(servicePort!!))
            }
            application.log.info("Goodbye multum")
        }
    }
}
