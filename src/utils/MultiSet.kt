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

    fun addAll(elems: Collection<E>) {
        elems.forEach { add(it) }
    }

    val uniqueCount: Int
        get() = data.size

    data class ElementGrouped<E>(val elem: E, val count: Long)

    val grouped: Sequence<ElementGrouped<E>>
        get() = data.entries.asSequence().map { (e, c) -> ElementGrouped(e, c) }

    fun <R> flatMap(transform: (E) -> Iterable<R>): MultiSet<R> {
        return MultiSet<R>().also { res ->
            data.forEach { (e, c) ->
                transform(e).forEach { r ->
                    res.add(r, c)
                }
            }
        }
    }
}

fun <E> multiSetOf(vararg elems: E): MultiSet<E> = MultiSet(elems.asList())
