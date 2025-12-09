package year2025

import utils.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

// Task description:
//   https://adventofcode.com/2025/day/9

fun main() = runAoc {
    example {
        answer1(50)
        answer2(24)
        """
            7,1
            11,1
            11,7
            9,7
            9,5
            2,5
            2,3
            7,3
        """
    }
    example {
        // https://www.reddit.com/r/adventofcode/comments/1pi5rqn/2025_day_9_part_2_check_your_solution_with_this/
        answer2(30)
        """
            1,0
            3,0
            3,6
            16,6
            16,0
            18,0
            18,9
            13,9
            13,7
            6,7
            6,9
            1,9
        """
    }
    example {
        // https://www.reddit.com/r/adventofcode/comments/1pi4fos/comment/nt3nabk/
        answer2(18)
        """
            1,1
            3,1
            3,2
            6,2
            6,0
            8,0
            8,3
            9,3
            9,4
            4,4
            4,6
            7,6
            7,8
            2,8
            2,5
            1,5
        """
    }

    fun SolutionContext.parsePoints() = lines.map { it.numbers().let { (x, y) -> P(x, y) } }

    solution1 {
        parsePoints().combinations().maxOf { (a, b) -> area(a, b) }
    }

    solution2 {
        val points = parsePoints()

        // Remap coordinates.
        fun remap(coordSelector: (P) -> Long): Triple<Int, (Int) -> Long, (Long) -> Int> {
            val idxToCoord = points.flatMap {
                // Neighbor borders should not be joined, so we add one-width margins.
                val coord = coordSelector(it)
                listOf(coord - 1, coord, coord + 1)
            }.sorted().distinct()
            val coordToIdx = idxToCoord.withIndex().associate { it.value to it.index }
            return Triple(idxToCoord.size, idxToCoord::get, coordToIdx::getValue)
        }

        // TODO: performance, convert points to get rid of numerous conversion to indices?
        val (height, idxToY, yToIdx) = remap { it.y }
        val (width, idxToX, xToIdx) = remap { it.x }

        val map = Array2D.ofChars(height, width, '.')

        // Draw the borders and corners.
        run {
            fun setBorder(yIdx: Int, xIdx: Int, ch: Char) {
                check(map[yIdx, xIdx] == '.')
                map[yIdx, xIdx] = ch
            }

            for ((a, b) in (points.asSequence() + points.first()).windowedPairs()) {
                val aYIdx = yToIdx(a.y)
                val aXIdx = xToIdx(a.x)

                if (a.y == b.y) {
                    val yIdx = aYIdx
                    for (xIdx in xToIdx(min(a.x, b.x)) + 1..xToIdx(max(a.x, b.x)) - 1) {
                        setBorder(yIdx, xIdx, '-')
                    }
                } else {
                    check(a.x == b.x)
                    val xIdx = aXIdx
                    for (yIdx in yToIdx(min(a.y, b.y)) + 1..yToIdx(max(a.y, b.y)) - 1) {
                        setBorder(yIdx, xIdx, '|')
                    }
                }
            }

            for ((a, b, c) in (points.asSequence() + points.take(2)).windowed(3)) {
                check(a.y == b.y || b.y == c.y)
                check(a.x == b.x || b.x == c.x)
                val dY = (if (a.y == b.y) c.y - b.y else a.y - b.y).sign
                val dX = (if (a.x == b.x) c.x - b.x else a.x - b.x).sign
                val corner = when (dY to dX) {
                    (-1) to (-1) -> 'J'
                    (-1) to (+1) -> 'L'
                    (+1) to (-1) -> '7'
                    (+1) to (+1) -> 'F'
                    else -> error(dY to dX)
                }
                val bYIdx = yToIdx(b.y)
                val bXIdx = xToIdx(b.x)
                setBorder(bYIdx, bXIdx, corner)
            }
        }

        // Fill the area.
        run {
            // TODO: performance, should we just fill it?
            for (yIdx in 0..<height) {
                var state = '.'
                for (xIdx in 0..<width) {
                    val ch = map[yIdx, xIdx]
                    state = when (state + "" + ch) {
                        ".." -> '.'
                        ".|" -> 'X'
                        "X|" -> '.'
                        "X." -> 'X'.also { map[yIdx, xIdx] = 'X' }

                        // upper border
                        ".F" -> 'u'
                        "XL" -> 'u'
                        "u-" -> 'u'
                        "uJ" -> 'X'
                        "u7" -> '.'

                        // lower border
                        ".L" -> 'l'
                        "XF" -> 'l'
                        "l-" -> 'l'
                        "lJ" -> '.'
                        "l7" -> 'X'

                        else -> error(state to ch)
                    }
                }
                assert(state == '.')
            }

            if (width < 100) printExtra(map.toAsciiArt(' '))
        }

        points.combinations()
            .maxOf { (a, b) ->
                val ar = yToIdx(a.y)
                val br = yToIdx(b.y)
                val ac = xToIdx(a.x)
                val bc = xToIdx(b.x)
                val ys = min(ar, br)..max(ar, br)
                val xs = min(ac, bc)..max(ac, bc)
                // TODO: performance, try intersecting edges with borders without remapping
                val isGood = ys.all { yIdx -> xs.all { xIdx -> map[yIdx, xIdx] != '.' } }
                // TODO: performance: remember cur max and check isGood only if necessary
                if (isGood) area(a, b) else 0
            }
    }
}

private class P(val x: Long, val y: Long)

private fun area(a: P, b: P): Long =
    Math.multiplyExact((a.x - b.x).absoluteValue + 1, (a.y - b.y).absoluteValue + 1)
