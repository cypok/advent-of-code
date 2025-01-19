package year2023

import utils.*

fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Long {
    return parseSteps(input).sumOf { hash(it).toLong() }
}

private fun parseSteps(input: List<String>): List<String> =
    input.single().split(',')

private const val N = 256

private fun hash(str: String): Int =
    str.fold(0) { acc, ch -> (acc + ch.code) * 17 % N }

private val STEP_REGEX = """(.*)([=-])(\d*)""".toRegex()

private fun solve2(input: List<String>): Long {
    val steps = parseSteps(input)
    val boxes = Array<MutableList<Pair<String, String>>>(N) { mutableListOf() }
    for (step in steps) {
        val (label, op, focalLen) = STEP_REGEX.matchEntire(step)!!.destructured
        val boxIdx = hash(label)
        val lenses = boxes[boxIdx]
        when (op) {
            "=" -> {
                val elem = Pair(label, focalLen)
                when (val inBoxIdx = lenses.indexOfFirst { it.first == label }) {
                    -1 -> lenses += elem
                    else -> lenses[inBoxIdx] = elem
                }
            }
            "-" -> {
                lenses.removeIf { it.first == label }
            }
            else -> error(step)
        }
    }
    return boxes.withIndex().sumOf { (boxIdx, lenses) ->
        lenses.withIndex().sumOf { (inBoxIdx, elem) ->
            (boxIdx + 1) * (inBoxIdx + 1) * elem.second.toLong()
        }
    }
}