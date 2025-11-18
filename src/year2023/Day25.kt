package year2023

import utils.*
import kotlin.random.Random

// Task description:
//   https://adventofcode.com/2023/day/25

fun main() = runAoc {
    measureRunTime()

    example {
        answer1(54)
        """
            jqt: rhn xhk nvd
            rsh: frs pzl lsr
            xhk: hfx
            cmg: qnr nvd lhk bvb
            rhn: xhk bvb hfx
            bvb: xhk hfx
            pzl: lsr hfx nvd
            qnr: nvd
            ntq: jqt hfx bvb xhk
            nvd: lhk
            lsr: lhk
            rzs: qnr cmg lsr rsh
            frs: qnr lhk lsr
        """
    }

    solution1 {
        val enoughPathsCount = 1000

        val g = Graph<String>()
        for (line in lines) {
            val src = line.substringBefore(": ")
            val dsts = line.substringAfter(": ").words()
            for (dst in dsts) {
                g.addEdge(src, dst)
            }
        }

        // Only three edges connect the whole graph.
        // Take random paths, collect the most frequent edges, they are likely to be a cut.
        val frequentEdges = MultiSet<Pair<String, String>>()
        val rng = Random(2023 * 25)
        repeat(enoughPathsCount) { i ->
            val start = g.vertices.random(rng)
            val finish = g.vertices.random(rng)
            val path = g.findPath(start, finish)!!.map { it.sorted() }
            frequentEdges.addAll(path)
            if (frequentEdges.uniqueCount >= 3 && i % 10 == 0) {
                val cutEdges = frequentEdges.grouped.sortedByDescending { it.count }.map { it.elem }.take(3).toList()
                g.withoutEdges(cutEdges) {
                    val cutSize = g.findSccSize(g.vertices.first())
                    if (cutSize < g.vertices.size) {
                        return@solution1 1L * cutSize * (g.vertices.size - cutSize)
                    }
                }
            }
        }

        error("min cut not found")
    }
}

private fun Pair<String, String>.sorted(): Pair<String, String> =
    if (first > second) second to first else this

private class Graph<V> {
    val vertices = mutableSetOf<V>()
    val edges = mutableMapOf<V, MutableSet<V>>()

    fun addEdge(src: V, dst: V) {
        vertices += src
        vertices += dst
        edges.getOrPut(src) { mutableSetOf() } += dst
        edges.getOrPut(dst) { mutableSetOf() } += src
    }

    private inline fun <S, T> traverse(
        initState: S,
        nextState: (S, V) -> S,
        nodeFromState: (S) -> V,
        finish: Pair<V, (S) -> T>?,
        onEnd: (Set<V>) -> T
    ): T {
        val visited = mutableSetOf<V>()
        val next = ArrayDeque<S>()
        next += initState

        while (next.isNotEmpty()) {
            val s = next.removeFirst()
            val node = nodeFromState(s)

            if (node in visited) continue
            visited += node

            if (finish != null && node == finish.first) {
                return finish.second(s)
            }

            for (dst in edges[node]!!) {
                if (dst !in visited) {
                    next += nextState(s, dst)
                }
            }
        }

        return onEnd(visited)
    }

    fun findPath(start: V, finish: V): Set<Pair<V, V>>? =
        traverse(
            listOf(start),
            { path, dst -> path + dst },
            { path -> path.last() },
            finish to { path -> path.windowedPairs().toSet() },
            { null })

    fun areConnected(start: V, finish: V): Boolean =
        traverse(
            start, { _, dst -> dst }, { it },
            finish to { true },
            { false })

    fun findSccSize(start: V): Int =
        traverse(
            start, { _, dst -> dst }, { it },
            null,
            { it.size })

    inline fun <T> withoutEdges(edges: Collection<Pair<V, V>>, action: () -> T): T {
        for ((s, d) in edges) {
            this.edges[s]!! -= d
            this.edges[d]!! -= s
        }
        try {
            return action()
        } finally {
            for ((s, d) in edges) {
                this.edges[s]!! += d
                this.edges[d]!! += s
            }
        }
    }
}
