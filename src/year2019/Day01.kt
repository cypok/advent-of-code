package year2019

import utils.*

// Task description:
//   https://adventofcode.com/2019/day/1

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Long =
    input.sumOf { fuelFor(it.toLong()) }

private fun solve2(input: List<String>): Long =
    input.sumOf {
        generateSequence(it.toLong()) { fuelFor(it) }
            .drop(1)
            .takeWhile { it > 0 }
            .sum()
    }

private fun fuelFor(mass: Long): Long =
    (mass / 3 - 2).coerceAtLeast(0)