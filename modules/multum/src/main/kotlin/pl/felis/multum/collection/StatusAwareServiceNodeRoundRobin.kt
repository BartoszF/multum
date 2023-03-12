package pl.felis.multum.collection

import pl.felis.multum.domain.service.NodeStatus
import pl.felis.multum.domain.service.ServiceNodeEntry

class StatusAwareServiceNodeRoundRobin : RoundRobinMap<String, ServiceNodeEntry>() {

    override fun next(): ServiceNodeEntry? {
        return next(false)
    }

    fun next(nested: Boolean): ServiceNodeEntry? {
        val nextActiveEntryIndexed = entries.mapIndexed { index, entry -> Pair(index, entry) }
            .filter { it.second.value.status == NodeStatus.ACTIVE }.firstOrNull { it.first > currentIndex }

        if (!nested && nextActiveEntryIndexed == null) {
            currentIndex = -1 // NOTE: We are checking for entry that index is greater than currentIndex.
            //       Hence, we are setting it to -1, so next loop will check for index=0 as well.
            return next(true)
        }

        currentIndex = nextActiveEntryIndexed?.first ?: 0

        return nextActiveEntryIndexed?.second?.value
    }
}
