package year2019

import utils.*

// Task description:
//   https://adventofcode.com/2019/day/9

fun main() = runAoc {
    solution {
        intCodeComputer().run(if (isPart1) 1 else 2).single()
    }
}