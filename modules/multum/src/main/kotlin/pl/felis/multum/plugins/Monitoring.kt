package pl.felis.multum.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.*
import org.slf4j.event.*
import pl.felis.multum.common.util.UUID_REGEX
import java.util.UUID

val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc("call-id")
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty() and
                UUID_REGEX.matcher(callId).matches()
        }
        replyToHeader(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString() }
    }

    val discoveryPort =
        environment.config.propertyOrNull("multum.dicovery.port")?.getString()?.toInt() ?: 9091

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    routing {
        localPort(discoveryPort) {
            get("/metrics") {
                call.respond(appMicrometerRegistry.scrape())
            }
        }
    }
}
