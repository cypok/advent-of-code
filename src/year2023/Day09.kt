package year2023

import utils.*

@Suppress("DEPRECATION")
fun main() = test(
    ::part1,
    ::part2,
)

private fun solve(input: List<String>, combine: (List<Long>, Long) -> Long): Long {
    fun extrapolate(seq: List<Long>): Long {
        if (seq.all { it == 0L }) return 0
        val step = extrapolate(seq.windowed(2).map { (a, b) -> b - a })
        return combine(seq, step)
    }
    return input.sumOf { line ->
        extrapolate(line.numbers())
    }
}

private fun part1(input: List<String>) =
    solve(input) { seq, step -> seq.last() + step }

private fun part2(input: List<String>) =
    solve(input) { seq, step -> seq.first() - step }
