package pl.felis.multum.domain.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import pl.felis.multum.collection.RoundRobinMap
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*
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
    var lastActivity: Instant = Clock.System.now()
) {
    fun getKey(): String = "$name@$ip:$port"
}

@Single
class ServiceService(private val application: Application) { // TODO: This name...
    private val serviceCache: Cache<String, RoundRobinMap<String, ServiceNodeEntry>> =
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
            RoundRobinMap()
        }

        service[entry.getKey()] = entry
    }

    fun heartbeat(entry: ServiceNodeEntry) {
        try {
            val service = serviceCache.getIfPresent(entry.name)!!

            service[entry.getKey()]!!.lastActivity = Clock.System.now()
        } catch (e: NullPointerException) {
            throw RuntimeException("No service ${entry.name} on ${entry.ip}:${entry.port} registered!")
        }
    }

    fun getServices(): List<String> {
        return serviceCache.asMap().keys.toList()
    }

    fun serviceExists(name: String): Boolean {
        val map = serviceCache.getIfPresent(name)
        return map?.isNotEmpty() ?: false
    }

    fun roundRobinNodes(name: String): ServiceNodeEntry {
        return serviceCache.getIfPresent(name)?.next() ?: throw RuntimeException("No registered nodes for $name")
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
