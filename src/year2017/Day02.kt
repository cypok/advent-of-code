package year2017

import utils.*

// Task description:
//   https://adventofcode.com/2017/day/2

fun main() = runAoc {
    example {
        answer1(18)
        """
            5 1 9 5
            7 5 3
            2 4 6 8
        """
    }

    solution {
        lines.sumOf { line ->
            val nums = line.numbersAsInts()
            if (isPart1) {
                nums.max() - nums.min()
            } else {
                listOf(nums, nums).cartesianProduct()
                    .filter { (a, b) -> a > b && a % b == 0 }
                    .map { (a, b) -> a / b }
                    .single()
            }
        }
    }
}