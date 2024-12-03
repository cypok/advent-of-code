package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/3

fun main() = test(
    ::solve1,
    ::solve2,
    """
        xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))
    """.trimIndent(),
    """
        xmul(2,4mul(3,7)
    """.trimIndent(),
    """
        xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))
    """.trimIndent(),
)

private val INSTRUCTION = """mul\((\d{1,3}),(\d{1,3})\)""".toRegex()

private fun solve1(input: List<String>): Long =
    INSTRUCTION.findAll(input.joinToString()).sumOf { it.calculate() }

private fun solve2(input: List<String>): Long {
    var enabled = true
    val line = input.joinToString()
    var sum = 0L
    for (i in 0..line.length) {
        if (line.startsWith("do()", i)) {
            enabled = true
        } else if (line.startsWith("don't()", i)) {
            enabled = false
        } else if (enabled) {
            sum += INSTRUCTION.matchAt(line, i)?.calculate() ?: 0
        }
    }
    return sum
}

private fun MatchResult.calculate(): Long =
    this.groupValues.drop(1).productOf { it.toLong() }