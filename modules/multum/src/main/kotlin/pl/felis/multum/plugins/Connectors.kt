package pl.felis.multum.plugins

import io.ktor.server.config.*
import io.ktor.server.engine.*
import pl.felis.multum.ConfigKeys
import pl.felis.multum.common.ssl.SslSettings

fun ApplicationEngine.Configuration.envConfig(config: ApplicationConfig) {
    val host = config.tryGetString(ConfigKeys.HOST_CONFIG_PATH) ?: "0.0.0.0"
    val port = config.tryGetString(ConfigKeys.PORT_CONFIG_PATH)?.toInt() ?: 8080

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
            { (sslPrivateKeyPassword ?: "").toCharArray() },
        ) {
            this.host = host
            this.port = sslPort.toInt()
        }
    }

    val multumDiscoveryPort = config.propertyOrNull(ConfigKeys.DISCOVERY_PORT)?.getString()?.toInt() ?: 9091

    connector {
        this.host = host
        this.port = multumDiscoveryPort
    }
}
