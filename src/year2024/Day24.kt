package year2024

import utils.*
import java.io.File

// Task description:
//   https://adventofcode.com/2024/day/24

fun main() = runAoc {
    solution {
        val [inputDescs, gateDescs] = lines.splitByEmptyLines().toList()

        val inputValues = inputDescs.associate { line ->
            val [name, value] = line.split(": ")
            name to (value != "0")
        }
        val inputWires = inputValues.keys

        data class Gate(val l: String, val r: String, val res: String, val op: String)

        val gateRegex = """(.*) (AND|OR|XOR) (.*) -> (.*)""".toRegex()
        val gates = gateDescs.associate { line ->
            val [l, op, r, res] = gateRegex.find(line)!!.groupValues.drop(1)
            res to Gate(l, r, res, op)
        }

        fun performOp(gateOp: String, left: Boolean, right: Boolean): Boolean =
            when (gateOp) {
                "AND" -> left && right
                "OR" -> left || right
                "XOR" -> left xor right
                else -> error(gateOp)
            }

        if (true) {
            File("/tmp/gates.dot").printWriter().use { it.apply {
                println("digraph G {")
                for (w in inputWires) {
                    println("{ node [style=filled,fillcolor=palegreen] $w; }")
                }
                for (g in gates.values) {
                    val name = g.res
                    val style = if (name.startsWith('z')) ",style=filled,fillcolor=lightpink" else ""
                    println("{ node [label=\"$name [${g.op}]\"$style] $name; }")
                }
                for (gate in gates.values) {
                    println("${gate.l} -> ${gate.res};")
                    println("${gate.r} -> ${gate.res};")
                }
                println("}")
            }}
        }

        val zWires = gates.keys.filter { it.startsWith('z') }.sorted()

        if (isPart1) {
            fun calcValue(wire: String): Boolean {
                inputValues[wire]?.let { return it }
                val gate = gates[wire]!!
                val l = calcValue(gate.l)
                val r = calcValue(gate.r)
                return performOp(gate.op, l, r)
            }

            zWires
                .map(::calcValue)
                .asReversed()
                .joinToString("") { if (it) "1" else "0" }
                .toLong(2)

        } else {
            val correctGates = mutableSetOf<String>()

            val swaps = mutableMapOf<String, String>()
            fun gateByName(name: String): Gate? =
                gates[swaps[name] ?: name]
            fun gateName(gate: Gate): String = swaps[gate.res] ?: gate.res

            outer@for ([i, zWire] in zWires.withIndex()) {
                fun check(): Boolean {
                    fun decToBin(v: Int) = (v and 1 != 0) to (v and 2 != 0)

                    for (x in 0 until 4) {
                        val [x0, x1] = decToBin(x)
                        for (y in 0 until 4) {
                            val [y0, y1] = decToBin(y)

                            // No need to check carry.
                            if (i == 0 && (x0 || y0)) continue
                            // Check only carry.
                            if (i == zWires.size - 1 && (x1 || y1)) continue

                            fun calcValue(wire: String): Boolean? =
                                if (wire in inputWires) {
                                    val inputIdx = wire.drop(1).toInt()
                                    val isX = wire.startsWith('x')
                                    if (inputIdx == i) {
                                        if (isX) x1 else y1
                                    } else if (inputIdx == i - 1) {
                                        if (isX) x0 else y0
                                    } else if (inputIdx <= i - 2) {
                                        false
                                    } else {
                                        null
                                    }
                                } else {
                                    val gate = gateByName(wire)!!
                                    val l = calcValue(gate.l) ?: return null
                                    val r = calcValue(gate.r) ?: return null
                                    performOp(gate.op, l, r)
                                }

                            val [z0, z1] = decToBin(x + y)

                            if (i > 0 && calcValue(zWires[i - 1]) != z0) return false
                            if (calcValue(zWires[i]) != z1) return false
                        }
                    }

                    return true
                }

                fun collectUsedNewGates() = buildSet {
                    fun calcValue(wire: String) {
                        if (wire in correctGates) return
                        val gate = gateByName(wire) ?: return
                        add(wire)
                        calcValue(gate.l)
                        calcValue(gate.r)
                    }
                    calcValue(zWire)
                }

                val usedNewGates = collectUsedNewGates()

                if (check()) {
                    correctGates += usedNewGates
                    continue
                }

                val availableNewGates = buildSet {
                    fun availableInput(arg: String) =
                        arg in correctGates || arg in this ||
                                (arg in inputWires && arg.drop(1).toInt() <= i)

                    while (true) {
                        val newGates = gates.values
                            .filter { gate ->
                                val name = gateName(gate)
                                name !in correctGates && name !in this &&
                                        availableInput(gate.l) && availableInput(gate.r)
                            }
                            .map { gateName(it) }
                        if (!addAll(newGates)) {
                            break
                        }
                    }
                }

                for ([u, a] in listOf(usedNewGates, availableNewGates).cartesianProduct()) {
                    check(u !in swaps && a !in swaps)
                    swaps[u] = a
                    swaps[a] = u
                    if (check()) {
                        correctGates += collectUsedNewGates()
                        continue@outer
                    }
                    swaps.remove(u)
                    swaps.remove(a)
                }

                error("no swaps were found")
            }

            check(swaps.size == 8)
            swaps.keys.sorted().joinToString(",")
        }
    }
    example {
        answer1(2024)
        """
            x00: 1
            x01: 0
            x02: 1
            x03: 1
            x04: 0
            y00: 1
            y01: 1
            y02: 1
            y03: 1
            y04: 1

            ntg XOR fgs -> mjb
            y02 OR x01 -> tnw
            kwq OR kpj -> z05
            x00 OR x03 -> fst
            tgd XOR rvg -> z01
            vdt OR tnw -> bfw
            bfw AND frj -> z10
            ffh OR nrd -> bqk
            y00 AND y03 -> djm
            y03 OR y00 -> psh
            bqk OR frj -> z08
            tnw OR fst -> frj
            gnj AND tgd -> z11
            bfw XOR mjb -> z00
            x03 OR x00 -> vdt
            gnj AND wpb -> z02
            x04 AND y00 -> kjc
            djm OR pbm -> qhw
            nrd AND vdt -> hwm
            kjc AND fst -> rvg
            y04 OR y02 -> fgs
            y01 AND x02 -> pbm
            ntg OR kjc -> kwq
            psh XOR fgs -> tgd
            qhw XOR tgd -> z09
            pbm OR djm -> kpj
            x03 XOR y03 -> ffh
            x00 XOR y04 -> ntg
            bfw OR bqk -> z06
            nrd XOR fgs -> wpb
            frj XOR qhw -> z04
            bqk OR frj -> z07
            y03 OR x01 -> nrd
            hwm AND bqk -> z03
            tgd XOR rvg -> z12
            tnw OR pbm -> gnj
        """
    }
}