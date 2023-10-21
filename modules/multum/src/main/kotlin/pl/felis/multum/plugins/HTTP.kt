package pl.felis.multum.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import pl.felis.multum.ConfigKeys

fun Application.configureHTTP() {
    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                else -> null
            }
        }
    }
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }
    install(DefaultHeaders) {
        header("X-Engine", "Multum") // will send this header with each response
    }

    val allowedHeaders = environment.config.tryGetStringList(ConfigKeys.allowedHeaders)

    install(CORS) {
        allowCredentials = true
        allowSameOrigin = true
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowedHeaders?.forEach {
            allowHeader(it)
        }
        anyHost()
    }
//    routing {
//        openAPI(path = "openapi")
//    }
//    routing {
//        swaggerUI(path = "openapi")
//    }
}
