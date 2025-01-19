package year2023

import utils.*
import year2023.AreaState.*

fun main() = test(
    { solve(it).first },
    { solve(it).second },
)

private fun solve(input: List<String>): Pair<Long, Long> {
    val map = StringArray2D(input)
    map.find('S').let { (i, j) ->
        return startAt(map, i, j)
    }
}

private val pipes = mapOf(
    '|' to setOf(Dir.UP, Dir.DOWN),
    '-' to setOf(Dir.LEFT, Dir.RIGHT),
    'L' to setOf(Dir.UP, Dir.RIGHT),
    'J' to setOf(Dir.UP, Dir.LEFT),
    '7' to setOf(Dir.DOWN, Dir.LEFT),
    'F' to setOf(Dir.DOWN, Dir.RIGHT),
)

private fun parsePipe(ch: Char): Set<Dir> =
    pipes[ch].orEmpty()

private fun startAt(map: Array2D<Char>, startRow: Int, startCol: Int): Pair<Long, Long> {
    val borderMap = Array2D.ofBooleans(map.height, map.width, false)
    for (dir in Dir.entries) {
        followPipes(map, borderMap, startRow, startCol, dir, 0, allowInvalid = true)?.let { (length, returnDir) ->
            val maxDist = (length + 1) / 2
            val startDirs = setOf(dir, returnDir.opposite)
            val startPipe = pipes.entries.find { it.value == startDirs }!!.key
            val area = calcInsideArea(map, borderMap, startPipe)
            return Pair(maxDist, area)
        }
    }

    error("no pipe connection to start is found")
}

private tailrec fun followPipes(
    map: Array2D<Char>,
    borderMap: Array2D<Boolean>,
    row: Int,
    col: Int,
    dir: Dir,
    length: Long,
    allowInvalid: Boolean = false
): Pair<Long, Dir>? {
    val (nextRow, nextCol) = (row x col).moveInDir(dir)

    val cell = map.getOrNull(nextRow, nextCol)
    if (cell == null) {
        assert(allowInvalid)
        return null
    }
    if (cell == 'S') {
        borderMap[nextRow][nextCol] = true
        return Pair(length, dir)
    }

    val nextOutDirs = parsePipe(cell)
    val dirAsOut = dir.opposite
    if (dirAsOut !in nextOutDirs) {
        assert(allowInvalid)
        return null
    }

    borderMap[nextRow][nextCol] = true
    val nextDir = (nextOutDirs - dirAsOut).single()
    return followPipes(map, borderMap, nextRow, nextCol, nextDir, length + 1)
}

private enum class AreaState {
    OUTSIDE, UPPER_BORDER, LOWER_BORDER, INSIDE,
}

private fun calcInsideArea(map: Array2D<Char>, borderMap: Array2D<Boolean>, startPipe: Char): Long {
    var area = 0L
    for (i in 0 until map.height) {
        var state = OUTSIDE
        for (j in 0 until map.width) {
            if (borderMap[i][j]) {
                val pipe = when (val origPipe = map[i, j]) {
                    'S' -> startPipe
                    else -> origPipe
                }
                state = changeStateOnBorder(pipe, state)

            } else if (state == INSIDE) {
                area++
            }
        }
        assert(state == OUTSIDE)
    }
    return area
}

private fun changeStateOnBorder(pipe: Char, state: AreaState): AreaState {
    return when (pipe) {
        '-' -> state
        '|' ->
            when (state) {
                OUTSIDE -> INSIDE
                INSIDE -> OUTSIDE
                else -> error(state)
            }

        'L' ->
            when (state) {
                OUTSIDE -> LOWER_BORDER
                INSIDE -> UPPER_BORDER
                else -> error(state)
            }

        'F' ->
            when (state) {
                OUTSIDE -> UPPER_BORDER
                INSIDE -> LOWER_BORDER
                else -> error(state)
            }

        'J' ->
            when (state) {
                UPPER_BORDER -> INSIDE
                LOWER_BORDER -> OUTSIDE
                else -> error(state)
            }

        '7' ->
            when (state) {
                UPPER_BORDER -> OUTSIDE
                LOWER_BORDER -> INSIDE
                else -> error(state)
            }

        else -> error(pipe)
    }
}
