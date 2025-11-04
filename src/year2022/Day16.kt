package year2022

import utils.*
import java.util.BitSet
import java.util.PriorityQueue

// Task description:
//   https://adventofcode.com/2022/day/16

fun main() = runAoc {
    measureRunTime()

    example {
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

        val graph: Map<Valve, Map<Valve, Int>> = run {
            adjacentValvesCompressed.keys.associateWith { base ->
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
        }

        val goodValves = graph.keys.filter { it.rate > 0 }

        val valveId: Map<Valve, Int> = valves.toList().sortedBy { it.first }.withIndex().associate { (i, namedValve) -> namedValve.second to i }
        val alreadyOpened = BitSet(valveId.size)

        fun open(valve: Valve, timeLeft: Int, alreadyReleased: Int, oldRate: Int): Int {
            assert(timeLeft >= 0)
            val newRate = oldRate + valve.rate
            val somethingOpened = goodValves.maxOf { next ->
                val nextId = valveId[next]!!
                if (alreadyOpened[nextId]) return@maxOf -1
                val timeToOpenNext = graph[valve]!![next]!! + 1
                if (timeToOpenNext > timeLeft) return@maxOf -1
                try {
                    alreadyOpened[nextId] = true
                    open(next, timeLeft - timeToOpenNext, alreadyReleased + timeToOpenNext * newRate, newRate)
                } finally {
                    alreadyOpened[nextId] = false
                }
            }
            return if (somethingOpened != -1) somethingOpened else alreadyReleased + timeLeft * newRate
        }

        check(start.rate == 0)
        open(start, 30, 0, 0)
    }
}

private class Valve(val name: String, val rate: Int)