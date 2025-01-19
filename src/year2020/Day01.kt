package year2020

import utils.*

// Task description:
//   https://adventofcode.com/2020/day/1

fun main() = runAoc {
    example {
        answer1(514579)
        answer2(241861950)
        """
            1721
            979
            366
            299
            675
            1456
        """
    }

    fun twoSum(xs: List<Long>, sum: Long): Long? {
        val seen = mutableSetOf<Long>()
        for (x in xs) {
            if ((sum - x) in seen) {
                return x * (sum - x)
            }
            seen += x
        }
        return null
    }

    @Suppress("LocalVariableName")
    val YEAR = 2020L

    solution1 {
        val xs = lines.map { it.toLong() }
        twoSum(xs, YEAR)!!
    }

    solution2 {
        val xs = lines.map { it.toLong() }
        for (a in xs) {
            twoSum(xs, YEAR - a)
                ?.let {
                    return@solution2 it * a
                }
        }
        shouldNotReachHere()
    }
}
