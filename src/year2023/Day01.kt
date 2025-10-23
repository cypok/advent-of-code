package year2023

import utils.*

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Int {
    return input.sumOf { line ->
        val (fst, lst) = line.fold(Pair(-1, -1)) { acc, ch ->
            if (ch.isDigit()) {
                var (fst, _) = acc
                val lst = ch.digitToInt()
                if (fst == -1) {
                    fst = lst
                }
                Pair(fst, lst)
            } else {
                acc
            }
        }
        fst * 10 + lst
    }
}

private fun solve2(input: List<String>): Int {
    fun digitToInt(digit: String): Int {
        return if (digit.length == 1) {
            digit[0].digitToInt()
        } else {
            when (digit) {
                "one" -> 1
                "two" -> 2
                "three" -> 3
                "four" -> 4
                "five" -> 5
                "six" -> 6
                "seven" -> 7
                "eight" -> 8
                "nine" -> 9
                else -> error(digit)
            }
        }
    }

    fun findDigit(regex: Regex, line: String): Int {
        val m = regex.matchAt(line, 0)
        check(m != null)
        return digitToInt(m.groups[1]!!.value)
    }

    return input.sumOf { line ->
        val digit = "([0-9]|one|two|three|four|five|six|seven|eight|nine)"
        val fstRegex = Regex(".*?$digit")
        val lstRegex = Regex(".*$digit")
        10 * findDigit(fstRegex, line) + findDigit(lstRegex, line)
    }
}
