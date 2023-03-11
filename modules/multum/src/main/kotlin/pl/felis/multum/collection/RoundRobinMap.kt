package pl.felis.multum.collection

import java.util.concurrent.ConcurrentHashMap

class RoundRobinMap<K : Any, V : Any>() : ConcurrentHashMap<K, V>() {
    private var currentIndex = 0
    private var currentKeys = keys.toList()

    fun next(): V {
        val entry = this[currentKeys[currentIndex]]
        currentIndex += 1
        if (currentIndex >= currentKeys.size) currentIndex = 0

        return entry!!
    }

    override fun put(key: K, value: V): V? {
        val v = super.put(key, value)

        currentKeys = keys.toList()

        return v
    }

    override fun putAll(from: Map<out K, V>) {
        super.putAll(from)

        currentKeys = keys.toList()
    }

    override fun putIfAbsent(key: K, value: V): V? {
        val v = super.putIfAbsent(key, value)

        currentKeys = keys.toList()

        return v
    }
}
