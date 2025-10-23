package year2023

import utils.*

@Suppress("DEPRECATION")
fun main() = test(
    { solve(it, 1) },
    { solve(it, 5) },
)

private fun <T> expand(xs: Iterable<T>, expansion: Int, separator: T?): LList<T>? =
    (0 until expansion)
        .map { xs }
        .reduce { x, y -> if (separator != null) x + separator + y else x + y }
        .toLList()

private fun solve(input: List<String>, expansion: Int): Long {
    return input.sumOf { line ->
        val pattern = expand(line.substringBefore(' ').asIterable(), expansion, '?')
        val groups = expand(line.substringAfter(' ').numbersAsInts(), expansion, null)

        countVariants(mutableMapOf(), skipSpaces(pattern), null, groups)
    }
}

private fun <A, R> memoize(cache: MutableMap<A, R>, args: A, calcFunc: () -> R): R =
    cache.getOrPut(args) { calcFunc() }

private tailrec fun skipSpaces(pattern: LList<Char>?): LList<Char>? =
    if (pattern == null || pattern.head != '.') pattern else skipSpaces(pattern.tail)

private fun countVariants(
    cache: MutableMap<Any, Long>,
    pattern: LList<Char>?, curGroupRemaining: Int?, groups: LList<Int>?
): Long =
    memoize(cache, Triple(pattern, curGroupRemaining, groups)) {
        if (pattern == null) {
            // recursion termination case
            if (groups == null && (curGroupRemaining == null || curGroupRemaining == 0)) {
                1L
            } else {
                0L
            }
        } else if (curGroupRemaining != null) {
            // trivial recursion case, when the next character is predictable
            if (curGroupRemaining == 0) {
                // . must be the next char.
                when (pattern.head) {
                    '#' -> 0L
                    '.', '?' -> countVariants(cache, skipSpaces(pattern.tail), null, groups)
                    else -> shouldNotReachHere()
                }
            } else {
                assert(curGroupRemaining > 0)
                // # must be the next char.
                when (pattern.head) {
                    '.' -> 0L
                    '#', '?' -> countVariants(cache, pattern.tail, curGroupRemaining - 1, groups)
                    else -> shouldNotReachHere()
                }
            }
        } else {
            // non-trivial recursion case, when we have to try both values for the next character
            fun countAsHashStart() =
                if (groups == null) 0
                else countVariants(cache, pattern.tail, groups.head - 1, groups.tail)

            when (pattern.head) {
                '#' -> countAsHashStart()
                '?' -> {
                    val asHash = countAsHashStart()
                    val asSpace = countVariants(cache, skipSpaces(pattern.tail), null, groups)
                    asHash + asSpace
                }

                '.' -> shouldNotReachHere()
                else -> shouldNotReachHere()
            }
        }
    }
