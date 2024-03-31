package commandmaster.utils

interface BiMap<K,V>: Map<K,V> {

    val keyToVal: Map<K,V>
    val valToKey: Map<V,K>

    fun getKey(value: V): K?

}

class MutableBiMap<K,V>: BiMap<K,V>, MutableMap<K,V> {

    private val map= mutableMapOf<K,V>()
    private val reverse= mutableMapOf<V,K>()

    override val entries = map.entries

    override val keys = map.keys

    override val size = map.size

    override val values = map.values

    override val keyToVal get() = map
    override val valToKey get() = reverse

    override fun clear() {
        map.clear()
        reverse.clear()
    }

    override fun isEmpty() = map.isEmpty()

    override fun remove(key: K): V? {
        val removed=map.remove(key)
        if(removed!=null)reverse.remove(removed)
        return removed
    }

    override fun putAll(from: Map<out K, V>) {
        for((k,v) in from){
            put(k,v)
        }
    }

    override fun put(key: K, value: V): V? {
        val ret=map.put(key,value)
        if(ret!=null)reverse.remove(ret)
        reverse.put(value,key)
        return ret
    }

    override fun get(key: K) = map[key]

    override fun getKey(value: V) = reverse[value]

    override fun containsValue(value: V) = map.containsValue(value)

    override fun containsKey(key: K) = map.containsKey(key)

}

fun <K,V> mutableBiMapOf(vararg pairs: Pair<K,V>): MutableBiMap<K,V> {
    val map= MutableBiMap<K,V>()
    for((k,v) in pairs){
        map.put(k,v)
    }
    return map
}

fun <K,V> biMapOf(vararg pairs: Pair<K,V>) = mutableBiMapOf(*pairs) as BiMap<K,V>