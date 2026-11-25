package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/19

fun main() = runAoc {
    example {
        answer1(6)
        answer2(16)
        """
            r, wr, b, g, bwu, rb, gb, br

            brwrr
            bggr
            gbbr
            rrbgbr
            ubwu
            bwurrg
            brgr
            bbrgwb
        """
    }
    example("tricky end") {
        answer1(1)
        answer2(1)
        """
            abc

            ab
            abc
            abcd
        """
    }
    example("hard artificial") {
        answer1(0)
        answer2(0)
        """
            a, aa

            aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaax
        """
    }
    solution {
        val [availableTowelsStr, designs] = lines.splitByEmptyLines().toList()
        val allTowels = PrefixTree.make(availableTowelsStr.single().split(", ").toList())

        fun isPossible(design: String): Boolean {
            val cache = mutableMapOf<Int, Boolean>()

            fun check(offset: Int, towels: PrefixTree): Boolean {
                if (offset == design.length) {
                    return towels.hasValue() || towels === allTowels
                }

                if (towels.hasValue() && cache.getOrPut(offset) { check(offset, allTowels) }) {
                    return true
                }

                val subTowels = towels.children[design[offset]]
                return subTowels != null && check(offset + 1, subTowels)
            }

            return check(0, allTowels)
        }

        fun countWays(design: String): Long {
            val cache = mutableMapOf<Int, Long>()

            fun count(offset: Int, towels: PrefixTree): Long {
                if (offset == design.length) {
                    return if (towels.hasValue() || towels === allTowels) 1 else 0
                }

                var ways = 0L

                if (towels.hasValue()) {
                    ways += cache.getOrPut(offset) { count(offset, allTowels) }
                }

                val subTowels = towels.children[design[offset]]
                if (subTowels != null) {
                    ways += count(offset + 1, subTowels)
                }

                return ways
            }

            return count(0, allTowels)
        }

        if (isPart1) {
            designs.count { isPossible(it) }
        } else {
            designs.sumOf { countWays(it) }
        }
    }
}

private class PrefixTree(val value: String?, val children: Map<Char, PrefixTree>) {

    fun hasValue() = value != null

    companion object {
        fun make(strs: List<String>): PrefixTree = make(strs, 0)

        private fun make(strs: List<String>, offset: Int): PrefixTree =
            PrefixTree(
                strs.find { it.length == offset },
                strs
                    .filter { it.length > offset }
                    .groupBy { it[offset] }
                    .mapValues { make(it.value, offset + 1) })

    }
}