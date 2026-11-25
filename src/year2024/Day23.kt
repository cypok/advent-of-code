package year2024

import org.jgrapht.alg.clique.BronKerboschCliqueFinder
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import utils.*

// Task description:
//   https://adventofcode.com/2024/day/23

fun main() = runAoc {
    example {
        answer1(7)
        answer2("co,de,ka,ta")
        """
            kh-tc
            qp-kh
            de-cg
            ka-co
            yn-aq
            qp-ub
            cg-tb
            vc-aq
            tb-ka
            wh-tc
            yn-cg
            kh-ub
            ta-co
            de-co
            tc-td
            tb-wq
            wh-td
            ta-ka
            td-qp
            aq-cg
            wq-ub
            ub-vc
            de-ta
            wq-aq
            wq-vc
            wh-yn
            ka-de
            kh-ta
            co-tc
            wh-qp
            tb-vc
            td-yn
        """
    }
    solution1 {
        val connections = buildMap<String, MutableSet<String>> {
            for (line in lines) {
                val f = line.substringBefore('-')
                val t = line.substringAfter('-')
                getOrPut(f) { mutableSetOf() } += t
                getOrPut(t) { mutableSetOf() } += f
            }
        }

        val triplets = mutableSetOf<List<String>>()
        for ([a, bs] in connections) {
            for (b in bs) {
                for (c in connections[b]!!) {
                    if (c in bs) {
                        triplets += listOf(a, b, c).sorted()
                    }
                }
            }
        }
        triplets.filter { it.any { it.startsWith('t') } }.size
    }
    solution2 {
        val graph = SimpleGraph<String, DefaultEdge>(DefaultEdge::class.java)
        for (line in lines) {
            val f = line.substringBefore('-')
            val t = line.substringAfter('-')
            graph.addVertex(f)
            graph.addVertex(t)
            graph.addEdge(f, t)
        }

        val cliqueFinder = BronKerboschCliqueFinder(graph)
        cliqueFinder.maxBy { it.size }.sorted().joinToString(",")
    }
}