package year2022

import utils.*

// Task description:
//   https://adventofcode.com/2022/day/12

fun main() = runAoc {
    example {
        answer1(31)
        """
            Sabqponm
            abcryxxl
            accszExk
            acctuvwj
            abdefghi
        """
    }

    solution1 {
        val start = map.find('S')
        val end = map.find('E')
        map[start] = 'a'
        map[end] = 'z'

        val visited = Array2D.ofInts(map.height, map.width, Int.MAX_VALUE)

        fun visit(p: Point, steps: Int) {
            if (visited[p] <= steps) return
            visited[p] = steps
            val nextSteps = steps + 1
            val maxNextHeight = map[p] + 1
            Dir.entries
                .map { p.moveInDir(it) }
                .filter { map.contains(it) }
                .map { it to map[it] }
                .filter { it.second <= maxNextHeight }
                .sortedByDescending { it.second }
                .forEach {
                    visit(it.first, nextSteps)
                }
        }
        visit(start, 0)

        visited[end]
    }
}