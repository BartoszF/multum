package pl.felis.multum.client.discovery

import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.config.*

class MultumPluginConfiguration(config: ApplicationConfig) {
    var port: Int? = config.tryGetString("service.port")?.toInt()
    var serviceName: String? = config.tryGetString("service.name")
    var endpoint: String? = config.tryGetString("discovery.endpoint")
    var heartbeatInterval: Long = config.tryGetString("discovery.heartbeatInSeconds")?.toLong() ?: 1L
}

val MultumDiscoveryPlugin = createApplicationPlugin(
    "multum-discovery-client",
    "multum",
    { config: ApplicationConfig -> MultumPluginConfiguration(config) },
) {
    val handler = MultumDiscoveryHandler(pluginConfig, application)

    on(MonitoringEvent(ApplicationStarted)) { application ->
        handler.initialize()
    }
    on(MonitoringEvent(ApplicationStopped)) { application ->
        handler.dispose()
        // Release resources and unsubscribe from events
        application.environment.monitor.unsubscribe(ApplicationStarted) {}
        application.environment.monitor.unsubscribe(ApplicationStopped) {}
    }
}
