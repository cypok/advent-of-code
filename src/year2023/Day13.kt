package year2023

import utils.*
import utils.Array2D

@Suppress("DEPRECATION")
fun main() = test(
    { solve(it, 0) },
    { solve(it, 1) },
)

private fun solve(input: List<String>, expectedSmudgesCount: Int): Long {
    return input.splitByEmptyLines().sumOf { map ->
        when (val ref = solveOne(Array2D.fromLines(map.toList()), expectedSmudgesCount)) {
            is Reflection.Vertical -> ref.col
            is Reflection.Horizontal -> ref.row * 100
        }.toLong()
    }
}

private sealed class Reflection {
    data class Vertical(val col: Int) : Reflection()
    data class Horizontal(val row: Int) : Reflection()
}

private fun solveOne(map: Array2D<Char>, expectedSmudgesCount: Int): Reflection {
    fun tryDimension(width: Int, rows: List<List<Char>>): Int? =
        (1 until width).firstOrNull { colNum ->
            rows.sumOf { row -> countSmudges(row, colNum) } == expectedSmudgesCount
        }

    return tryDimension(map.width, map.rows)?.let { Reflection.Vertical(it) } ?:
           tryDimension(map.height, map.cols)?.let { Reflection.Horizontal(it) } ?:
           shouldNotReachHere()
}

private fun countSmudges(row: List<Char>, colNum: Int): Int {
    // We might be able to check smudges overflow earlier, but it isn't required.
    val leftPart = row.slice(colNum - 1 downTo 0)
    val rightPart = row.slice(colNum until row.size)
    return (leftPart zip rightPart).count { (l, r) -> l != r }
}
