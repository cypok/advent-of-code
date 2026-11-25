package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/11

fun main() = runAoc {
    example {
        answer1(55312)
        "125 17"
    }

    solution {
        var stones = MultiSet(lines.first().numbers())
        repeat(if (isPart1) 25 else 75) {
            val newStones = MultiSet<Long>()
            for ([stone, count] in stones.grouped) {
                fun add(ns: Long) =
                    newStones.add(ns, count)

                if (stone == 0L) {
                    add(1)
                } else {
                    val digits = stone.toString()
                    val digitsCount = digits.length
                    if (digitsCount % 2 == 0) {
                        add(digits.substring(0, digitsCount / 2).toLong())
                        add(digits.substring(digitsCount / 2).toLong())
                    } else {
                        add(stone * 2024)
                    }
                }
            }
            stones = newStones
        }

        stones.grouped.sumOf { it.count }
    }
}