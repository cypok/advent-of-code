package year2019

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

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

}
