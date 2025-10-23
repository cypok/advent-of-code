package year2023

import utils.*
import utils.Array2D

fun main() = runAoc {
    solution1 { solve1() }
    solution2 { solve2() }
}

private fun SolutionContext.solve1(): Long {
    return Array2D.fromLines(lines).cols.sumOf { col ->
        col.withIndex().fold(Pair(0, 0L)) { acc, (i, ch) ->
            if (ch != 'O') return@fold acc

            val (nextEmpty, weight) = acc

            val emptySpot = 1 + ((i downTo nextEmpty)
                .firstOrNull { col[it] == '#' }
                ?: (nextEmpty - 1))

            Pair(emptySpot + 1, weight + (col.size - emptySpot).toLong())
        }.second
    }
}

private fun SolutionContext.solve2(): Long {
    val originalMap = Array2D.fromLines(lines)
    val height = originalMap.height
    val width = originalMap.width

    val map = Array(height) { i -> Array(width) { j -> originalMap[i, j] } }

    fun tilt(w: Int, h: Int, cellGet: (Int, Int) -> Char, cellSet: (Int, Int, Char) -> Unit) {
        for (j in 0 until w) {
            var nextPossible = 0
            for (i in 0 until h) {
                when (cellGet(i, j)) {
                    '.' -> {}
                    '#' -> nextPossible = i + 1
                    'O' -> {
                        for (reallyEmpty in nextPossible..<i) {
                            if (cellGet(reallyEmpty, j) == '.') {
                                cellSet(reallyEmpty, j, 'O')
                                cellSet(i, j, '.')
                                nextPossible = reallyEmpty + 1
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    fun tiltN() = tilt(width, height, { i, j -> map[i][j] }, { i, j, c -> map[i][j] = c } )
    fun tiltW() = tilt(height, width, { i, j -> map[j][i] }, { i, j, c -> map[j][i] = c } )
    fun tiltS() = tilt(width, height, { i, j -> map[height - i - 1][j] }, { i, j, c -> map[height - i - 1][j] = c } )
    fun tiltE() = tilt(height, width, { i, j -> map[j][width - i - 1] }, { i, j, c -> map[j][width - i - 1] = c } )

    fun tiltCycle() {
        tiltN()
        tiltW()
        tiltS()
        tiltE()
    }

    fun calcLoadN(): Long =
        (0 until height).sumOf { i ->
            (0 until width).sumOf { j ->
                (if (map[i][j] == 'O') height - i else 0).toLong()
            }
        }

    val measure = 2000

    val loads = CyclicState(-1L)

    for (i in 0 until measure) {
        tiltCycle()
        loads.current = calcLoadN()
        loads.tick(i.toLong())
    }

    val cycle = loads.detectCycle() ?: error("cycle not found")
    val realLastLoad = loads.extrapolateUntil(1_000_000_000)

    printExtra("cycle = $cycle")
    return realLastLoad
}