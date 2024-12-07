package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/3

fun main() = runAoc {
    example {
        answer1(161)
        "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))"
    }

    example {
        answer2(48)
        "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))"
    }

    val instruction = """mul\((\d{1,3}),(\d{1,3})\)""".toRegex()

    solution1 {
        instruction.findAll(lines.joinToString()).sumOf { it.calculate() }
    }

    solution2 {
        var enabled = true
        val line = lines.joinToString()
        var sum = 0L
        for (i in 0..line.length) {
            if (line.startsWith("do()", i)) {
                enabled = true
            } else if (line.startsWith("don't()", i)) {
                enabled = false
            } else if (enabled) {
                sum += instruction.matchAt(line, i)?.calculate() ?: 0
            }
        }
        sum
    }
}

private fun MatchResult.calculate(): Long =
    this.groupValues.drop(1).productOf { it.toLong() }
