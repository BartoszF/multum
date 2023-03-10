package pl.felis.multum.client.discovery

import io.ktor.client.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.eclipse.jetty.util.ssl.SslContextFactory

val client = HttpClient(Jetty) {
    engine {
        sslContextFactory = SslContextFactory.Client(true)
        clientCacheSize = 12
    }
    install(ContentNegotiation) {
        json()
    }
}
