package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/4

fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Long {
    val xmasChars1 = "XMAS".toCharArray().toList()
    val xmasChars2 = xmasChars1.reversed()

    val map = StringArray2D(input)
    return (map.rows + map.cols + map.diagonalsLeft + map.diagonalsRight).sumOf { chars ->
        chars.windowed(4).count { it == xmasChars1 || it == xmasChars2 }.toLong()
    }
}

private fun solve2(input: List<String>): Long {
    val map = StringArray2D(input)
    return (1 ..< map.width-1).sumOf { j ->
        (1 ..< map.height-1).sumOf { i ->
            val middle = map[j, i]
            if (middle != 'A') return@sumOf 0L

            val nw = map[j-1, i-1]
            val ne = map[j+1, i-1]
            val sw = map[j-1, i+1]
            val se = map[j+1, i+1]
            if (setOf(nw, ne, sw, se) != setOf('M', 'S')) return@sumOf 0L

            if (nw == ne && sw == se || nw == sw && ne == se) 1L else 0L
        }
    }
}
