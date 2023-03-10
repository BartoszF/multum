package pl.felis.multum.plugins

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import pl.felis.multum.MultumModule

fun Application.configureKoin() {
    val app = this
    install(Koin) {
        slf4jLogger()
        modules(MultumModule().module, module { single { app } })
    }
}
