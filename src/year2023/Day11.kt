package year2023

import utils.*
import utils.Array2D

@Suppress("DEPRECATION")
fun main() = test(
    { solve(it, 2) },
    { solve(it, 1000000) },
)

private fun solve(input: List<String>, expansion: Int): Long {
    val map = Array2D.fromLines(input)
    val emptyCols = collectEmptyLines(map.cols)
    val emptyRows = collectEmptyLines(map.rows)

    val points = map.rows.withIndex().flatMap { (row, line) ->
        line.withIndex().filter { it.value == '#' }.map { Pair(row, it.index) }
    }.toList()

    val totalDistance =
        (0 until points.size).sumOf { i1 ->
            val p1 = points[i1]
            ((i1 + 1) until points.size).sumOf { i2 ->
                val p2 = points[i2]
                (countDistance(p1.first, p2.first, emptyRows, expansion) +
                        countDistance(p1.second, p2.second, emptyCols, expansion)).toLong()
            }
        }

    return totalDistance
}

private fun collectEmptyLines(lines: List<List<Char>>): Set<Int> {
    return lines.withIndex()
        .filter { it.value.all { it == '.' } }
        .map { it.index }.toSet()
}

private fun rangeBetween(x: Int, y: Int): IntRange {
    return if (x <= y) {
        x until y
    } else {
        y until x
    }
}

private fun countDistance(p1: Int, p2: Int, emptyLines: Set<Int>, expansion: Int): Int {
    return rangeBetween(p1, p2).sumOf { if (it in emptyLines) expansion else 1 }
}