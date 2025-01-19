package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/12

fun main() = runAoc {
    example {
        answer1(140)
        answer2(80)
        """
            AAAA
            BBCD
            BBCC
            EEEC
        """
    }
    example {
        answer1(772)
        answer2(436)
        """
            OOOOO
            OXOXO
            OOOOO
            OXOXO
            OOOOO
        """
    }
    example {
        answer2(236)
        """
            EEEEE
            EXXXX
            EEEEE
            EXXXX
            EEEEE
        """
    }
    example {
        answer1(1930)
        answer2(1206)
        """
            RRRRIICCFF
            RRRRIICCCF
            VVRRRCCFFF
            VVRCCCJFFF
            VVVVCJJCFE
            VVIVCCJJEE
            VVIIICJJEE
            MIIIIIJJEE
            MIIISIJEEE
            MMMISSJEEE
        """
    }
    solution {
        val visited = Array2D.ofBooleans(map.height, map.width, false)
        map.indices.sumOf { pos ->
            if (visited[pos]) return@sumOf 0L

            val visitedSides = mutableSetOf<Pair<Point, Point>>()

            fun dfs(pos: Point): Pair<Long, Long> {
                check(!visited[pos])
                visited[pos] = true
                val ch = map[pos]
                var perimeter = 0L
                var area = 1L
                for (dir in Dir.entries) {
                    val nextPos = pos.moveInDir(dir)
                    val nextCh = map.getOrNull(nextPos)
                    if (nextCh == ch) {
                        // same area
                        if (!visited[nextPos]) {
                            val (a, p) = dfs(nextPos)
                            area += a
                            perimeter += p
                        }
                    } else {
                        // border
                        if (isPart1) {
                            perimeter += 1
                        } else {
                            if (pos to nextPos !in visitedSides) {
                                for (adjDir in listOf(dir.left, dir.right)) {
                                    var adjPos = pos
                                    var adjNextPos = nextPos
                                    while (true) {
                                        adjPos = adjPos.moveInDir(adjDir)
                                        adjNextPos = adjNextPos.moveInDir(adjDir)
                                        if (map.getOrNull(adjPos) == ch && map.getOrNull(adjNextPos) != ch) {
                                            visitedSides += adjPos to adjNextPos
                                        } else {
                                            break
                                        }
                                    }
                                }

                                perimeter += 1
                            }
                        }
                    }
                }
                return area to perimeter
            }

            val (area, perimeter) = dfs(pos)
            area * perimeter
        }
    }
}