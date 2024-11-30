package year2017

import utils.*
import java.util.Collections

// Task description:
//   https://adventofcode.com/2017/day/1

fun main() = test(
    { solve(it, { _ -> 1 }) },
    { solve(it, { l -> l / 2 }) },
)

private fun solve(input: List<String>, shiftProvider: (Int) -> Int): Int {
    val digits = input[0].map { it.digitToInt() }
    val shift = shiftProvider(digits.size)
    val digitsShifted = digits.toMutableList()
    Collections.rotate(digitsShifted, shift)

    return digits.zip(digitsShifted).sumOf { (x, y) -> if (x == y) x else 0 }
}