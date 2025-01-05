package year2023

import utils.*
import utils.Dir.*

fun main() = runAoc {
    solution {
        val respectSlopes = isPart1

        val slopes = mapOf(
            '^' to UP,
            '<' to LEFT,
            'v' to DOWN,
            '>' to RIGHT,
        )

        val start = 0 x 1
        val finish = (map.height - 1) x (map.width - 2)
        check(map[start] == '.')
        check(map[finish] == '.')

        fun canGoTo(nextPos: Point, dir: Dir): Boolean {
            val ch = map.getOrNull(nextPos) ?: return false
            if (ch == '#') return false

            if (respectSlopes) {
                slopes[ch]?.let { slopeDir ->
                    if (slopeDir != dir) {
                        return false
                    }
                }
            }

            return true
        }

        fun possibleDirs(pos: Point) =
            Dir.entries.filter { canGoTo(pos.moveInDir(it), it) }.toSet()

        val forkPointDirections = mutableMapOf<Point, List<Pair<Point, Int>>>().also { res ->
            fun buildGraph(forkPos: Point) {
                tailrec fun buildEdge(nextPos: Point, dir: Dir, curLen: Int): Pair<Point, Int>? {
                    val outDirs = (possibleDirs(nextPos) - dir.opposite)
                    if (outDirs.size > 1 || nextPos == finish) {
                        return nextPos to curLen
                    }
                    if (outDirs.isEmpty()) {
                        return null
                    }

                    val nextDir = outDirs.single()
                    return buildEdge(nextPos.moveInDir(nextDir), nextDir, curLen + 1)
                }

                if (forkPos in res || forkPos == finish) return

                val dirs = possibleDirs(forkPos)
                val edges = dirs.mapNotNull { dir -> buildEdge(forkPos.moveInDir(dir), dir, 1) }
                res[forkPos] = edges
                edges.forEach { (nextForkPos, _) -> buildGraph(nextForkPos) }
            }
            buildGraph(start)
        }

        fun findLongest(forkPos: Point, curLen: Int, visited: MutableSet<Point>): Int {
            if (forkPos in visited) return -1
            if (forkPos == finish) return curLen

            val nextEdges = forkPointDirections[forkPos]!!.filter { it.first !in visited }
            if (nextEdges.isEmpty()) return -1

            visited += forkPos
            try {
                return nextEdges.maxOf { (nextPos, len) ->
                    findLongest(nextPos, curLen + len, visited)
                }
            } finally {
                visited -= forkPos
            }
        }

        findLongest(start, 0, mutableSetOf())
    }

    example {
        answer1(94)
        answer2(154)

        """
            #.#####################
            #.......#########...###
            #######.#########.#.###
            ###.....#.>.>.###.#.###
            ###v#####.#v#.###.#.###
            ###.>...#.#.#.....#...#
            ###v###.#.#.#########.#
            ###...#.#.#.......#...#
            #####.#.#.#######.#.###
            #.....#.#.#.......#...#
            #.#####.#.#.#########v#
            #.#...#...#...###...>.#
            #.#.#v#######v###.###v#
            #...#.>.#...>.>.#.###.#
            #####v#.#.###v#.#.###.#
            #.....#...#...#.#.#...#
            #.#########.###.#.#.###
            #...###...#...#...#.###
            ###.###.#.###v#####v###
            #...#...#.#.>.>.#.>.###
            #.###.###.#.###.#.#v###
            #.....###...###...#...#
            #####################.#
        """
    }
}
