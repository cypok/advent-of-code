package year2024

import utils.*

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
    val map = StringArray2D(input)
    var curPos = findGuard(map)
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
    val map = StringArray2D(input)
    val originalGuardPos = findGuard(map)

    var curPos = originalGuardPos
    var curDir = Dir.UP

    val possibleObstructions = mutableSetOf<Point>()
    val visited = mutableSetOf(curPos)
    while (true) {
        val nextPos = curPos.moveInDir(curDir)
        val nextField = map.getOrNull(nextPos)
        when (nextField) {
            null -> break
            '#' -> {
                curDir = curDir.right
            }
            else -> {
                if (nextPos !in visited &&
                    nextPos !in possibleObstructions &&
                    wouldLoop(map, nextPos, curPos, curDir)) {
                    possibleObstructions += nextPos
                }
                curPos = nextPos
                visited += curPos
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
    val map = StringArray2D(input)
    val originalGuardPos = findGuard(map)
    val originalGuardDir = Dir.UP

    return (0..<map.height).sumOf { i ->
        (0..<map.width).sumOf { j ->
            val pos = i x j
            val field = map[pos]
            if (field != '#' && field != '^' &&
                wouldLoop(map, pos, originalGuardPos, originalGuardDir)) {
                1
            } else {
                @Suppress("USELESS_CAST") // KT-46360
                0 as Int
            }
        }
    }
}

private fun wouldLoop(map: StringArray2D, extraObstruction: Point,
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

private fun findGuard(map: StringArray2D): Point {
    for (i in 0..<map.height) {
        for (j in 0..<map.width) {
            if (map[i, j] == '^') {
                return i x j
            }
        }
    }
    shouldNotReachHere()
}