package pl.felis.multum

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import pl.felis.multum.plugins.*
import pl.felis.multum.plugins.configureHTTP
import pl.felis.multum.plugins.configureMonitoring
import pl.felis.multum.plugins.configureRouting
import pl.felis.multum.plugins.configureSerialization

object ConfigKeys {
    const val HOST_CONFIG_PATH = "ktor.deployment.host"
    const val PORT_CONFIG_PATH = "ktor.deployment.port"

    const val hostSslPortPath = "ktor.deployment.sslPort"
    const val hostSslKeyAlias = "ktor.security.ssl.keyAlias"
    const val hostSslKeyStorePassword = "ktor.security.ssl.keyStorePassword"
    const val hostSslPrivateKeyPassword = "ktor.security.ssl.privateKeyPassword"

    const val ALLOWED_HEADERS = "multum.cors.allowHeaders"
    const val DISCOVERY_PORT = "multum.discovery.port"
}

fun main(args: Array<String>) {
    val cliConfig = CommandLineConfig(args)

    embeddedServer(
        factory = Netty,
        environment = cliConfig.environment,
        configure = {
            loadCommonConfiguration(cliConfig.rootConfig.environment.config)
            envConfig(cliConfig.environment.config)
        },
    ).start(true)
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureKoin()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
