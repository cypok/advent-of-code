package year2023

import utils.*

fun main() = test(
    ::solve1,
    ::solve2,
)

private val ds = listOf(-1, 0, 1)
private val neighbors = ds.flatMap { d1 -> ds.map { d2 -> Pair(d1, d2) } }
    .filter { (d1, d2) -> d1 != 0 || d2 != 0}

private fun solve1(input: List<String>): Int {
    var sum = 0
    val width = input.first().length
    for (i in input.indices) {
        var curNum = 0
        var isPart = false
        for (j in 0.until(width + 1)) {
            val c = if (j < width) input[i][j] else '.'
            if (!c.isDigit()) {
                if (curNum != 0 && isPart) {
                    sum += curNum
                }
                curNum = 0
                isPart = false
            } else {
                curNum = curNum * 10 + c.digitToInt()
                if (!isPart) {
                    for (n in neighbors) {
                        val ii = i + n.first
                        val jj = j + n.second
                        if (0 <= ii && ii < input.size && 0 <= jj && jj < width) {
                            val cc = input[ii][jj]
                            if (!cc.isDigit() && cc != '.') {
                                isPart = true
                                break
                            }
                        }
                    }
                }
            }
        }
    }
    return sum
}

private fun solve2(input: List<String>): Int {
    val gears = mutableMapOf<Pair<Int, Int>, Int>()

    var gearsSum = 0
    val width = input.first().length
    for (i in input.indices) {
        var curNum = 0
        var isPart = false
        var geared: Pair<Int, Int>? = null
        for (j in 0.until(width + 1)) {
            val c = if (j < width) input[i][j] else '.'
            if (!c.isDigit()) {
                if (curNum != 0 && isPart) {
                    if (geared != null) {
                        when (val prev = gears[geared]) {
                            null ->
                                gears.put(geared, curNum)
                            else ->
                                gearsSum += prev * curNum
                        }
                    }
                }
                curNum = 0
                isPart = false
                geared = null
            } else {
                curNum = curNum * 10 + c.digitToInt()
                if (!isPart) {
                    for (n in neighbors) {
                        val ii = i + n.first
                        val jj = j + n.second
                        if (0 <= ii && ii < input.size && 0 <= jj && jj < width) {
                            val cc = input[ii][jj]
                            if (cc == '*') {
                                geared = Pair(ii, jj)
                            }
                            if (!cc.isDigit() && cc != '.') {
                                isPart = true
                                break
                            }
                        }
                    }
                }
            }
        }
    }
    return gearsSum
}
