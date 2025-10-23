package year2023

import utils.*
import utils.Array2D
import utils.Dir.*

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private fun solve1(input: List<String>): Long {
    val map = Array2D.fromLines(input)
    return calculateEnergy(map, Point(0, 0), RIGHT)
}

private fun solve2(input: List<String>): Long {
    val map = Array2D.fromLines(input)
    return sequence {
        for (i in 0 until map.height) {
            yield(calculateEnergy(map, Point(i, 0), RIGHT))
            yield(calculateEnergy(map, Point(i, map.width - 1), LEFT))
        }
        for (j in 0 until map.width) {
            yield(calculateEnergy(map, Point(0, j), DOWN))
            yield(calculateEnergy(map, Point(map.height - 1, j), UP))
        }
    }.max()
}

private fun calculateEnergy(map: Array2D<Char>, startPos: Point, startDir: Dir): Long {
    val beams = Array2D.of(map.height, map.width) { mutableSetOf<Dir>() }

    tailrec fun traverse(pos: Point, dir: Dir) {
        val (i, j) = pos
        if (map.getOrNull(i, j) == null) return
        if (dir in beams[i, j]) return
        beams[i, j] += dir

        val elem = map[i, j]
        val dirs =
            when (elem) {
                '.' -> listOf(dir)

                '/' -> listOf(when (dir) {
                    RIGHT -> UP
                    LEFT -> DOWN
                    UP -> RIGHT
                    DOWN -> LEFT
                })

                '\\' -> listOf(when (dir) {
                    RIGHT -> DOWN
                    LEFT -> UP
                    UP -> LEFT
                    DOWN -> RIGHT
                })

                '|' -> when (dir) {
                    UP, DOWN -> listOf(dir)
                    LEFT, RIGHT -> listOf(dir.left, dir.right)
                }

                '-' -> when (dir) {
                    LEFT, RIGHT -> listOf(dir)
                    UP, DOWN -> listOf(dir.left, dir.right)
                }

                else -> error(elem)
            }

        if (dirs.size == 2) {
            @Suppress("NON_TAIL_RECURSIVE_CALL")
            traverse(pos.moveInDir(dirs[1]), dirs[1])
        } else {
            assert(dirs.size == 1)
        }
        traverse(pos.moveInDir(dirs[0]), dirs[0])
    }

    traverse(startPos, startDir)
    return beams.sumOf { if (it.isEmpty()) 0L else 1L }
}
