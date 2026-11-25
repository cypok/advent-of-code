package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/22

fun main() = runAoc {
    example {
        answer1(37327623)
        """
            1
            10
            100
            2024
        """
    }
    example {
        answer2(23)
        """
            1
            2
            3
            2024
        """
    }
    solution {
        fun secretNumbers(initial: Int) = generateSequence(initial) { n ->
            var r = n
            val secretMask = (1 shl 24) - 1
            r = r xor (r shl 6) and secretMask
            r = r xor (r ushr 5) and secretMask
            r = r xor (r shl 11) and secretMask
            r
        }.take(1 + 2000)

        val buyerSecrets = lines.map { it.toInt() }

        if (isPart1) {
            buyerSecrets.sumOf { secretNumbers(it).last().toLong() }

        } else {
            data class DeltasSeq(val d1: Int, val d2: Int, val d3: Int, val d4: Int)

            fun generateDeltasSeqs(initialSecret: Int): Sequence<Pair<DeltasSeq, Int>> =
                secretNumbers(initialSecret)
                    .map { it.mod(10) }
                    .windowed(5)
                    .map { [a, b, c, d, e] ->
                        DeltasSeq(b - a, c - b, d - c, e - d) to e
                    }
                    // ensure that only the first occurrence is used
                    .distinctBy { it.first }

            buildMap {
                for (s in buyerSecrets) {
                    for ([ds, p] in generateDeltasSeqs(s)) {
                        put(ds, p + (get(ds) ?: 0))
                    }
                }
            }.values.max()
        }
    }
}