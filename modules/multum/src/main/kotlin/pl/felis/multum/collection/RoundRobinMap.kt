package pl.felis.multum.collection

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

open class RoundRobinMap<K : Any, V : Any>() : ConcurrentHashMap<K, V>() {
    protected var currentIndex = AtomicInteger(0)
    private var currentKeys = keys.toList()

    open suspend fun next(): V? {
        val entry = this[currentKeys[currentIndex.get()]]
        val i = currentIndex.addAndGet(1)
        if (i >= currentKeys.size) currentIndex.set(0)

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
