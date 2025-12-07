package year2025

import utils.*
import utils.Dir.*

// Task description:
//   https://adventofcode.com/2025/day/7

fun main() = runAoc {
    example {
        answer1(21)
        answer2(40)
        """
            .......S.......
            ...............
            .......^.......
            ...............
            ......^.^......
            ...............
            .....^.^.^.....
            ...............
            ....^.^...^....
            ...............
            ...^.^...^.^...
            ...............
            ..^...^.....^..
            ...............
            .^.^.^.^.^...^.
            ...............
        """
    }

    solution {
        var splitsCount = 0

        val start = map.find('S')
        var nextRow = start.row + 1
        var curBeams = multiSetOf(start.col)
        while (nextRow < map.height) {
            curBeams = curBeams.flatMap { beamCol ->
                when (val ch = map[nextRow, beamCol]) {
                    '.' -> listOf(beamCol)
                    '^' -> listOf(beamCol - 1, beamCol + 1).also { splitsCount++ }
                    else -> error(ch)
                }
            }
            nextRow++
        }

        if (isPart1) splitsCount
        else curBeams.grouped.sumOf { it.count }
    }

}