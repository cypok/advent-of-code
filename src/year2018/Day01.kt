package year2018

import utils.*

// Task description:
//   https://adventofcode.com/2018/day/1

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Long =
    input.sumOf { it.toLong() }

private fun solve2(input: List<String>): Long {
    var cur = 0L
    val visited = mutableSetOf<Long>()
    for (line in input.cycle()) {
        cur += line.toLong()
        if (!visited.add(cur)) {
            return cur
        }
    }
    shouldNotReachHere()
}
