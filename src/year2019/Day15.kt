package year2019

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.*
import kotlin.math.abs

// Task description:
//   https://adventofcode.com/2019/day/15

fun main() = runAoc {
    measureRunTime()

    @Suppress("LocalVariableName")
    solution1 {
        val UNKNOWN = ' '
        val EMPTY = '.'
        val WALL = '#'
        val START = '*'
        val TARGET = 'O'

        val size = 100
        val start = size/2 x size/2

        val canvas = Array(size) { Array(size) { UNKNOWN } }
        canvas[start] = START

        val distances = Array(size) { Array(size) { -1 } }
        distances[start] = 0

        fun wayToStart(from: Point) = sequence<Pair<Point, Int>> {
            var pos = from
            var dist = distances[from]
            assert(dist >= 0)

            while (true) {
                yield(pos to dist)
                if (dist == 0) break
                dist--
                pos = Dir.entries.map { pos.moveInDir(it) }.find { distances[it] == dist }!!
            }
        }

        fun wayFromTo(src: Point, dst: Point): Sequence<Point> {
            if (src == dst) return sequenceOf(src)
            if (abs(src.i - dst.i) + abs(src.j - dst.j) <= 1) return sequenceOf(src, dst)

            // We build two ways to start and find the point where they join.
            class CachedIterator<T>(private val base: Iterator<T>) {
                val way = mutableListOf<T>()
                fun next(): T = base.next().also { way += it }
            }

            val wS = CachedIterator(wayToStart(src).iterator())
            val wD = CachedIterator(wayToStart(dst).iterator())

            var pS = wS.next()
            var pD = wD.next()
            while (true) {
                if (pS.first == pD.first) break
                val op1 = pS
                val op2 = pD
                if (op1.second >= op2.second) { pS = wS.next() }
                if (op1.second <= op2.second) { pD = wD.next() }
            }
            val half1 = wS.way.asSequence()
            val half2 = wD.way.asReversed().asSequence().drop(1)
            return (half1 + half2).map { it.first }
        }

        fun dirFromTo(src: Point, dst: Point): Dir =
            when {
                dst.i == src.i && dst.j < src.j -> Dir.LEFT
                dst.i == src.i && dst.j > src.j -> Dir.RIGHT
                dst.j == src.j && dst.i < src.i -> Dir.UP
                dst.j == src.j && dst.i > src.i -> Dir.DOWN
                else -> error(src to dst)
            }

        val droid = object {
            private var curPos = start

            private val movement = Channel<Long>(capacity = Channel.RENDEZVOUS)
            private val status = Channel<Long>(capacity = Channel.RENDEZVOUS)

            init {
                GlobalScope.launch {
                    IntCodeComputer(intCode).run(movement, status)
                }
            }

            fun moveTo(dest: Point) {
                wayFromTo(curPos, dest)
                    .windowed(2)
                    .forEach { (f, t) ->
                        step(dirFromTo(f, t))
                    }
            }

            fun step(dir: Dir): Char = runBlocking {
                movement.send(1L + dir.ordinal)
                when (val v = status.receive().toIntExact()) {
                    0 -> WALL
                    1 -> EMPTY
                    2 -> TARGET
                    else -> error(v)
                }.also {
                    if (it != WALL) {
                        curPos = curPos.moveInDir(dir)
                    }
                }
            }
        }

        try {
            // BFS traversal. Initially I thought that the labyrinth was infinite and
            // thus implemented a BFS solution against infinitely deep corridors.
            // However, it requires a lot of movements.

            val queue = ArrayDeque<Point>()
            queue += start

            while (true) {
                val pos = queue.removeFirst()
                assert(canvas[pos].let { it != UNKNOWN && it != WALL })
                val dist = distances[pos]
                assert(dist >= 0)
                val nextDist = dist + 1

                for (dir in Dir.entries) {
                    val nextPos = pos.moveInDir(dir)
                    if (canvas[nextPos] != UNKNOWN) {
                        continue
                    }

                    droid.moveTo(pos)
                    val v = droid.step(dir)
                    canvas[nextPos] = v
                    if (v != WALL) {
                        if (isPart1 && v == TARGET) {
                            return@solution1 nextDist
                        }
                        distances[nextPos] = nextDist
                        queue += nextPos
                    }
                }
            }

        } finally {
            printExtra(canvas.toAsciiArt())
        }
    }
}
