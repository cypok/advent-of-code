package year2024

import kotlinx.coroutines.*
import utils.*
import utils.Array2D
import java.util.concurrent.ConcurrentHashMap

// Task description:
//   https://adventofcode.com/2024/day/6

fun main() = runAoc {
    example("from task") {
        answer1(41)
        answer2(6)
        """
            ....#.....
            .........#
            ..........
            ..#.......
            .......#..
            ..........
            .#..^.....
            ........#.
            #.........
            ......#...
        """
    }
    example {
        answer2(1)
        """
            ..........
            ..........
            ..........
            ....#.....
            ........#.
            ..........
            ....^.....
            ..........
            ...?......
            .......#..
        """
    }
    example {
        answer2(1)
        """
            ..........
            ..........
            ..........
            ....#.....
            ........#.
            ..........
            ....^.....
            ..........
            ...#......
            .......?..
        """
    }
    example { // Obstruction should not be placed at guard's initial position.
        answer2(2)
        """
            .#..#.....
            ........#.
            #.........
            .......#..
            ...#......
            .........#
            ?.?^......
            ........#.
            ..........
            ..........
        """
    }
    example { // Obstruction should not be placed on the old path (X):
        answer2(1)
        """
            ....#.....
            ...#....#.
            ........#.
            ..?X......
            .......#..
            ..........
            ...^......
            ..........
            ..........
            ..........
        """
    }

    solution1 { solve1(lines) }
    solution2 { solve2(lines) }
}

private fun solve1(input: List<String>): Int {
    val map = Array2D.fromLines(input)
    var curPos = map.find('^')
    var curDir = Dir.UP

    val visited = mutableSetOf<Point>()
    while (true) {
        visited += curPos
        val nextPos = curPos.moveInDir(curDir)
        val nextField = map.getOrNull(nextPos)
        when (nextField) {
            null -> break
            '#' -> curDir = curDir.right
            else -> curPos = nextPos
        }
    }

    return visited.size
}

private fun solve2(input: List<String> ): Int {
    val map = Array2D.fromLines(input)
    val originalGuardPos = map.find('^')

    var curPos = originalGuardPos
    var curDir = Dir.UP

    val possibleObstructions = ConcurrentHashMap<Point, Boolean>()
    val visited = mutableSetOf(curPos)
    runBlocking(Dispatchers.Default) {
        fun tryLoopAsync(obstructionPos: Point, startPos: Point, startDir: Dir) = launch {
            if (wouldLoop(map, obstructionPos, startPos, startDir)) {
                possibleObstructions[obstructionPos] = true
            }
        }

        while (true) {
            val nextPos = curPos.moveInDir(curDir)
            val nextField = map.getOrNull(nextPos)
            when (nextField) {
                null -> break
                '#' -> {
                    curDir = curDir.right
                }

                else -> {
                    if (nextPos !in visited && !possibleObstructions.contains(nextPos)) {
                        tryLoopAsync(nextPos, curPos, curDir)
                    }
                    curPos = nextPos
                    visited += curPos
                }
            }
        }
    }

    return possibleObstructions.size.also {
        if (false) {
            // A bit slow. :(
            assert(it == solve2BruteForce(input))
        }
    }
}

private fun solve2BruteForce(input: List<String>): Int {
    val map = Array2D.fromLines(input)
    val originalGuardPos = map.find('^')
    val originalGuardDir = Dir.UP

    return map.valuesIndexed.sumOf { (field, pos) ->
        if (field != '#' && field != '^' &&
            wouldLoop(map, pos, originalGuardPos, originalGuardDir)) {
            1
        } else {
            @Suppress("USELESS_CAST") // KT-46360
            0 as Int
        }
    }
}

private fun wouldLoop(map: Array2D<Char>, extraObstruction: Point,
                      startPos: Point, startDir: Dir): Boolean {
    var curPos = startPos
    var curDir = startDir

    val visited = mutableSetOf<Pair<Point, Dir>>()
    while (true) {
        if (!visited.add(curPos to curDir)) {
            return true
        }

        val nextPos = curPos.moveInDir(curDir)
        val nextField = if (nextPos == extraObstruction) '#' else map.getOrNull(nextPos)
        when (nextField) {
            null -> return false
            '#' -> curDir = curDir.right
            else -> curPos = nextPos
        }
    }
}
