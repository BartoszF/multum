package pl.felis.multum.client

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import pl.felis.multum.common.ssl.SslSettings
import java.net.http.HttpClient

fun Application.getHttpClient(): io.ktor.client.HttpClient {
    val token = environment.config.tryGetString("interService.bearer.token")
    return HttpClient(Java) {
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
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            maxRetries = 3
            retryIf { _, response ->
                !response.status.isSuccess()
            }
            exponentialDelay(base = 1.4, randomizationMs = 200, maxDelayMs = 5000)
        }
        if (token != null) {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token, "")
                    }
                }
            }
        }
    }
}
