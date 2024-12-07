package year2024

import utils.*
import kotlin.math.absoluteValue

// Task description:
//   https://adventofcode.com/2024/day/1

fun main() = runAoc {
    example("from task") {
        answer1(11)
        answer2(31)
        """
            3   4
            4   3
            2   5
            1   3
            3   9
            3   3
        """
    }

    solution1 { solve1(lines) }
    solution2 { solve2(lines) }
}

private fun solve1(input: List<String>): Long {
    val (xs, ys) = parse(input)
    return xs.sorted().zip(ys.sorted())
        .sumOf { (x, y) -> (x - y).absoluteValue.toLong() }
}

private fun solve2(input: List<String>): Long {
    val (xs, ys) = parse(input)
    return xs.sumOf { x -> x.toLong() * ys.count { y -> x == y } }
}

private fun parse(input: List<String>) =
    input
        .map { it.numbers().let { (a, b) -> a to b } }
        .unzip()
