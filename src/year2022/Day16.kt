package year2022

import utils.*
import java.util.BitSet
import java.util.PriorityQueue
import kotlin.math.max
import kotlin.math.min

// Task description:
//   https://adventofcode.com/2022/day/16
fun main() = main2()
fun main2() = runAoc {
    //measureRunTime()

    if (false) example {
        answer1(1651)
        answer2(1707)
        """
            Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
            Valve BB has flow rate=13; tunnels lead to valves CC, AA
            Valve CC has flow rate=2; tunnels lead to valves DD, BB
            Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
            Valve EE has flow rate=3; tunnels lead to valves FF, DD
            Valve FF has flow rate=0; tunnels lead to valves EE, GG
            Valve GG has flow rate=0; tunnels lead to valves FF, HH
            Valve HH has flow rate=22; tunnel leads to valve GG
            Valve II has flow rate=0; tunnels lead to valves AA, JJ
            Valve JJ has flow rate=21; tunnel leads to valve II
        """
    }

    solution1 {
        val input = lines.map { line ->
            // e.g. "Valve AA has flow rate=0; tunnels lead to valves DD, II, BB"
            val words = line.words()
            val valve = words[1]
            val rate = words[4].numbersAsInts().single()
            val adjacentValves = words.drop(9).map { it.trim(',') }
            Triple(valve, rate, adjacentValves)
        }
        val valves: Map<String, Valve> =
            input.associate { (v, r, _) ->
                if (false) printExtra("$v [label = \"$v ($r)\"];")
                v to Valve(v, r)
            }
        val start = valves["AA"]!!
        check(start.rate == 0)

        val adjacentValvesRaw: Map<Valve, List<Valve>> =
            input.associate { (v, _, avs) ->
                if (false) printExtra("$v -> ${avs.joinToString(", ")};")
                valves[v]!! to avs.map { valves[it]!! }
            }

        val adjacentValvesCompressed: Map<Valve, List<Pair<Valve, Int>>> = run {
            fun nextNonZero(prev: Valve, cur: Valve, acc: Int): Pair<Valve, Int>? {
                if (cur.rate == 0) {
                    val next = adjacentValvesRaw[cur]!!.filter { it != prev }
                    when (next.size) {
                        0 -> return null
                        1 -> return nextNonZero(cur, next.single(), acc + 1)
                        else -> check(cur == start)
                    }
                }
                return cur to acc
            }

            adjacentValvesRaw
                .filterKeys { it.rate > 0 || it == start }
                .mapValues { (v, avs) ->
                    avs.mapNotNull { av ->
                        nextNonZero(v, av, 1)
                    }
                }
        }

        val graph: Map<Valve, Map<Valve, Int>> =
            adjacentValvesCompressed.keys
                .associateWith { base ->
                    mutableMapOf<Valve, Int>().also { dists ->

                        val queue = PriorityQueue<Pair<Valve, Int>>(Comparator.comparing { it.second })
                        queue.add(base to 0)
                        while (queue.isNotEmpty()) {
                            val (cur, dist) = queue.poll()
                            if (cur in dists) continue
                            if (cur != base) dists[cur] = dist

                            adjacentValvesCompressed[cur]!!.forEach { (next, leg) ->
                                if (next !in dists && next != base) {
                                    queue.add(next to (dist + leg))
                                }
                            }
                        }
                    }
                }

        fun dist(src: Valve, dst: Valve) =
            graph.getValue(src).getValue(dst)

        val goodValves = valves.values.filter { it.rate > 0 }.sortedBy { it.rate }

        val valveId: Map<Valve, Int> = valves.toList().sortedBy { it.first }.withIndex().associate { (i, namedValve) -> namedValve.second to i }
        val alreadyOpened = BitSet(valveId.size)

        @Suppress("LocalVariableName")
        val NEVER = 1_000_000

        var currentBest = -1
        tailrec fun simulate(
            valveA: Valve, timeToValveA: Int,
            valveB: Valve, timeToValveB: Int,
            timeLeft: Int, alreadyReleased: Int, oldRate: Int,
            skipBoundCheck: Boolean = false
        ): Int {
            if (timeLeft == 0) {
                if (alreadyReleased > currentBest) {
                    currentBest = alreadyReleased
                }
                return alreadyReleased
            }

            fun estimateMax(): Int =
                alreadyReleased +
                        oldRate * timeLeft +
                        valveA.rate * max(0, timeLeft - timeToValveA) +
                        valveB.rate * max(0, timeLeft - timeToValveB) +
                        goodValves
                            .sumOf {
                                if (valveId[it]!! in alreadyOpened) return@sumOf 0
                                val tA = timeToValveA + dist(valveA, it)
                                val tB = timeToValveB + dist(valveB, it)
                                it.rate * max(0, timeLeft - min(tA, tB) - 1)
                            }

            if (!skipBoundCheck && estimateMax() < currentBest) {
                return -1
            }

            fun tryOpenMore(src: Valve, calcWithOpened: (Valve, Int) -> Int?): Int? {
                return goodValves.mapNotNull { next ->
                    val nextId = valveId[next]!!
                    if (nextId in alreadyOpened) return@mapNotNull null
                    val timeToOpenNext = dist(src, next) + 1
                    if (timeToOpenNext > timeLeft) return@mapNotNull null
                    try {
                        alreadyOpened += nextId
                        calcWithOpened(next, timeToOpenNext)
                    } finally {
                        alreadyOpened -= nextId
                    }
                }.maxOrNull()
            }

            val hitA = timeToValveA == 0
            val hitB = timeToValveB == 0

            val newRate = oldRate +
                    (if (hitA) valveA.rate else 0) +
                    (if (hitB) valveB.rate else 0)

            return when {
                !hitA && !hitB ->
                    simulate(
                        valveA, timeToValveA - 1,
                        valveB, timeToValveB - 1,
                        timeLeft - 1, alreadyReleased + newRate, newRate,
                        skipBoundCheck = true
                    )

                hitA && !hitB ->
                    tryOpenMore(valveA) { next, timeToOpenNext ->
                        @Suppress("NON_TAIL_RECURSIVE_CALL")
                        simulate(
                            next, timeToOpenNext - 1,
                            valveB, timeToValveB - 1,
                            timeLeft - 1, alreadyReleased + newRate, newRate
                        )
                    } ?: simulate(
                        start, NEVER,
                        valveB, timeToValveB - 1,
                        timeLeft - 1, alreadyReleased + newRate, newRate
                    )

                !hitA && hitB ->
                    tryOpenMore(valveB) { next, timeToOpenNext ->
                        @Suppress("NON_TAIL_RECURSIVE_CALL")
                        simulate(
                            valveA, timeToValveA - 1,
                            next, timeToOpenNext - 1,
                            timeLeft - 1, alreadyReleased + newRate, newRate
                        )
                    } ?: simulate(
                        valveA, timeToValveA - 1,
                        start, NEVER,
                        timeLeft - 1, alreadyReleased + newRate, newRate
                    )

                else -> // hitA && hitB
                    tryOpenMore(valveA) { nextA, timeToOpenNextA ->
                        tryOpenMore(valveB) { nextB, timeToOpenNextB ->
                            @Suppress("NON_TAIL_RECURSIVE_CALL")
                            simulate(
                                nextA, timeToOpenNextA - 1,
                                nextB, timeToOpenNextB - 1,
                                timeLeft - 1, alreadyReleased + newRate, newRate
                            )
                        }
                    } ?: simulate(
                        start, NEVER,
                        start, NEVER,
                        timeLeft - 1, alreadyReleased + newRate, newRate
                    )
            }
        }

        check(start.rate == 0)
        simulate(
            start, 0,
            start, if (isPart1) NEVER else 0,
            if (isPart1) 30 else 26, 0, 0,
            skipBoundCheck = true
        )
    }
}

private class Valve(val name: String, val rate: Int) {
    override fun toString() = name
}