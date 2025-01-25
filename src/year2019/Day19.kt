package year2019

import utils.Array2D
import utils.runAoc
import utils.toAsciiArt
import utils.toIntExact

// Task description:
//   https://adventofcode.com/2019/day/19

fun main() = runAoc {
    measureRunTime()

    solution {
        fun isPulledAt(x: Int, y: Int): Boolean {
            val s = intCodeComputer().run(x.toLong(), y.toLong()).single().toIntExact()
            return when (s) {
                0 -> false
                1 -> true
                else -> error(s)
            }
        }

        if (isPart1) {
            val map = Array2D.ofChars(50, 50) { y, x ->
                if (isPulledAt(x, y)) '#' else '.'
            }
            printExtra(map.toAsciiArt())
            map.count { it == '#' }

        } else {
            val shipSize = 100

            var bottomLeftX = 0
            var bottomLeftY = shipSize
            while (true) {
                while (!isPulledAt(bottomLeftX, bottomLeftY)) {
                    bottomLeftX++
                }
                val topRightX = bottomLeftX + shipSize - 1
                val topRightY = bottomLeftY - shipSize + 1
                if (isPulledAt(topRightX, topRightY)) {
                    return@solution bottomLeftX * 10_000 + topRightY
                }
                bottomLeftY++
            }
        }
    }
}