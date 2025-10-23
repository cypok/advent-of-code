package year2023

import utils.*
import utils.Dir.*

@Suppress("DEPRECATION")
fun main() = test(
    { solve(it.map(::parseLinePart1)) },
    { solve(it.map(::parseLinePart2)) },
)

private val INPUT_REGEX_PART1 = """(.) (\d+) .*""".toRegex()

private fun parseLinePart1(line: String): Pair<Int, Dir> {
    val (dirCh, len) = INPUT_REGEX_PART1.matchEntire(line)!!.destructured
    return Pair(len.toInt(), Dir.fromChar(dirCh[0]))
}

private val INPUT_REGEX_PART2 = """. \d+ \(#(.....)(.)\)""".toRegex()
private val dirCodeToDir = "RDLU".map { Dir.fromChar(it) }

private fun parseLinePart2(line: String): Pair<Int, Dir> {
    val (lenHex, dirCode) = INPUT_REGEX_PART2.matchEntire(line)!!.destructured
    val dir = dirCodeToDir[dirCode.toInt()]
    return Pair(lenHex.toInt(16), dir)
}

private fun solve(commands: List<Pair<Int, Dir>>): Long {
    return commands.fold(Pair(0L, 1L)) { (offset, area), (len, dir) ->
        when (dir) {
            RIGHT -> Pair(offset + len, area + len)
            LEFT  -> Pair(offset - len, area)
            DOWN  -> Pair(offset,       area + len * (offset + 1))
            UP    -> Pair(offset,       area - len * offset)
        }
    }.second
}