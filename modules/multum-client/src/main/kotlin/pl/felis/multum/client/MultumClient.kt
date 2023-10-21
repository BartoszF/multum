package pl.felis.multum.client

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*

class MultumCallBuilder {
    var path: String = "/"
    var method: HttpMethod = HttpMethod.Get
    val headers: HeadersBuilder = HeadersBuilder()
    var body: Any = EmptyContent
}

class MultumClient(private val application: Application) {
    private val endpoint = application.environment.config.tryGetString("multum.discovery.endpoint")

    private val client = application.getHttpClient()

    suspend fun call(serviceName: String, request: MultumCallBuilder.() -> Unit): HttpResponse {
        val details = MultumCallBuilder().apply(request)
        application.log.debug("Routing request to $serviceName${details.path} via multum.")
        return client.request("$endpoint/$serviceName${details.path}") {
            method = details.method
            setBody(details.body)
            headers {
                details.headers
            }
        }
    }
}
