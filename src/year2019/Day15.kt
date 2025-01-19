package year2019

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.*

// Task description:
//   https://adventofcode.com/2019/day/15

fun main() = runAoc {
    measureRunTime()

    @Suppress("LocalVariableName")
    solution {
        val UNKNOWN = ' '
        val EMPTY = '.'
        val WALL = '#'
        val START = '*'
        val OXYGEN = 'O'

        val size = 100
        val start = size/2 x size/2

        val canvas = Array2D.ofChars(size, size, UNKNOWN)
        canvas[start] = START

        val droid = object {
            private val movement = Channel<Long>(capacity = Channel.RENDEZVOUS)
            private val status = Channel<Long>(capacity = Channel.RENDEZVOUS)

            init {
                GlobalScope.launch {
                    IntCodeComputer(intCode).run(movement, status)
                }
            }

            fun step(dir: Dir): Char = runBlocking {
                movement.send(1L + dir.ordinal)
                when (val v = status.receive().toIntExact()) {
                    0 -> WALL
                    1 -> EMPTY
                    2 -> OXYGEN
                    else -> error(v)
                }
            }
        }

        fun dfs(pos: Point) {
            assert(canvas[pos].let { it != UNKNOWN && it != WALL })

            for (dir in Dir.entries) {
                val nextPos = pos.moveInDir(dir)
                if (canvas[nextPos] != UNKNOWN) {
                    continue
                }

                val v = droid.step(dir)
                canvas[nextPos] = v
                if (v != WALL) {
                    dfs(nextPos)
                    droid.step(dir.opposite)
                }
            }
        }

        dfs(start)
        printExtra(canvas.toAsciiArt())

        fun distanceFromTo(src: Point, dst: Point?): Int {
            val queue = ArrayDeque<Point>()
            val distances = Array2D.ofInts(canvas.height, canvas.width, Int.MAX_VALUE)

            distances[src] = 0
            queue += src

            while (true) {
                val pos = queue.removeFirst()
                val dist = distances[pos]
                val nextDist = dist + 1

                for (dir in Dir.entries) {
                    val nextPos = pos.moveInDir(dir)
                    if (nextPos == dst) {
                        return nextDist
                    }
                    if (canvas[nextPos] != WALL && nextDist < distances[nextPos]) {
                        distances[nextPos] = nextDist
                        queue += nextPos
                    }
                }

                if (queue.isEmpty()) {
                    // It was a maximum distance.
                    return dist
                }
            }
        }

        val oxygen = canvas.find { it == OXYGEN }
        distanceFromTo(oxygen, if (isPart1) start else null)
    }
}
