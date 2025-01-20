package utils

class MultiSet<E>() {
    private val data = mutableMapOf<E, Long>()

    constructor(elems: Collection<E>) : this() {
        elems.forEach { add(it ) }
    }

    operator fun get(elem: E): Long =
        data[elem] ?: 0

    fun add(elem: E, count: Long = 1L) {
        data[elem] = Math.addExact(get(elem), count)
    }

    data class ElementGrouped<E>(val elem: E, val count: Long)

    val grouped: Sequence<ElementGrouped<E>>
        get() = data.entries.asSequence().map { (e, c) -> ElementGrouped(e, c) }
}

fun <E> multiSetOf(vararg elems: E): MultiSet<E> = MultiSet(elems.asList())
