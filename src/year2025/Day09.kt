package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/9

fun main() = runAoc {
    example {
        answer1(50)
        answer2(24)
        """
            7,1
            11,1
            11,7
            9,7
            9,5
            2,5
            2,3
            7,3
        """
    }

    solution1 {
        val points = lines.map {
            it.numbersAsInts().let { (r, c) -> r x c }
        }
        points.cartesianSquare()
            .filter { (a, b) -> a.i <= b.i && a.j <= b.j }
            .maxOf { (a, b) ->
                1L * (b.i - a.i + 1) * (b.j - a.j + 1)
            }
    }

    solution2 {
        wrongAnswer
    }
}