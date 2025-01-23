package year2019

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import utils.toIntExact
import year2019.IntCodeComputer.AsciiApi.AsciiResult.*

class IntCodeComputer(program: List<Long>) {

    private class Mem(initial: List<Long>) {
        private val fixed = initial.toMutableList()
        private val flexible = mutableMapOf<Long, Long>()

        operator fun get(i: Long): Long =
            if (i < fixed.size) {
                fixed[i.toInt()]
            } else {
                flexible[i] ?: 0
            }

        operator fun set(i: Long, value: Long) {
            if (i < fixed.size) {
                fixed[i.toInt()] = value
            } else {
                flexible[i] = value
            }
        }
    }

    private val mem = Mem(program)

    operator fun get(i: Long): Long {
        return mem[i]
    }
    operator fun set(i: Long, value: Long) {
        mem[i] = value
    }

    suspend fun run(input: suspend () -> Long, output: suspend (Long) -> Unit) {
        var ip = 0L // instruction pointer
        var rb = 0L // relative base
        while (true) {
            val opAndMode = mem[ip++]
            val op = (opAndMode % 100).toInt()

            var remainingModes = opAndMode / 100
            fun nextMode(): Int {
                val mode = remainingModes % 10
                remainingModes /= 10
                return mode.toInt()
            }

            fun addr(mode: Int, raw: Long): Long =
                when (mode) {
                    0 -> raw
                    2 -> raw + rb
                    else -> error(mode)
                }

            fun param(): Long {
                val raw = mem[ip++]
                val mode = nextMode()
                return if (mode == 1) {
                    raw
                } else {
                    mem[addr(mode, raw)]
                }
            }

            fun result(value: Long) {
                val raw = mem[ip++]
                val mode = nextMode()
                check(mode != 1)
                mem[addr(mode, raw)] = value
            }

            fun jumpIf(cond: Boolean) {
                val dst = param()
                if (cond) {
                    ip = dst
                }
            }

            fun cmp(cond: Boolean) =
                result(if (cond) 1 else 0)

            when (op) {
                1 -> result(param() + param())
                2 -> result(param() * param())

                3 -> result(input())
                4 -> output(param())

                5 -> jumpIf(param() != 0L)
                6 -> jumpIf(param() == 0L)

                7 -> cmp(param() < param())
                8 -> cmp(param() == param())

                9 -> rb += param()

                99 -> break

                else -> error(op)
            }
        }
    }

    suspend fun run(input: ReceiveChannel<Long>, output: SendChannel<Long>) =
        run(input::receive, output::send)

    fun run(input: List<Long>): List<Long> =
        runBlocking {
            buildList {
                run(input.iterator()::next, ::add)
            }
        }

    fun run(vararg input: Long): List<Long> =
        run(input.asList())


    fun launchAsAscii(logging: (String) -> Unit = {}): AsciiApi =
        AsciiApi(this, logging)

    class AsciiApi(pc: IntCodeComputer, private val logging: (String) -> Unit) {
        private val runScope = CoroutineScope(Dispatchers.Default)
        private val output = Channel<Long>(capacity = Channel.UNLIMITED)
        private val input = Channel<Long>(capacity = Channel.UNLIMITED)

        init {
            runScope.launch {
                pc.run(input, output)
                output.close()
                runScope.cancel()
            }
        }

        fun printLine(str: String) = runBlocking {
            str.lines().forEach { logging(">> $it") }
            str.forEach { input.send(it.code.toLong()) }
            input.send('\n'.code.toLong())
        }

        sealed interface AsciiResult {
            object End : AsciiResult
            class Str(val value: String) : AsciiResult
            class Num(val value: Long) : AsciiResult
        }

        private fun read(): AsciiResult = runBlocking {
            val rcv = output.receiveCatching()
            if (rcv.isClosed) {
                return@runBlocking End
                    .also {
                        logging("<$ The End.")
                    }
            }
            val value = rcv.getOrThrow()
            if (value in 0..127) {
                Str(buildString {
                    var raw = value
                    while (raw != '\n'.code.toLong()) {
                        append(raw.toIntExact().toChar())
                        raw = output.receive()
                    }
                }).also {
                    logging("<< ${it.value}")
                }
            } else {
                Num(value).also {
                    logging("<# ${it.value}")
                }
            }
        }

        fun readNum(): Long = (read() as Num).value
        fun readLine(): String = (read() as Str).value

        fun expectLine(expected: String) {
            val actual = readLine()
            check(actual == expected) { "Expected '$expected' but got '$actual'" }
        }

        fun expectEnd() {
            check(read() == End)
        }
    }

}
