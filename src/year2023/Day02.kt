package year2023

import utils.*
import kotlin.math.max

private data class RGB(
    val red: Int,
    val green: Int,
    val blue: Int
)

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Int {
    return input.sumOf { line ->
        val match = Regex("""Game (\d+): (.*)""").matchEntire(line)!!
        val gameId = match.groupValues[1].toInt()
        val sets = match.groupValues[2]
        val isPossible = sets.splitToSequence("; ").all { set ->
            set.splitToSequence(", ").all { group ->
                val (numStr, colorStr) = group.words()
                val num = numStr.toInt()
                when (colorStr) {
                    "red" -> num <= 12
                    "green" -> num <= 13
                    "blue" -> num <= 14
                    else -> shouldNotReachHere()
                }
            }
        }
        if (isPossible) gameId else 0
    }
}

private fun solve2(input: List<String>): Int {
    return input.sumOf { line ->
        val sets = line.substringAfter(": ")
        val res = sets.splitToSequence("; ").fold(RGB(0, 0, 0)) { acc, set ->
            set.splitToSequence(", ").fold(acc) { acc, group ->
                val (numStr, colorStr) = group.words()
                val num = numStr.toInt()
                when (colorStr) {
                    "red"   -> acc.copy(red   = max(acc.red,   num))
                    "green" -> acc.copy(green = max(acc.green, num))
                    "blue"  -> acc.copy(blue  = max(acc.blue,  num))
                    else -> error(colorStr)
                }
            }
        }
        res.red * res.blue * res.green
    }
}
