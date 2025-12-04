package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/4

fun main() = runAoc {
    measureRunTime()

    example {
        answer1(13)
        answer2(43)
        """
            ..@@.@@@@.
            @@@.@.@.@@
            @@@@@.@.@@
            @.@@@@..@.
            @@.@@@@.@@
            .@@@@@@@.@
            .@.@.@.@@@
            @.@@@.@@@@
            .@@@@@@@@.
            @.@.@@@.@.
        """
    }

    solution {
        val adjs = listOf(-1, 0, 1).cartesianSquare()
            .filter { (di, dj) -> di != 0 || dj != 0 }.toList()
        assert(adjs.size == 8)

        val ROLL = '@'

        fun isRemovable(p: Point) =
            (map[p] == ROLL && adjs.count { (di, dj) -> map.getOrNull(p.i + di, p.j + dj) == ROLL } < 4)

        if (isPart1) {
            map.indices.count { isRemovable(it) }

        } else {
            val workList = WorkList(map.indices)
            workList.asSequence().count { p ->
                isRemovable(p).also {
                    if (it) {
                        map[p] = '.'
                        adjs.forEach { (di, dj) ->
                            val ap = (p.i + di) x (p.j + dj)
                            if (map.getOrNull(ap) == ROLL) {
                                workList += ap
                            }
                        }
                    }
                }
            }
        }
    }
}
