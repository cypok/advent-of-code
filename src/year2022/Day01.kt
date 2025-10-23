package year2022

import utils.splitByEmptyLines
import utils.test

@Suppress("DEPRECATION")
fun main() = test(
    { solve(it, 1) },
    { solve(it, 3) },
)

private fun solve(input: List<String>, topCount: Int): Long =
    input.splitByEmptyLines()
        .map { it.sumOf { it.toLong() } }
        .sortedDescending()
        .take(topCount)
        .sum()
