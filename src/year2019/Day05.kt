package year2019

import utils.*

// Task description:
//   https://adventofcode.com/2019/day/5

fun main() = runAoc {
    solution {
        val pc = intCodeComputer()
        if (isPart1) {
            val result = pc.run(1)
            check(result.dropLast(1).all { it == 0L })
            result.last()
        } else {
            pc.run(5).single()
        }
    }
}