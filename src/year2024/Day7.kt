package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/7

fun main() = runAoc {
    example {
        answer1(3749L)
        answer2(11387L)
        """
            190: 10 19
            3267: 81 40 27
            83: 17 5
            156: 15 6
            7290: 6 8 6 15
            161011: 16 10 13
            192: 17 8 14
            21037: 9 7 18 13
            292: 11 6 16 20
        """
    }

    solution1 { solution(allowConcatenation = false) }
    solution2 { solution(allowConcatenation = true) }
}

private fun SolutionContext.solution(allowConcatenation: Boolean): Long =
    lines.sumOf { line ->
        val allLine = line.numbers()
        val expected = allLine.first()
        val nums = allLine.drop(1)

        fun bruteForce(curIdx: Int, curResult: Long): Boolean {
            if (curIdx == nums.size) {
                return curResult == expected
            }
            if (curResult > expected) {
                return false
            }

            val nextNum = nums[curIdx]

            return  bruteForce(curIdx + 1, curResult * nextNum) ||
                    bruteForce(curIdx + 1, curResult + nextNum) ||
                    (allowConcatenation && bruteForce(curIdx + 1, concatenate(curResult, nextNum)))
        }

        if (bruteForce(1, nums[0])) {
            expected
        } else {
            0
        }
    }

private fun concatenate(x: Long, y: Long) = "$x$y".toLong()
