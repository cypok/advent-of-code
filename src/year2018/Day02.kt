package year2018

import utils.*

// Task description:
//   https://adventofcode.com/2018/day/2

fun main() = runAoc {
    example {
        answer1(12)
        """
            abcdef
            bababc
            abbcde
            abcccd
            aabcdd
            abcdee
            ababab
        """
    }

    example {
        answer2("fgij")
        """
            abcde
            fghij
            klmno
            pqrst
            fguij
            axcye
            wvxyz
        """
    }

    solution1 {
        lines.fold(0 to 0) { (c2, c3), line ->
            val grouped = line.groupBy { it }
            fun has(count: Int): Int = if (grouped.any { it.value.size == count }) 1 else 0
            (c2 + has(2)) to (c3 + has(3))
        }.let { (c2, c3) -> c2 * c3 }
    }

    solution2 {
        fun diff(sa: String, sb: String) =
            (sa.asSequence() zip sb.asSequence())
                .count { (ca, cb) -> ca != cb }

        fun common(sa: String, sb: String) =
            (sa.asSequence() zip sb.asSequence())
                .filter { (ca, cb) -> ca == cb }
                .map { (ca, _) -> ca }
                .joinToString(separator = "")

        listOf(lines, lines).cartesianProduct()
            .find { (sa, sb) -> sa < sb && diff(sa, sb) == 1 }!!
            .let { (sa, sb) -> common(sa, sb) }
    }
}
