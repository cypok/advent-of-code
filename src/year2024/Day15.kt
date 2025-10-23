package year2024

import utils.*
import utils.Array2D

// Task description:
//   https://adventofcode.com/2024/day/15

fun main() = runAoc {
    example {
        answer1(2028)
        """
            ########
            #..O.O.#
            ##@.O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########

            <^^>>>vv<v>>v<<
        """
    }
    example {
        answer1(10092)
        answer2(9021)
        """
            ##########
            #..O..O.O#
            #......O.#
            #.OO..O.O#
            #..O@..O.#
            #O#..O...#
            #O..O..O.#
            #.OO.O.OO#
            #....O...#
            ##########

            <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
            vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
            ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
            <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
            ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
            ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
            >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
            <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
            ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
            v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
        """
    }
    solution {
        val (mapLines, moveLines) = lines.splitByEmptyLines().toList()

        val widthMultiplier = if (isPart2) 2 else 1

        val originalMap = Array2D.fromLines(mapLines)
        val height = originalMap.height
        val width = originalMap.width * widthMultiplier
        val initialPos = originalMap.find('@').let { (i, j) -> i x (j * widthMultiplier) }

        val canvas =
            Array2D.ofChars(height, width) { i, j ->
                val ch = originalMap[i, j / widthMultiplier]
                if (ch == 'O' && isPart2) {
                    if (j % 2 == 0) '[' else ']'
                } else if (ch == '@' && isPart2 && j % 2 == 1) {
                    '.'
                } else {
                    ch
                }
            }

        var robotPos = initialPos
        for (moveCh in moveLines.asSequence().flatten()) {
            val dir = Dir.fromChar(moveCh)

            fun canMakeFreeAndMake(pos: Point, make: Boolean): Boolean =
                when (val ch = canvas[pos]) {
                    '.' -> true
                    '#' -> {
                        assert(!make)
                        false
                    }
                    '@', 'O' -> {
                        val nextPos = pos.moveInDir(dir)
                        val can = canMakeFreeAndMake(nextPos, make)
                        if (make) {
                            assert(can)
                            canvas[nextPos] = ch
                            canvas[pos] = '.'
                            if (ch == '@') {
                                robotPos = nextPos
                            }
                        }
                        can
                    }
                    else -> {
                        val (boxPosL, boxPosR) = when (ch) {
                            '[' -> pos to pos.moveInDir(Dir.RIGHT)
                            ']' -> pos.moveInDir(Dir.LEFT) to pos
                            else -> error(ch)
                        }
                        val boxPoses = setOf(boxPosL, boxPosR)
                        val nextBoxPosL = boxPosL.moveInDir(dir)
                        val nextBoxPosR = boxPosR.moveInDir(dir)
                        val can = listOf(nextBoxPosL, nextBoxPosR).all { nextPos ->
                            (nextPos in boxPoses) || canMakeFreeAndMake(nextPos, make)
                        }
                        if (make) {
                            assert(can)
                            canvas[boxPosL] = '.'
                            canvas[boxPosR] = '.'
                            canvas[nextBoxPosL] = '['
                            canvas[nextBoxPosR] = ']'
                        }
                        can
                    }
                }.also {
                    assert(!make || canvas[pos] == '.')
                }

            if (canMakeFreeAndMake(robotPos, make = false)) {
                canMakeFreeAndMake(robotPos, make = true)
            }
        }

        canvas.valuesIndexed
            .filter { (ch, _) -> ch == 'O' || ch == '[' }
            .sumOf { (_, pos) -> pos.i * 100L + pos.j }
    }
}

private fun Sequence<String>.flatten(): Sequence<Char> =
    sequence { this@flatten.forEach { s -> s.forEach { ch -> yield(ch) } } }