package year2024

import utils.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Task description:
//   https://adventofcode.com/2024/day/20

fun main() = runAoc {
    example {
        answer1(8, 12)
        answer2(41, 70)
        """
            ###############
            #...#...#.....#
            #.#.#.#.#.###.#
            #S#...#.#.#...#
            #######.#.#.###
            #######.#.#...#
            #######.#.###.#
            ###..E#...#...#
            ###.#######.###
            #...###...#...#
            #.#####.#.###.#
            #.#...#.#.#...#
            #.#.#.#.#.#.###
            #...#...#...###
            ###############
        """
    }
    solution {
        val timesMap = Array2D.ofInts(map.height, map.width, -1)
        val start = map.find('S')
        val finish = map.find('E')
        timesMap[start] = 0

        fun traverse(recordTime: Boolean, onStepAction: (Point) -> Unit) {
            var lastPos = start
            var curPos = start
            while (curPos != finish) {
                onStepAction(curPos)

                val nextPos = Dir.entries.map { curPos.moveInDir(it) }.first { it != lastPos && map[it] != '#' }
                lastPos = curPos
                curPos = nextPos

                if (recordTime) {
                    timesMap[curPos] = timesMap[lastPos] + 1
                }
            }
        }

        val minCheatSavedTime = exampleParam as? Int ?: 100
        val maxCheatDistance = if (isPart1) 2 else 20
        val cheats = mutableListOf<Pair<Point, Point>>()

        fun collectCheats(startPos: Point) {
            val startTime = timesMap[startPos]
            for (i in max(0, startPos.i - maxCheatDistance)..min(map.height - 1, startPos.i + maxCheatDistance)) {
                val remainingDistance = maxCheatDistance - abs(i - startPos.i)
                for (j in max(0, startPos.j - remainingDistance) .. min(map.width - 1, startPos.j + remainingDistance)) {
                    val endTime = timesMap[i, j]
                    if (endTime != -1) {
                        val distance = abs(i - startPos.i) + abs(j - startPos.j)
                        assert(distance <= maxCheatDistance)
                        if (endTime >= startTime + distance + minCheatSavedTime) {
                            cheats += startPos to (i x j)
                        }
                    }
                }
            }
        }

        traverse(recordTime = true) { /* nop */ }
        traverse(recordTime = false) { collectCheats(it) }

        cheats.size
    }
}