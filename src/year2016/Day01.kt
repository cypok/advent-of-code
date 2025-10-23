package year2016

import utils.*
import kotlin.math.absoluteValue

// Task description:
//   https://adventofcode.com/2016/day/1

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Int {
    val end = getMovement(input).last()
    return end.i.absoluteValue + end.j.absoluteValue
}

private fun solve2(input: List<String>): Int {
    val visited = mutableSetOf<Point>()
    for (pos in getMovement(input)) {
        if (!visited.add(pos)) {
            return pos.i.absoluteValue + pos.j.absoluteValue
        }
    }
    shouldNotReachHere()
}

private fun getMovement(input: List<String>): Sequence<Point> = sequence {
    var pos = Point(0, 0)
    var dir = Dir.UP

    yield(pos)
    for (cmd in input[0].split(", ")) {
        dir = when (cmd[0]) {
            'R' -> dir.right
            'L' -> dir.left
            else -> error(cmd)
        }
        val steps = cmd.substring(1).toInt()
        repeat(steps) {
            pos = pos.moveInDir(dir)
            yield(pos)
        }
    }
}