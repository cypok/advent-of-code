package year2015

import utils.*

// Task description:
//   https://adventofcode.com/2015/day/1

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Int =
    input[0].count { it == '(' } - input[0].count { it == ')' }

private fun solve2(input: List<String>): Int {
    var level = 0
    for ((i, c) in input[0].withIndex()) {
        when (c) {
            '(' -> level += 1
            ')' -> level -= 1
        }
        if (level < 0) {
            return i + 1
        }
    }
    shouldNotReachHere()
}

