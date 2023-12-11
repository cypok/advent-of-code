import AreaState.*
import kotlin.IllegalStateException

fun main() = test(
    ::solve,
)

private fun solve(input: List<String>): Pair<Long, Long> {
    val map = StringArray2D(input)

    for (i in 0 until map.height) {
        for (j in 0 until map.width) {
            if (map[i, j] == 'S') {
                return startAt(map, i, j)
            }
        }
    }

    throw IllegalArgumentException("start not found")
}

private enum class Dir {
    UP, DOWN, LEFT, RIGHT;

    fun opposite() =
        when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
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

private fun moveInDir(row: Int, col: Int, dir: Dir) =
    when (dir) {
        Dir.UP -> Pair(row - 1, col)
        Dir.DOWN -> Pair(row + 1, col)
        Dir.LEFT -> Pair(row, col - 1)
        Dir.RIGHT -> Pair(row, col + 1)
    }

private fun startAt(map: StringArray2D, startRow: Int, startCol: Int): Pair<Long, Long> {
    val borderMap = Array(map.height) { Array(map.width) { false } }
    for (dir in Dir.entries) {
        followPipes(map, borderMap, startRow, startCol, dir, 0, allowInvalid = true)?.let { (length, returnDir) ->
            val maxDist = (length + 1) / 2
            val startDirs = setOf(dir, returnDir.opposite())
            val startPipe = pipes.entries.find { it.value == startDirs }!!.key
            val area = calcInsideArea(map, borderMap, startPipe)
            return Pair(maxDist, area)
        }
    }

    throw IllegalStateException("no pipe connection to start is found")
}

private tailrec fun followPipes(map: StringArray2D, borderMap: Array<Array<Boolean>>, row: Int, col: Int, dir: Dir, length: Long, allowInvalid: Boolean = false): Pair<Long, Dir>? {
    val (nextRow, nextCol) = moveInDir(row, col, dir)

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
    val dirAsOut = dir.opposite()
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

private fun calcInsideArea(map: StringArray2D, borderMap: Array<Array<Boolean>>, startPipe: Char): Long {
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
                else -> throw IllegalStateException(state.toString())
            }

        'L' ->
            when (state) {
                OUTSIDE -> LOWER_BORDER
                INSIDE -> UPPER_BORDER
                else -> throw IllegalStateException(state.toString())
            }

        'F' ->
            when (state) {
                OUTSIDE -> UPPER_BORDER
                INSIDE -> LOWER_BORDER
                else -> throw IllegalStateException(state.toString())
            }

        'J' ->
            when (state) {
                UPPER_BORDER -> INSIDE
                LOWER_BORDER -> OUTSIDE
                else -> throw IllegalStateException(state.toString())
            }

        '7' ->
            when (state) {
                UPPER_BORDER -> OUTSIDE
                LOWER_BORDER -> INSIDE
                else -> throw IllegalStateException(state.toString())
            }

        else -> throw IllegalStateException(pipe.toString())
    }
}
