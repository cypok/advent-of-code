package year2023

import utils.*
import year2023.Module.*

fun main() = test(
    ::solve1,
    ::solve2,
)

private fun CyclicState<Pulse>.toPulseString() =
    "${this.current.toPulseString()}/${this.detectCycle() ?: "?"}"

private sealed class Module(val name: String, val outputNames: List<String>) {
    open fun prepareInput(input: String): Unit = Unit

    abstract fun process(inputs: List<Signal>): List<Pulse>

    open fun stateTick(time: Long): Unit = Unit

    open fun detectStateCycle(): Long? = 1

    final override fun toString() = "$name{${extraToString()}}"

    open fun extraToString() = ""

    class Output(name: String) : Module(name, emptyList()) {
        override fun process(inputs: List<Signal>): List<Pulse> =
            emptyList()
    }

    class Broadcaster(outputs: List<String>) : Module(NAME, outputs) {
        companion object {
            const val NAME = "broadcaster"
        }

        override fun process(inputs: List<Signal>) = listOf(inputs.single().value)
    }

    class FlipFlop(name: String, outputs: List<String>) : Module(name, outputs) {
        private var state = CyclicState(false)

        override fun process(inputs: List<Signal>) =
            if (inputs.all { it.value }) {
                emptyList()
            } else {
                state.current = !state.current
                listOf(state.current)
            }

        override fun stateTick(time: Long) =
            state.tick(time)

        override fun detectStateCycle(): Long? =
            state.detectCycle()?.toLong()

        override fun extraToString(): String =
            state.toPulseString()
    }

    class Conjunction(name: String, outputs: List<String>) : Module(name, outputs) {
        val state = mutableMapOf<String, CyclicState<Pulse>>()

        override fun prepareInput(input: String) {
            state[input] = CyclicState(false)
        }

        override fun process(inputs: List<Signal>): List<Pulse> =
            inputs.map {
                state[it.src]!!.current = it.value
                val allHigh = state.values.all { it.current }
                !allHigh
            }

        override fun stateTick(time: Long) =
            state.values.tickAll(time)

        override fun detectStateCycle(): Long? =
            state.values.detectCommonCycle()

        override fun extraToString(): String {
            val commonCycle = detectStateCycle()
            val internals = if (true) {
                "/" + state.entries.joinToString { (src, state) -> "$src: ${state.toPulseString()}" }
            } else {
                ""
            }
            return "($commonCycle)$internals"
        }
    }
}

private typealias Pulse = Boolean
private fun Pulse.toPulseString() = if (this) "1" else "0"

private data class Signal(val src: String, val value: Pulse, val dst: String) {
    override fun toString(): String =
        "$src -${value.toPulseString()}-> $dst"
}

private fun parseModules(input: List<String>): Map<String, Module> {
    val modules = input.associate {
        val fullName = it.substringBefore(" -> ")
        val outputs = it.substringAfter(" -> ").split(", ")
        val module = when {
            fullName == Broadcaster.NAME -> Broadcaster(outputs)
            fullName.startsWith('%') -> FlipFlop(fullName.substring(1), outputs)
            fullName.startsWith('&') -> Conjunction(fullName.substring(1), outputs)
            else -> error(it)
        }
        module.name to module
    }.toMutableMap()

    val plainOutputs = modules.values.flatMap { it.outputNames }.filter { it !in modules }.map { Output(it) }
    modules += plainOutputs.associateBy { it.name }

    for (src in modules.values) {
        for (dst in src.outputNames) {
            modules[dst]!!.prepareInput(src.name)
        }
    }

    return modules
}

@Suppress("unused")
private fun modulesToGraphviz(modules: Map<String, Module>): String =
    with(StringBuilder()) {
        appendLine("digraph G {")
        for (src in modules.values) {
            val shape = when (src) {
                is Broadcaster -> "doublecircle"
                is Conjunction -> "pentagon"
                is FlipFlop -> "invtriangle"
                is Output -> "star"
            }
            appendLine(" ${src.name} [ shape=$shape ];")
            for (dst in src.outputNames) {
                appendLine(" ${src.name} -> $dst;")
            }
        }
        appendLine("}")
        toString()
    }

private fun sendSignals(modules: Map<String, Module>, signals: List<Signal>): List<Signal> =
    signals.groupBy { it.dst }.flatMap { (dst, ss) ->
        val module = modules[dst]!!
        val newPulses = module.process(ss)
        newPulses.flatMap { newPulse ->
            module.outputNames.map { Signal(dst, newPulse, it) }
        }
    }

private fun pressButtonAndWait(modules: Map<String, Module>): Triple<Long, Long, Boolean> {
    var lows = 0L
    var highs = 0L
    var rxGotLow = false

    var signals = listOf(Signal("button", false, Broadcaster.NAME))
    while (signals.isNotEmpty()) {
        lows += signals.count { !it.value }
        highs += signals.count { it.value }
        rxGotLow = rxGotLow || (signals.any { it.dst == "rx" && !it.value })
        signals = sendSignals(modules, signals)
    }

    return Triple(lows, highs, rxGotLow)
}

private fun solve1(input: List<String>): Long {
    val modules = parseModules(input)

    var totalLow = 0L
    var totalHigh = 0L
    repeat(1000) {
        val (lows, highs, _) = pressButtonAndWait(modules)
        totalLow += lows
        totalHigh += highs
    }

    return totalLow * totalHigh
}

private fun solve2(input: List<String>): Long {
    val modules = parseModules(input)

    if (false) println()
    var times = 0L
    while (true) {
        modules.forEach { it.value.stateTick(times) }

        if (times % 10000 == 0L) {
            val (cyclic, acyclic) = modules.values
                .map { it.detectStateCycle() }
                .partition { it != null }
            val commonCycle =
                if (acyclic.isEmpty()) cyclic.reduce { a, b -> lcm(a!!, b!!) } else null

            if (false) {
                val cycleInfo =
                    if (commonCycle != null) {
                        "common cycle $commonCycle}"
                    } else {
                        "not yet ${cyclic.size}/${acyclic.size}"
                    }
                println("$times ($cycleInfo) : ${modules.values.joinToString { it.toString() }}")
            }

            if (commonCycle != null) {
                return commonCycle
            }
        }

        times++
        val (_, _, finish) = pressButtonAndWait(modules)
        assert(!finish) { "universe collapsed?!" }
    }
}
