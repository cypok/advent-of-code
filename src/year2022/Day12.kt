package year2022

import utils.*

// Task description:
//   https://adventofcode.com/2022/day/12

fun main() = runAoc {
    example {
        answer1(31)
        answer2(29)
        """
            Sabqponm
            abcryxxl
            accszExk
            acctuvwj
            abdefghi
        """
    }

    solution {
        val start = map.find('S')
        val end = map.find('E')
        map[start] = 'a'
        map[end] = 'z'

        fun height(p: Point) = map[p]

        val stepsMap = Array2D.ofInts(map.height, map.width, Int.MAX_VALUE)
        val queue = ArrayDeque<Point>()

        fun mark(p: Point, steps: Int) {
            stepsMap[p] = steps
            queue += p
        }

        mark(if (isPart1) start else end, 0)

        while (queue.isNotEmpty()) {
            val p = queue.removeFirst()
            val steps = stepsMap[p]
            val height = height(p)
            if (isPart1 && p == end ||
                isPart2 && height == 'a') {
                return@solution steps
            }
            val nextSteps = steps + 1
            for (dir in Dir.entries) {
                val nextP = p.moveInDir(dir)
                if (!map.contains(nextP)) continue
                if (stepsMap[nextP] <= nextSteps) continue
                val nextHeight = height(nextP)
                if (isPart1 && nextHeight > height + 1 ||
                    isPart2 && nextHeight < height - 1) continue
                mark(nextP, nextSteps)
            }
        }
    }
}