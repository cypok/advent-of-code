package year2024

import utils.numbers
import utils.test
import utils.words
import kotlin.math.absoluteValue

// Task description:
//   https://adventofcode.com/2024/day/1

fun main() = test(
    ::solve1,
    ::solve2,
    """
        3   4
        4   3
        2   5
        1   3
        3   9
        3   3
    """.trimIndent()
)

private fun solve1(input: List<String>): Long {
    val (xs, ys) = parse(input)
    return xs.sorted().zip(ys.sorted())
        .sumOf { (x, y) -> (x - y).absoluteValue.toLong() }
}

private fun solve2(input: List<String>): Long {
    val (xs, ys) = parse(input)
    return xs.sumOf { x -> ys.count { y -> (x - y).absoluteValue <= 2 }.toLong() }
}

private fun parse(input: List<String>) =
    input
        .map { it.words().let { (a, b) -> a to b } }
        .unzip()
