package pl.felis.multum

import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import pl.felis.multum.common.ssl.SslSettings
import pl.felis.multum.plugins.*
import pl.felis.multum.plugins.configureHTTP
import pl.felis.multum.plugins.configureMonitoring
import pl.felis.multum.plugins.configureRouting
import pl.felis.multum.plugins.configureSerialization

object ConfigKeys {
    const val hostConfigPath = "ktor.deployment.host"
    const val portConfigPath = "ktor.deployment.port"

    const val hostSslPortPath = "ktor.deployment.sslPort"
    const val hostSslKeyAlias = "ktor.security.ssl.keyAlias"
    const val hostSslKeyStorePassword = "ktor.security.ssl.keyStorePassword"
    const val hostSslPrivateKeyPassword = "ktor.security.ssl.privateKeyPassword"
}

fun main(args: Array<String>) { // = io.ktor.server.netty.EngineMain.main(args)
    val env = applicationEngineEnvironment { envConfig(args) }
    embeddedServer(Netty, env).start(true)
}

fun ApplicationEngineEnvironmentBuilder.envConfig(args: Array<String>) {
    config = commandLineEnvironment(args).config // TODO: Don't create whole environment to load config

    val host = config.tryGetString(ConfigKeys.hostConfigPath) ?: "0.0.0.0"
    val port = config.tryGetString(ConfigKeys.portConfigPath)?.toInt() ?: 8080

    connector {
        this.host = host
        this.port = port
    }

    val sslPort = config.tryGetString(ConfigKeys.hostSslPortPath)
    val sslKeyStorePassword = config.tryGetString(ConfigKeys.hostSslKeyStorePassword)?.trim()
    val sslPrivateKeyPassword = config.tryGetString(ConfigKeys.hostSslPrivateKeyPassword)?.trim()
    val sslKeyAlias = config.tryGetString(ConfigKeys.hostSslKeyAlias) ?: "mykey"

    if (sslPort != null) {
        sslConnector(
            keyStore = SslSettings.getKeyStore(),
            sslKeyAlias,
            { (sslKeyStorePassword ?: "").toCharArray() },
            { (sslPrivateKeyPassword ?: "").toCharArray() }
        ) {
            this.host = host
            this.port = sslPort.toInt()
        }
    }

    val multumDiscoveryPort = config.propertyOrNull("multum.discovery.port")?.getString()?.toInt() ?: 9091

    connector {
        this.host = host
        this.port = multumDiscoveryPort
    }
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureKoin()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
