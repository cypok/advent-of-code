package year2023

import utils.*
import kotlin.math.max
import kotlin.math.min

@Suppress("DEPRECATION")
fun main() = test(
    ::solve,
)

private fun solve(input: List<String>): Long {
    val g = Graph(input)

    for (e1 in g.edges) {
        val (start, finish) = e1
        g.withoutEdge(e1) {
            val path23 = g.findPath(start, finish)!!
            for (e2 in path23) {
                g.withoutEdge(e2) {
                    val path3 = g.findPath(start, finish)!!
                    for (e3 in path3) {
                        g.withoutEdge(e3) {
                            if (!g.areConnected(start, finish)) {
                                return 1L * g.findSccSize(start) * g.findSccSize(finish)
                            }
                        }
                    }
                }
            }
        }
    }

    shouldNotReachHere()
}

private class Graph(input: List<String>) {
    val nodes: Set<String>

    val nameByIdx: List<String>
    val idxByName: Map<String, Int>

    val edges: List<Pair<Int, Int>>
    val edgesByNodeMutable: Array<MutableSet<Int>>

    init {
        val parsed = input.map {
            val src = it.substringBefore(": ")
            val dsts = it.substringAfter(": ").words()
            Pair(src, dsts)
        }
        nodes = buildSet<String> {
            for ((src, dsts) in parsed) {
                add(src)
                addAll(dsts)
            }
        }
        nameByIdx = nodes.toList().sorted()
        idxByName = nameByIdx.withIndex().associate { (idx, node) -> node to idx }
        edges = mutableListOf()
        val connectionMatrix = Array(nodes.size) { BooleanArray(nodes.size) }
        for ((src, dsts) in parsed) {
            val srcIdx = idxByName[src]!!
            for (dst in dsts) {
                val dstIdx = idxByName[dst]!!
                check(srcIdx != dstIdx)
                connectionMatrix[srcIdx][dstIdx] = true
                connectionMatrix[dstIdx][srcIdx] = true
                edges += min(srcIdx, dstIdx) to max(srcIdx, dstIdx)
            }
        }
        edgesByNodeMutable = Array(nodes.size) { srcIdx ->
            val connections = connectionMatrix[srcIdx]
            nodes.indices.filter { connections[it] }.toMutableSet()
        }
    }

    private inline fun <S, T> traverse(
        initState: S,
        nextState: (S, Int) -> S,
        nodeFromState: (S) -> Int,
        finish: Pair<Int, (S) -> T>?,
        onEnd: (BooleanArray) -> T
    ): T {
        val visited = BooleanArray(nodes.size)
        val next = ArrayDeque<S>()
        next += initState

        while (next.isNotEmpty()) {
            val s = next.removeFirst()
            val node = nodeFromState(s)

            if (visited[node]) continue
            visited[node] = true

            if (finish != null && node == finish.first) {
                return finish.second(s)
            }

            for (dst in edgesByNodeMutable[node]) {
                if (!visited[dst]) {
                    next += nextState(s, dst)
                }
            }
        }

        return onEnd(visited)
    }

    fun findPath(start: Int, finish: Int): Set<Pair<Int, Int>>? =
        traverse(
            listOf(start),
            { path, dst -> path + dst },
            { path -> path.last() },
            finish to { path -> path.windowedPairs().toSet() },
            { null })

    fun areConnected(start: Int, finish: Int): Boolean =
        traverse(
            start, { _, dst -> dst }, { it },
            finish to { true },
            { false })

    fun findSccSize(start: Int): Int =
        traverse(
            start, { _, dst -> dst }, { it },
            null,
            { it.count { it } })

    inline fun <T> withoutEdge(edge: Pair<Int, Int>, action: () -> T): T {
        val src = edge.first
        val dst = edge.second
        edgesByNodeMutable[src].without(dst) {
            edgesByNodeMutable[dst].without(src) {
                return action()
            }
        }
    }
}

inline fun <E, T> MutableSet<E>.without(elem: E, action: () -> T): T {
    check(this.remove(elem))
    try {
        return action()
    } finally {
        this.add(elem)
    }
}
