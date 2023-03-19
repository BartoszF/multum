package pl.felis.multum.collection

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.felis.multum.domain.discovery.NodeStatus
import pl.felis.multum.domain.discovery.ServiceNodeEntry

class StatusAwareServiceNodeRoundRobin : RoundRobinMap<String, ServiceNodeEntry>() {

    private val lock = Mutex()

    override suspend fun next(): ServiceNodeEntry? {
        return lock.withLock {
            next(false)
        }
    }

    fun next(nested: Boolean): ServiceNodeEntry? {
        val nextActiveEntryIndexed = entries.mapIndexed { index, entry -> Pair(index, entry) }
            .filter { it.second.value.status == NodeStatus.ACTIVE }.firstOrNull { it.first > currentIndex.get() }

        if (!nested && nextActiveEntryIndexed == null) {
            currentIndex.set(-1) // NOTE: We are checking for entry that index is greater than currentIndex.
            //       Hence, we are setting it to -1, so next loop will check for index=0 as well.
            return next(true)
        }

        currentIndex.set(nextActiveEntryIndexed?.first ?: 0)

        return nextActiveEntryIndexed?.second?.value
    }
}
