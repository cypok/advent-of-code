package year2019

import kotlinx.coroutines.runBlocking
import year2019.IntCodeComputer.State.*

class IntCodeComputer(program: List<Long>) {
    enum class State {
        NOT_STARTED,
        RUNNING,
        WAITING_IO,
        FINISHED,
    }

    @Volatile
    var state = NOT_STARTED
        private set

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
        check(state == NOT_STARTED || state == FINISHED)
        return mem[i]
    }
    operator fun set(i: Long, value: Long) {
        check(state == NOT_STARTED || state == FINISHED)
        mem[i] = value
    }

    private fun changeState(from: State, to: State) {
        check(state == from)
        state = to
    }

    private inline fun <R> withState(from: State, to: State, action: () -> R): R {
        changeState(from, to)
        try {
            return action()
        } finally {
            changeState(to, from)
        }
    }

    private inline fun <R> io(action: () -> R) =
        withState(RUNNING, WAITING_IO) { action() }

    suspend fun run(input: suspend () -> Long, output: suspend (Long) -> Unit) {
        changeState(NOT_STARTED, RUNNING)

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

                3 -> result(io { input() })
                4 -> param().let { io { output(it) } }

                5 -> jumpIf(param() != 0L)
                6 -> jumpIf(param() == 0L)

                7 -> cmp(param() < param())
                8 -> cmp(param() == param())

                9 -> rb += param()

                99 -> break

                else -> error(op)
            }
        }

        changeState(RUNNING, FINISHED)
    }

    fun run(input: List<Long>): List<Long> =
        runBlocking() {
            buildList {
                run(input.iterator()::next, ::add)
            }
        }

    fun run(vararg input: Long): List<Long> =
        run(input.asList())

}