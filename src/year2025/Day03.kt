package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/3

fun main() = runAoc {
    example {
        answer1(357)
        answer2(3121910778619)
        """
            987654321111111
            811111111111119
            234234234234278
            818181911112111
        """
    }

    solution {
        val len = if (isPart1) 2 else 12
        lines.sumOf { line ->
            check(line.length >= len)
            (0 until len).fold(0L to 0) { (acc, nextIdx), i ->
                val idx = (nextIdx .. line.length - len + i).maxBy { line[it] }
                acc * 10 + line[idx].digitToInt() to idx + 1
            }.first
        }
    }
}