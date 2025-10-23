package year2022

import utils.*
import kotlin.math.abs
import kotlin.math.sign

// Task description:
//   https://adventofcode.com/2022/day/9

fun main() = runAoc {
    example {
        answer1(13)
        answer2(1)
        """
            R 4
            U 4
            L 3
            D 1
            R 4
            D 1
            L 5
            R 2
        """
    }

    solution {
        val length = if (isPart1) 2 else 10

        val rope = Array(length) { 0 x 0 }
        val visited = mutableSetOf(rope.last())

        for (line in lines) {
            val (dirStr, stepsStr) = line.words()
            val dir = Dir.fromChar(dirStr.single())
            val steps = stepsStr.toInt()

            repeat(steps) {
                rope[0] = rope[0].moveInDir(dir)
                for (i in 1 until length) {
                    rope[i] = rope[i].moveCloserTo(rope[i - 1])
                }
                visited += rope.last()
            }
        }

        visited.size
    }
}

private fun Point.moveCloserTo(that: Point): Point {
    val di = that.i - this.i
    val dj = that.j - this.j
    return when {
        abs(di) <= 1 && abs(dj) <= 1 ->
            this
        else ->
            (this.i + di.sign) x (this.j + dj.sign)
    }
}
