package pl.felis.multum.domain.discovery

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import pl.felis.multum.collection.StatusAwareServiceNodeRoundRobin
import pl.felis.multum.common.dao.RegisterData
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

enum class NodeStatus {
    ACTIVE,
    INACTIVE
}

@Serializable
data class ServiceNodeEntryQuery(
    val name: String,
    val port: Int,
    val ip: String
) {
    fun getKey(): String = "$name@$ip:$port"
}

@Serializable
data class ServiceNodeEntry(
    val name: String,
    val port: Int,
    val ip: String,
    val prometheusMetrics: Boolean,
    var status: NodeStatus = NodeStatus.ACTIVE,
    var lastActivity: Instant = Clock.System.now()
) {
    fun getKey(): String = "$name@$ip:$port"
    fun toQuery(): ServiceNodeEntryQuery = ServiceNodeEntryQuery(name, port, ip)
}

@Single
class DiscoveryService(private val application: Application) { // TODO: This name...
    private val serviceCache: Cache<String, StatusAwareServiceNodeRoundRobin> =
        Caffeine.newBuilder().recordStats().build()
    private val expiryTask: TimerTask
    private val expiryTime: Long =
        application.environment.config.property("multum.discovery.expiryAfterSeconds").getString().toLong()
    private val removalTime: Long =
        application.environment.config.property("multum.discovery.removeAfterSeconds").getString().toLong()

    init {
        expiryTask = Timer("serviceNodeExpiry").schedule(0, TimeUnit.SECONDS.toMillis(1)) {
            invalidateNodes()
        }
    }

    fun register(entry: ServiceNodeEntryQuery, data: RegisterData): ServiceNodeEntry {
        val service = serviceCache.get(entry.name) {
            StatusAwareServiceNodeRoundRobin()
        }

        val node = ServiceNodeEntry(entry.name, entry.port, entry.ip, data.prometheusMetrics)
        service[entry.getKey()] = node

        return node
    }

    fun heartbeat(entry: ServiceNodeEntryQuery) {
        try {
            val service = serviceCache.getIfPresent(entry.name)!!

            service[entry.getKey()]!!.lastActivity = Clock.System.now()
        } catch (e: NullPointerException) {
            throw RuntimeException("No service ${entry.getKey()} registered!")
        }
    }

    fun getServices(): List<String> {
        return serviceCache.asMap().keys.toList()
    }

    fun getServiceMap(): ConcurrentMap<String, StatusAwareServiceNodeRoundRobin>? {
        return serviceCache.asMap()
    }

    fun serviceExists(name: String): Boolean {
        val map = serviceCache.getIfPresent(name)
        return map?.isNotEmpty() ?: false
    }

    suspend fun roundRobinNodes(name: String): ServiceNodeEntry {
        return serviceCache.getIfPresent(name)?.next() ?: throw RuntimeException("No registered nodes for $name")
    }

    fun getNodes(name: String): List<ServiceNodeEntry> {
        return serviceCache.getIfPresent(name)?.values?.toList() ?: emptyList()
    }

    fun setNodeAsInactive(node: ServiceNodeEntry, throwable: Throwable? = null) {
        node.status = NodeStatus.INACTIVE
        application.log.error("Service ${node.getKey()} no longer available", throwable)
    }

    private fun invalidateNodes() {
        val now = Clock.System.now()
        serviceCache.asMap().values.forEach {
            it.values.forEach { entry ->
                if (entry.status == NodeStatus.ACTIVE && (now - entry.lastActivity).inWholeSeconds > expiryTime) {
                    setNodeAsInactive(entry)
                }
                if (entry.status == NodeStatus.INACTIVE && (now - entry.lastActivity).inWholeSeconds > removalTime) {
                    application.log.info("Removing node ${entry.getKey()} due to inactivity")
                    remove(entry.toQuery())
                }
            }
        }
    }

    fun remove(node: ServiceNodeEntryQuery) {
        try {
            val service = serviceCache.getIfPresent(node.name)!!

            service.remove(node.getKey())
        } catch (e: NullPointerException) {
            throw RuntimeException("No service ${node.name} on ${node.ip}:${node.port} registered!")
        }
    }
}
