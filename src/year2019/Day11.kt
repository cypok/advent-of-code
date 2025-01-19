package year2019

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.whileSelect
import utils.*

// Task description:
//   https://adventofcode.com/2019/day/11

fun main() = runAoc {
    measureRunTime()

    @Suppress("LocalVariableName")
    solution { runBlocking {
        val camera = Channel<Long>(capacity = Channel.Factory.RENDEZVOUS)
        val instructions = Channel<Long>(capacity = 2)
        val robot = launch {
            IntCodeComputer(intCode).run(camera, instructions)
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
        val canvas = Array2D.ofChars(size, size, B)
        var curPos = size/2 x size/2
        var curDir = Dir.UP

        if (isPart2) {
            canvas[curPos] = W
        }

        val touched = mutableSetOf<Point>()
        whileSelect {
            robot.onJoin { false }

            camera.onSend(c2l(canvas[curPos])) {
                val color = l2c(instructions.receive())
                curDir = when (val dirCmd = instructions.receive()) {
                    0L -> curDir.left
                    1L -> curDir.right
                    else -> error(dirCmd)
                }
                canvas[curPos] = color
                touched += curPos
                curPos = curPos.moveInDir(curDir)
                true
            }
        }

        if (isPart1) {
            touched.size
        } else {
            visualAnswer(canvas.toAsciiArt())
        }
    }}
}