package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/11

fun main() = runAoc {
    example {
        answer1(5)
        """
            aaa: you hhh
            you: bbb ccc
            bbb: ddd eee
            ccc: ddd eee fff
            ddd: ggg
            eee: out
            fff: out
            ggg: out
            hhh: ccc fff iii
            iii: out
        """
    }

    example {
        answer2(2)
        """
            svr: aaa bbb
            aaa: fft
            fft: ccc
            bbb: tty
            tty: ccc
            ccc: ddd eee
            ddd: hub
            hub: fff
            eee: dac
            dac: fff
            fff: ggg hhh
            ggg: out
            hhh: out
        """
    }

    solution {
        fun printGraph(str: String) {
            if (false) printExtra(str)
        }

        printGraph("digraph G {")

        val f = "out"
        val s1 = "you"
        val s2 = "svr"
        val mA = "dac"
        val mB = "fft"
        listOf(
            "green" to listOf(s1, s2),
            "blue" to listOf(mA, mB),
            "red" to listOf(f),
        ).forEach { (c, ns) ->
            ns.forEach { n ->
                printGraph("  $n [color=$c,style=filled];")
            }
        }

        val edges = mutableMapOf<String, MutableList<String>>()
        lines.forEach { line ->
            val src = line.substringBefore(":")
            val nodes = line.substringAfter(":").words()
            nodes.forEach { dst ->
                edges.getOrPut(src) { mutableListOf<String>() } += dst
                edges.getOrPut(dst) { mutableListOf<String>() }
                printGraph("  $src -> $dst;")
            }
        }
        printGraph("}")

        fun paths(start: String, finish: String): Long {
            val visited = mutableMapOf(finish to 1L)
            fun dfs(v: String): Long {
                visited[v]?.let { return it }
                return edges[v]!!.sumOf {
                    dfs(it)
                }.also {
                    visited[v] = it
                }
            }
            return dfs(start)
        }

        if (isPart1) {
            paths(s1, f)
        } else {
            listOf(listOf(s2, mA, mB, f), listOf(s2, mB, mA, f)).map { points ->
                points.windowedPairs().productOf { (start, finish) -> paths(start, finish) }
            }.single { it != 0L }
        }
    }
}
