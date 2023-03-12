package pl.felis.multum.collection

import java.util.concurrent.ConcurrentHashMap

open class RoundRobinMap<K : Any, V : Any>() : ConcurrentHashMap<K, V>() {
    protected var currentIndex = 0
    private var currentKeys = keys.toList()

    open fun next(): V? {
        val entry = this[currentKeys[currentIndex]]
        currentIndex += 1
        if (currentIndex >= currentKeys.size) currentIndex = 0

        return entry
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

    override fun remove(key: K): V? {
        val v = super.remove(key)

        currentKeys = keys.toList()

        return v
    }

    override fun remove(key: K, value: V): Boolean {
        val result = super.remove(key, value)

        currentKeys = keys.toList()

        return result
    }
}
