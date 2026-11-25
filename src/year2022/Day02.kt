package year2022

import utils.*

// Task description:
//   https://adventofcode.com/2022/day/2

fun main() = runAoc {
    example {
        answer1(15)
        answer2(12)
        """
            A Y
            B X
            C Z
        """
    }
    solution {
        lines.sumOf { line ->
            val [l, r] = line.split(" ")
            val lValue = "ABC".indexOf(l.single())
            val rValue = "XYZ".indexOf(r.single())
            check(lValue >= 0 && rValue >= 0)

            val their: Int
            val yours: Int
            if (isPart1) {
                their = lValue
                yours = rValue
            } else {
                their = lValue
                yours = when (rValue) {
                    0 -> (their + 2) % 3
                    1 -> their
                    2 -> (their + 1) % 3
                    else -> shouldNotReachHere()
                }
            }

            val shapeScore = yours + 1
            val outcomeScore =
                if (their == yours) {
                    3
                } else if ((their + 1) % 3 == yours) {
                    6
                } else if (their == (yours + 1) % 3) {
                    0
                } else {
                    shouldNotReachHere()
                }
            shapeScore + outcomeScore
        }
    }
}