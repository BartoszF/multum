package pl.felis.multum.domain.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

enum class NodeStatus {
    ACTIVE,
    INACTIVE
}

@Serializable
data class ServiceNodeEntry(
    val name: String,
    val port: Int,
    val ip: String,
    var status: NodeStatus = NodeStatus.ACTIVE,
    val lastActivity: Instant = Clock.System.now()
) {
    fun getKey(): String = "$name@$ip:$port"
}

@Single
class ServiceService(private val application: Application) { // TODO: This name...
    private val serviceCache: Cache<String, MutableMap<String, ServiceNodeEntry>> =
        Caffeine.newBuilder().recordStats().build()
    private val expiryTask: TimerTask
    private val expiryTime: Long =
        application.environment.config.property("multum.discovery.expiryInSeconds").getString().toLong()

    init {
        expiryTask = Timer("serviceNodeExpiry").schedule(0, TimeUnit.SECONDS.toMillis(1)) {
            invalidateNodes()
        }
    }

    fun register(entry: ServiceNodeEntry) {
        val service = serviceCache.get(entry.name) {
            ConcurrentHashMap()
        }

        service[entry.getKey()] = entry
    }

    fun getServices(): List<String> {
        return serviceCache.asMap().keys.toList() + "127.0.0.1"
    }

    fun getNodes(name: String): List<ServiceNodeEntry> {
        return serviceCache.getIfPresent(name)?.values?.toList() ?: emptyList()
    }

    private fun invalidateNodes() {
        val now = Clock.System.now()
        serviceCache.asMap().values.forEach {
            it.values.forEach { entry ->
                if (entry.status == NodeStatus.ACTIVE && (now - entry.lastActivity).inWholeSeconds > expiryTime) {
                    entry.status = NodeStatus.INACTIVE
                    application.log.info("Service ${entry.name} on ${entry.ip}:${entry.port} no longer available")
                }
            }
        }
    }
}
