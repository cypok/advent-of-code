package utils

/**
 * It allows iteration over the elements and adding of new elements to the end at the same time.
 * New elements are not added if they are already in the work list.
 */
class WorkList<E> : Iterator<E> {
    private val data = LinkedHashSet<E>()

    constructor(vararg elements: E) { data += elements }
    constructor(elements: Iterable<E>) { data += elements }
    constructor(elements: Sequence<E>) { data += elements }

    override fun next(): E = data.removeFirst()
    override fun hasNext(): Boolean = data.isNotEmpty()

    operator fun plusAssign(e: E) { data += e }

    fun asSequence(): Sequence<E> = Sequence { this }
}
