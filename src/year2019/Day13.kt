package year2019

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.whileSelect
import utils.*
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.math.sign

// Task description:
//   https://adventofcode.com/2019/day/13

fun main() = runAoc {
    @Suppress("LocalVariableName")
    solution {
        val EMPTY = ' '
        val WALL = FULL_BLOCK
        val BLOCK = '#'
        val PADDLE = '='
        val BALL = 'o'
        val cellsById = arrayOf(EMPTY, WALL, BLOCK, PADDLE, BALL)

        val size = 100
        val canvas = Array(size) { Array(size) { EMPTY } }

        val game = IntCodeComputer(intCode)

        if (isPart1) {
            game.run().map { it.toIntExact() }.chunked(3).forEach { (x, y, id) ->
                canvas[y, x] = cellsById[id]
            }
            printExtra(canvas.toAsciiArt(EMPTY))
            canvas.valuesIndexed.count { (ch, _) -> ch == BLOCK }

        } else runBlocking {
            val info = object {
                val score = 1
                val joystick = 3

                fun fieldWidth() = canvas[0].count { it == WALL }

                fun clear(line: Int) {
                    (fieldWidth() + 1 until size).forEach { j ->
                        canvas[line, j] = EMPTY
                    }
                }

                fun draw(line: Int, text: String) {
                    text.withIndex().forEach { (idx, ch) ->
                        canvas[line, fieldWidth() + 1 + idx] = ch
                    }
                }
            }

            val frames = object {
                val enabled = false

                var nextFrameId = -1
                val dir = Path("/tmp/aoc-2019-13")

                init {
                    if (enabled) {
                        dir.deleteRecursively()
                        dir.createDirectories()
                    }
                }

                fun start() {
                    if (enabled && nextFrameId == -1) {
                        nextFrameId = 0
                    }
                }

                fun save() {
                    if (enabled && nextFrameId >= 0) {
                        val frame = dir.resolve("frame${nextFrameId++}.txt")
                        frame.writeText(canvas.toAsciiArt(EMPTY))
                    }
                }
            }

            val joystick = Channel<Long>(capacity = Channel.RENDEZVOUS)
            // Add buffer for several drawing instructions between joystick requests:
            val renderInstructions = Channel<Long>(capacity = 3 * 20)
            launch {
                game[0] = 2
                game.run(joystick, renderInstructions)
                renderInstructions.close()
            }

            var score = 0
            var paddlePos: Int? = null
            var ballPos: Int? = null
            var nextJoystickValue = 0

            whileSelect {
                renderInstructions.onReceiveCatching { r ->
                    val x = r.getOrNull()?.toIntExact() ?: return@onReceiveCatching false
                    val y = renderInstructions.receive().toIntExact()
                    val id = renderInstructions.receive().toIntExact()

                    if (x == -1 && y == 0) {
                        score = id
                        if (frames.enabled) {
                            frames.start()
                            info.clear(info.score)
                            info.draw(info.score, score.toString())
                        }

                    } else {
                        val type = cellsById[id]
                        canvas[y, x] = type

                        if (type == PADDLE || type == BALL) {
                            when (type) {
                                PADDLE -> paddlePos = x
                                BALL -> ballPos = x
                            }

                            if (paddlePos != null && ballPos != null) {
                                nextJoystickValue = (ballPos - paddlePos).sign
                            }
                        }
                    }

                    frames.save()
                    true
                }

                joystick.onSend(nextJoystickValue.toLong()) {
                    if (frames.enabled) {
                        val ch = when (nextJoystickValue) {
                            -1 -> '<'
                            0 -> '='
                            1 -> '>'
                            else -> shouldNotReachHere()
                        }
                        frames.start()
                        info.draw(info.joystick, ch.toString().repeat(5))
                        frames.save()
                        info.clear(info.joystick)
                        frames.save()
                    }
                    true
                }
            }

            printExtra(canvas.toAsciiArt(EMPTY))
            score
        }
    }
}
