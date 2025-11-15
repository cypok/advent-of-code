package year2023

import utils.*
import kotlin.math.min

// Task description:
//   https://adventofcode.com/2023/day/21

fun main() = runAoc {
    example {
        answer1(16, param = 6)
        //answer2(1594, param = 50)
        //answer2(16733044, param = 5000)
        """
            ...........
            .....###.#.
            .###.##..#.
            ..#.#...#..
            ....#.#....
            .##..S####.
            .##..#...#.
            .......##..
            .##.#.####.
            .##..##.##.
            ...........
        """
    }

    @Suppress("LocalVariableName")
    val UNVISITED = Int.MIN_VALUE

    fun SolutionContext.step(marks: Array2D<Int>, dir: Dir, base: Point, s: Int): Point? {
        val next = base.moveInDir(dir)
        val ch = map.getOrNull(next.i % map.height, next.j % map.width)
        if (ch == null || ch == '#') return null
        val old = marks[next]
        return if (old == UNVISITED) {
            marks[next] = s + 1
            next
        } else {
            assert(old in 0..<s || old == s + 1)
            null
        }
    }

    fun SolutionContext.iterate(
        steps: Int, start: Point, marks: Array2D<Int>,
        preStepCheck: (List<Point>, Int) -> Boolean = { _, _ -> true }
    ) {
        marks[start] = 0
        var frontier = listOf(start)
        for (s in 0 until steps) {
            if (!preStepCheck(frontier, s)) break

            val nextFrontier = mutableListOf<Point>()
            for (base in frontier) {
                for (dir in Dir.entries) {
                    step(marks, dir, base, s)?.let {
                        nextFrontier += it
                    }
                }
            }
            frontier = nextFrontier
        }
    }

    fun countPlots(marks: Array2D<Int>, steps: Int): Long =
        marks.count { it >= 0 && it % 2 == steps % 2 }

    solution1 {
        val steps = (exampleParam as Int?) ?: 64
        val marks = Array2D.ofInts(map.height, map.width, UNVISITED)
        val start = map.find('S')
        iterate(steps, start, marks)

        if (false) printExtra(
            Array2D.ofChars(map.height, map.width) { i, j ->
                when {
                    map[i, j] == '#' -> '#'
                    marks[i, j] == steps -> '0'
                    marks[i, j] < 0 -> '.'
                    marks[i, j] % 2 == 0 -> '/'
                    marks[i, j] % 2 == 1 -> '\\'
                    else -> shouldNotReachHere()
                }
            }.toAsciiArt(' ')
        )

        countPlots(marks, steps)
    }

    solution2 {
        val totalSteps = (exampleParam as Int?) ?: 26_501_365

        val enoughExtension = 5

        val enoughSteps = map.width * enoughExtension * 2

        val multiplier = 1 + 3 * enoughSteps / min(map.width, map.height)
        val marks = Array2D.ofInts(map.height * multiplier, map.width * multiplier, UNVISITED)

        val start = map.find('S').let { (i, j) ->
            (i + multiplier / 2 * map.height) x (j + multiplier / 2 * map.width)
        }

        // Just choose one of the directions.
        var leftLimitNext = multiplier/2 * map.width

        val limitHitEvents = mutableListOf<Pair<Int, Long>>()

        iterate(enoughSteps, start, marks) { frontier, s ->
            if (frontier.any() { it.j == leftLimitNext }) {
                leftLimitNext -= map.width
                val count = countPlots(marks, s)
                limitHitEvents += s to count
                if (limitHitEvents.size == enoughExtension) {
                    return@iterate false
                }
            }
            return@iterate true
        }

        check(limitHitEvents.size == enoughExtension)

        val velocities = limitHitEvents.map { it.second }.derivative()
        val accelerations = velocities.derivative()
        val acceleration = accelerations.first()
        assert(accelerations.all { it == acceleration })

        val stepStart = limitHitEvents.first().first
        val stepDeltas = limitHitEvents.map { it.first.toLong() }.derivative()
        val stepDelta = stepDeltas.first().toIntExact()
        assert(stepDeltas.all { it.toIntExact() == stepDelta })

        assert((totalSteps - stepStart) % stepDelta == 0) {
            "otherwise it's easier to tune leftLimitNext, rather than write more code" }

        var curStep = limitHitEvents.last().first
        var curCount = limitHitEvents.last().second
        var curVelocity = velocities.last()

        while (curStep < totalSteps) {
            curVelocity += acceleration
            curCount += curVelocity
            curStep += stepDelta
        }
        assert(curStep == totalSteps)
        curCount
    }
}

private fun List<Long>.derivative() = windowed(2).map { it[1] - it[0] }
