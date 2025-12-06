package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/6

fun main() = runAoc {
    example {
        answer1(4277556)
        answer2(3263827)
        """
            123 328  51 64 
             45 64  387 23 
              6 98  215 314
            *   +   *   +  
        """
    }

    solution1 {
        val wordLines = lines.map { it.words() }
        Array2D.from(wordLines).cols.sumOf { col ->
            val args = col.dropLast(1).map { it.toLong() }
            val op = arithOpByChar(col.last())
            args.reduce(op)
        }
    }

    solution2 {
        var totalRes = 0L
        var curCol = 0
        var curOpAndRes: Pair<(Long, Long) -> Long, Long>? = null
        while (curCol < map.width + 1) {
            val argStr = if (curCol < map.width) map.col(curCol).joinToString("") else ""
            if (argStr.isBlank()) {
                totalRes += curOpAndRes!!.second
                curOpAndRes = null
            } else {
                val opCh = map[map.height - 1][curCol]
                val arg = argStr.dropLast(1).trim().toLong()
                if (curOpAndRes == null) {
                    curOpAndRes = arithOpByChar(map[map.height - 1][curCol]) to arg
                } else {
                    check(opCh == ' ')
                    val (op, res) = curOpAndRes
                    curOpAndRes = op to op(res, arg)
                }
            }
            curCol++
        }
        totalRes
    }
}