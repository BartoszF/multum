package pl.felis.multum.client

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import pl.felis.multum.common.ssl.SslSettings
import java.net.http.HttpClient

val client = HttpClient(Java) {
    engine {
        config {
            sslContext(SslSettings.getSslContext())
        }
        threadsCount = 8
        pipelining = true
        protocolVersion = HttpClient.Version.HTTP_2
    }
    install(ContentNegotiation) {
        json()
    }
}
