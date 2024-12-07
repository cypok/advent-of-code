package utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min

/**
 * Converts string to md5 hash.
 */
@Suppress("unused")
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

// Inspired by Iterable.sumOf()
inline fun <T> Iterable<T>.productOf(selector: (T) -> Long): Long =
    fold(1) { acc, x -> acc * selector(x) }

fun <T> List<T>.split(separator: T): Sequence<List<T>> = sequence {
    val remaining = this@split.iterator()
    while (remaining.hasNext()) {
        yield(buildList {
            while (remaining.hasNext()) {
                val elem = remaining.next()
                if (elem == separator) {
                    break
                }
                add(elem)
            }
        })
    }
}

fun List<String>.splitByEmptyLines(): Sequence<List<String>> =
    split("")

fun String.words(): List<String> =
    split("""\s+""".toRegex())

fun String.numbers(): List<Long> =
    split("""[^0-9+-]+""".toRegex()).filterNot { it.isEmpty() }.map { it.toLong() }

fun String.numbersAsInts(): List<Int> =
    numbers().map { it.toIntExact() }

/**
 * Transform
 *
 *     [[a,b], [c], [d,e]]
 *
 * to
 *
 *     [[a,c,d], [a,c,e], [b,c,d], [b,c,e]]
 */
fun <T> Collection<Iterable<T>>.cartesianProduct(): List<List<T>> {
    if (isEmpty()) return listOf(emptyList())
    val tails = drop(1).cartesianProduct()
    return first().flatMap { head -> tails.map { tail -> listOf(head) + tail } }
}

fun <T> Collection<T>.cycle(): Sequence<T> =
    generateSequence { this }.flatten()

fun gcd(x: Long, y: Long): Long {
    var a = max(x, y)
    var b = min(x, y)
    while (b > 0L) {
        val rem = a % b
        a = b
        b = rem
    }
    return a
}

fun lcm(x: Long, y: Long) = x / gcd(x, y) * y

fun Long.toIntExact() = Math.toIntExact(this)

inline fun <T, R> Pair<T, T>.map(transform: (T) -> R): Pair<R, R> =
    Pair(transform(first), transform(second))

operator fun <T> List<T>.component6() = get(5)

fun <T> List<T>.middle(): T = this[this.size/2]

fun shouldNotReachHere(): Nothing = error("should not reach here")