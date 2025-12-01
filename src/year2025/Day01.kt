package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/1

fun main() = runAoc {
    example {
        answer1(3)
        answer2(6)
        """
            L68
            L30
            R48
            L5
            R60
            L55
            L1
            L99
            R14
            L82
        """
    }

    solution {
        sequence {
            for (line in lines) {
                val dir = if (line[0] == 'R') 1 else -1
                val clicks = line.substring(1).toInt()
                if (isPart1) {
                    yield(dir * clicks)
                } else {
                    repeat(clicks) { yield(dir) }
                }
            }
        }
            .runningFold(50, Math::addExact)
            .count { it % 100 == 0 }
    }
}