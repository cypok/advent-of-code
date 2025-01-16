package year2019

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.*

// Task description:
//   https://adventofcode.com/2019/day/11

fun main() = runAoc {
    @Suppress("LocalVariableName")
    solution { runBlocking {
        val camera = Channel<Long>()
        val instructions = Channel<Long>()
        val robot = IntCodeComputer(intCode)
        launch {
            robot.run(camera, instructions)
            camera.close()
        }

        val B = ' '
        val W = FULL_BLOCK

        fun c2l(v: Char): Long =
            when (v) {
                B -> 0
                W -> 1
                else -> error(v)
            }

        fun l2c(v: Long): Char =
            when (v) {
                0L -> B
                1L -> W
                else -> error(v)
            }

        val size = 200
        val canvas = Array(size) { Array(size) { B } }
        var curPos = size/2 x size/2
        var curDir = Dir.UP

        if (isPart2) {
            canvas[curPos] = W
        }

        val touched = mutableSetOf<Point>()
        while (true) {
            camera.sendCatchingClosed(c2l(canvas[curPos])).onFailure { break }
            val color = l2c(instructions.receive())
            curDir = when (val dirCmd = instructions.receive()) {
                0L -> curDir.left
                1L -> curDir.right
                else -> error(dirCmd)
            }
            canvas[curPos] = color
            touched += curPos
            curPos = curPos.moveInDir(curDir)
        }

        if (isPart1) {
            touched.size
        } else {
            visualAnswer(canvas.toAsciiArt())
        }
    }}
}