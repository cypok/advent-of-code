package utils

class LList<T>(val head: T, val tail: LList<T>?) {
    // For debugging purposes.
    override fun toString(): String =
        (this as LList<T>?).toString()
}

tailrec operator fun <T> LList<T>?.contains(element: T): Boolean =
    this != null && (this.head == element || this.tail.contains(element))

fun <T> LList<T>?.sumOf(selector: (T) -> Long): Long {
    tailrec fun <T> LList<T>?.iter(selector: (T) -> Long, acc: Long): Long =
        if (this == null) acc
        else tail.iter(selector, Math.addExact(acc, selector(head)))

    return iter(selector, 0)
}

fun <T> LList<T>?.toList(): List<T> =
    when (this) {
        null ->
            emptyList()
        else ->
            buildList {
                var cur: LList<T>? = this@toList
                while (cur != null) {
                    add(cur.head)
                    cur = cur.tail
                }
            }
    }

fun <T> LList<T>?.toString(): String =
    this.toList().toString()

fun <T> Iterable<T>.toLList(): LList<T>? =
    this.iterator().toLList()

fun <T> Iterator<T>.toLList(): LList<T>? =
    if (this.hasNext()) LList(this.next(), this.toLList()) else null

