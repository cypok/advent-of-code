package year2020

import utils.*

// Task description:
//   https://adventofcode.com/2020/day/1

fun main() = test(
    ::solve1,
    ::solve2,
)

private val year = 2020L

private fun solve1(input: List<String>): Long {
    val xs = mutableSetOf<Long>()
    input
        .map { it.toLong() }
        .forEach {
            if ((year - it) in xs) {
                return it * (year - it)
            }
            xs += it
        }
    shouldNotReachHere()
}

private fun solve2(input: List<String>): Long {
    val xs = input.map { it.toLong() }
    for (a in xs) {
        for (b in xs) {
            for (c in xs) {
                if (a + b + c == year) {
                    return a * b * c
                }
            }
        }
    }
    shouldNotReachHere()
}
