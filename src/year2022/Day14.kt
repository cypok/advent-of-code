package year2022

import utils.*
import kotlin.math.sign

// Task description:
//   https://adventofcode.com/2022/day/14

fun main() = runAoc {
    example {
        answer1(24)
        answer2(93)
        """
            498,4 -> 498,6 -> 496,6
            503,4 -> 502,4 -> 502,9 -> 494,9
        """
    }

    @Suppress("LocalVariableName")
    solution {
        val EMPTY = ' '
        val ROCK = FULL_BLOCK
        val SAND = 'o'

        val rockPaths = lines.map { it.numbersAsInts(). disjointPairs() }

        val (maxX, maxY) = rockPaths.flatten().unzip().map { it.max() }
        val height = maxY + 2 + 1
        check(maxX > maxY) // hope that no sand is going into the negative x part
        val width = maxX + maxY + 1
        val cave = Array2D.ofChars(height, width, EMPTY)
        for (path in rockPaths) {
            for ((src, dst) in path.windowedPairs()) {
                val (dx, dy) = (src.toList() zip dst.toList()).map { (it.second - it.first).sign }
                check((dx != 0) != (dy != 0))
                var x = src.first
                var y = src.second
                while (true) {
                    cave[y, x] = ROCK
                    if (x == dst.first && y == dst.second) break
                    x += dx
                    y += dy
                }
            }
        }

        fun isEmpty(x: Int, y: Int) =
            cave.getOrNull(y, x) == EMPTY && (!isPart2 || y != maxY + 2)

        val moves = listOf(0 to 1, -1 to 1, 1 to 1, 0 to 0)

        var sandUnitCount = 0
        overall@ while (true) {
            var x = 500
            var y = -1
            while (true) {
                if (isPart1 && y == maxY) break@overall
                val (dx, dy) = moves.first { isEmpty(x + it.first, y + it.second) }
                if (dy == 0 && dx == 0) {
                    sandUnitCount++
                    if (isPart2 && y == 0) break@overall
                    cave[y, x] = SAND
                    break
                }

                x += dx
                y += dy
            }
        }

        if (maxY < 20) {
            printExtra(cave.toAsciiArt())
        }
        sandUnitCount
    }
}