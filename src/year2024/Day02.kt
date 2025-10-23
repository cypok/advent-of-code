package year2024

import utils.*
import kotlin.math.absoluteValue

// Task description:
//   https://adventofcode.com/2024/day/2

@Suppress("DEPRECATION")
fun main() = test(
    { solve(it, false) },
    { solve(it, true) },
)

private fun solve(input: List<String>, allowCorrection: Boolean): Int =
    input.count { line ->
        val xs = line.numbers()
        return@count isSafe(xs) ||
                allowCorrection && xs.indices.any { i ->
                    val xsWithoutOne = xs.toMutableList()
                    xsWithoutOne.removeAt(i)
                    isSafe(xsWithoutOne)
                }
    }

private fun isSafe(xs: List<Long>): Boolean {
    var lastDelta: Long? = null
    for ((x, y) in xs.windowed(2)) {
        val newDelta = y - x
        if (newDelta.absoluteValue !in 1..3) {
            return false
        }
        if (lastDelta != null && lastDelta * newDelta < 0) {
            return false
        }
        lastDelta = newDelta
    }
    return true
}