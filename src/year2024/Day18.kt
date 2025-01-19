package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/18

fun main() = runAoc {
    example {
        answer1(22, 12)
        answer2("6,1")
        """
            5,4
            4,2
            4,5
            3,0
            2,1
            6,3
            2,4
            1,5
            0,6
            3,3
            2,6
            5,1
            1,2
            5,5
            2,5
            6,5
            1,4
            0,4
            6,4
            1,1
            6,1
            1,0
            0,5
            1,6
            2,0
        """
    }
    solution {
        val bytes = lines.map { line ->
            val (x, y) = line.numbersAsInts()
            Point(y, x)
        }
        val height = 1 + bytes.maxOf { it.i }
        val width = 1 + bytes.maxOf { it.j }

        fun calcStepsToFinish(canvas: Array2D<Char>): Int {
            val visited = Array2D.ofInts(height, width, Int.MAX_VALUE)
            val nextSteps = ArrayDeque(listOf((0 x 0) to 0))

            while (nextSteps.isNotEmpty() && visited[height - 1][width - 1] == Int.MAX_VALUE) {
                val (curPos, curSteps) = nextSteps.removeFirst()
                if (canvas.getOrNull(curPos) != '.') continue
                if (visited[curPos] <= curSteps) continue

                visited[curPos] = curSteps

                for (dir in Dir.entries) {
                    nextSteps.addLast(curPos.moveInDir(dir) to (curSteps + 1))
                }
            }

            return visited[height - 1, width - 1]
        }

        if (isPart1) {
            val bytesCount = exampleParam as? Int ?: 1024
            val canvas = Array2D.ofChars(height, width, '.')
            bytes.asSequence().take(bytesCount).forEach { canvas[it] = '#' }
            calcStepsToFinish(canvas)

        } else {
            var low = 0 // always non-blocked
            var high = bytes.size - 1 // always blocked

            val canvas = Array2D.ofChars(height, width, '.')
            var prevCount = 0

            while (low + 1 != high) {
                val mid = low + (high - low) / 2

                if (mid > prevCount) {
                    bytes.subList(prevCount, mid).forEach { assert(canvas[it] == '.'); canvas[it] = '#' }
                } else {
                    assert(mid < prevCount)
                    bytes.subList(mid, prevCount).forEach { assert(canvas[it] == '#'); canvas[it] = '.' }
                }
                prevCount = mid

                val isBlocked = calcStepsToFinish(canvas) == Int.MAX_VALUE
                if (isBlocked) {
                    high = mid
                } else {
                    low = mid
                }
            }

            val byte = bytes[low]
            return@solution "${byte.j},${byte.i}"
        }
    }
}