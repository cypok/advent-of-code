package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/13

fun main() = runAoc {
    example {
        answer1(480)
        """
            Button A: X+94, Y+34
            Button B: X+22, Y+67
            Prize: X=8400, Y=5400

            Button A: X+26, Y+66
            Button B: X+67, Y+21
            Prize: X=12748, Y=12176

            Button A: X+17, Y+86
            Button B: X+84, Y+37
            Prize: X=7870, Y=6450

            Button A: X+69, Y+23
            Button B: X+27, Y+71
            Prize: X=18641, Y=10279
        """
    }

    fun divOrNull(x: Long, y: Long): Long? =
        (x / y).takeIf { it * y == x }

    solution {
        lines.splitByEmptyLines().sumOf { machine ->
            val [buttonADesc, buttonBDesc, prizeDesc] = machine
            val [aX, aY] = buttonADesc.numbers()
            val [bX, bY] = buttonBDesc.numbers()
            val [pX, pY] = prizeDesc.numbers().map {
                if (isPart2) it + 10_000_000_000_000 else it
            }

            val det = aX * bY - aY * bX
            val aC = divOrNull(bY * pX - bX * pY, det)
            val bC = divOrNull(aX * pY - aY * pX, det)
            if (aC != null && bC != null && aC >= 0 && bC >= 0) {
                aC * 3 + bC
            } else {
                0
            }
        }
    }

}