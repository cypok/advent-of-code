package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/14

fun main() = runAoc {
    example {
        answer1(12)
        """
            p=0,4 v=3,-3
            p=6,3 v=-1,-3
            p=10,3 v=-1,2
            p=2,0 v=2,-1
            p=0,0 v=1,3
            p=3,0 v=-2,-2
            p=7,6 v=-1,-3
            p=3,0 v=-1,-2
            p=9,3 v=2,3
            p=7,3 v=-1,2
            p=2,4 v=2,-3
            p=9,5 v=-3,-3
        """
    }

    class Robot(var px: Int, var py: Int, val vx: Int, val vy: Int)

    solution {
        val robots = lines.map {
            val (px, py, vx, vy) = it.numbersAsInts()
            Robot(px, py, vx, vy)
        }
        val width = 1 + robots.maxOf { it.px }
        val height = 1 + robots.maxOf { it.py }

        repeat(if (isPart1) 100 else 999_999_999) { i ->
            if (isPart2 &&
                robots.map { it.px x it.py }.toSet().size == robots.size
            ) {
                printExtra(run {
                    val canvas = Array2D.ofChars(height, width, ' ')
                    robots.forEach { canvas[it.py][it.px] = '*' }
                    canvas.toAsciiArt()
                })
                return@solution i
            }
            for (r in robots) {
                r.px = (r.px + r.vx).mod(width)
                r.py = (r.py + r.vy).mod(height)
            }
        }

        assert(isPart1)
        var qs = mutableListOf(0, 0, 0, 0)
        for (r in robots) {
            if (r.px != width / 2 && r.py != height / 2) {
                val qx = r.px / (1 + width / 2)
                val qy = r.py / (1 + height / 2)
                qs[qy * 2 + qx]++
            }
        }
        qs.productOf { it.toLong() }
    }
}