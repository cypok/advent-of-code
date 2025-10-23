package year2021

import utils.*

// Task description:
//   https://adventofcode.com/2021/day/1

@Suppress("DEPRECATION")
fun main() = test(
    { solve(it, 1) },
    { solve(it, 3) },
)

private fun solve(input: List<String>, slidingCount: Int): Long =
    input
        .map { it.toInt() }
        .windowed(slidingCount)
        .map { it.sum() }
        .windowed(2)
        .count { it[0] < it[1] }
        .toLong()