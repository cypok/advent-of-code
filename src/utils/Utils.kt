@file:Suppress("unused")

package utils

import java.math.BigInteger
import java.security.MessageDigest
import java.util.BitSet
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

fun <T> List<T>.split(separatorPredicate: (T) -> Boolean): Sequence<List<T>> = sequence {
    val remaining = this@split.iterator()
    while (remaining.hasNext()) {
        yield(buildList {
            while (remaining.hasNext()) {
                val elem = remaining.next()
                if (separatorPredicate(elem)) {
                    break
                }
                add(elem)
            }
        })
    }
}

fun <T> List<T>.split(separator: T): Sequence<List<T>> =
    split { it == separator }

fun List<String>.splitByEmptyLines(): Sequence<List<String>> =
    split { it.isEmpty() }

private val WORDS_REGEX = """\s+""".toRegex()

fun String.words(): List<String> =
    split(WORDS_REGEX).filterNot { it.isEmpty() }

// Treat '-' or '+' as a sign only when it does not immediately follow a digit to support ranges (e.g., 10-20).
private val NUMBERS_REGEX = """(?<!\d)[+-]?\d+""".toRegex()
private fun String.numbersRaw(): Sequence<MatchResult> = NUMBERS_REGEX.findAll(this)

fun String.numbers(): List<Long> =
    numbersRaw().map { it.value.toLong() }.toList()

fun String.numbersAsInts(): List<Int> =
    numbersRaw().map { it.value.toInt() }.toList()

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

/**
 * Transform
 *
 *     [a,b,c]
 *
 * to
 *
 *     [(a,a), (a, b), (a, c),
 *      (b,a), (b, b), (b, c),
 *      (c,a), (c, b), (c, c)]
 */
fun <T> Iterable<T>.cartesianSquare(): Sequence<Pair<T, T>> = sequence {
    this@cartesianSquare.forEach { a ->
        this@cartesianSquare.forEach { b ->
            yield(a to b)
        }
    }
}

fun <T> Collection<T>.permutations(): Set<List<T>> {
    if (isEmpty()) return setOf(emptyList())

    val res = mutableSetOf<List<T>>()
    val thisSet = toSet()
    for (e in this) {
        for (smallerPerm in (thisSet - e).permutations()) {
            res.add(smallerPerm + e)
        }
    }
    return res
}

fun <T> Collection<T>.cycle(): Sequence<T> =
    generateSequence { this }.flatten()

fun <T> List<T>.combinations(): List<Pair<T, T>> = buildList {
    for (i in 0 ..< this@combinations.size) {
        for (j in i+1 ..< this@combinations.size) {
            add(this@combinations[i] to this@combinations[j])
        }
    }
}

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

fun <T, S> Pair<T, S>.swap(): Pair<S, T> =
    Pair(second, first)

/** Like [single], but for two elements. */
fun <T> List<T>.pair(): Pair<T, T> =
    when (size) {
        0, 1 -> throw NoSuchElementException("List has not enough elements.")
        2 -> this[0] to this[1]
        else -> throw IllegalArgumentException("List has more than one element.")
    }

/** Split the list into the list of disjoint pairs. */
fun <T> Iterable<T>.disjointPairs(): List<Pair<T, T>> =
    windowed(2, step = 2, partialWindows = true).map { it.pair() }

/** Generate the list of windowed pairs from the given list. */
fun <T> Iterable<T>.windowedPairs(): List<Pair<T, T>> =
    windowed(2, step = 1, partialWindows = false).map { it.pair() }

operator fun <T> List<T>.component6() = get(5)

fun <T> List<T>.middle(): T = this[this.size/2]

inline fun <T> Iterator<T>.countWhile(predicate: (T) -> Boolean): Int {
    var idx = 0
    for (item in this) {
        if (!predicate(item)) {
            break
        }
        idx++
    }
    return idx
}

inline fun <T> Iterable<T>.countWhile(predicate: (T) -> Boolean): Int = iterator().countWhile(predicate)
inline fun <T> Sequence<T>.countWhile(predicate: (T) -> Boolean): Int = iterator().countWhile(predicate)

inline fun <T> List<T>.countLastWhile(predicate: (T) -> Boolean): Int {
    var idx = size - 1
    while (idx >= 0) {
        if (!predicate(get(idx))) {
            break
        }
        idx--
    }
    return size - 1 - idx
}

operator fun BitSet.contains(elem: Int): Boolean = get(elem)
operator fun BitSet.plusAssign(elem: Int): Unit = set(elem)
operator fun BitSet.minusAssign(elem: Int): Unit = clear(elem)

fun arithOpByChar(ch: Char): (Long, Long) -> Long =
    when (ch) {
        '+' -> Math::addExact
        '*' -> Math::multiplyExact
        '-' -> Math::subtractExact
        else -> error("unexpected op '$ch'")
    }

fun arithOpByChar(ch: String): (Long, Long) -> Long =
    arithOpByChar(ch.single())

fun shouldNotReachHere(): Nothing = error("should not reach here")

const val FULL_BLOCK = '█'