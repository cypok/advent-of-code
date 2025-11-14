package year2023

import utils.*
import utils.Dir.*
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

    solution1 {
        val steps = (exampleParam as Int?) ?: 64
        val moves = Array2D.ofInts(map.height, map.width, -1)

        moves[map.find('S')] = 0

        fun step(dir: Dir, base: Point, s: Int) {
            val next = base.moveInDir(dir)
            val ch = map.getOrNull(next) ?: return
            if (ch == '#') return
            val old = moves[next]
            assert(old != s)
            if (old != s + 1) {
                moves[next] = s + 1
            }
        }

        repeat(steps) { s ->
            for (base in map.indices) {
                if (moves[base] == s) {
                    step(UP, base, s)
                    step(LEFT, base, s)
                    step(DOWN, base, s)
                    step(RIGHT, base, s)
                }
            }
        }

        moves.sumOf { if (it == steps) 1 else 0 }
    }

    solution2 {
        val totalSteps = (exampleParam as Int?) ?: 26_501_365

        val enoughExtension = 5
        val enoughSteps = map.width * enoughExtension * 2

        val multiplier = 1 + 3 * enoughSteps / min(map.width, map.height)
        val marks = Array2D.ofInts(map.height * multiplier, map.width * multiplier, -1)

        fun posForMap(p: Point) = p.row.mod(map.height) x p.col.mod(map.width)

        map.find('S').let { (i, j) ->
            marks[i + multiplier / 2 * map.height, j + multiplier / 2 * map.width] = 0
        }

        fun step(dir: Dir, base: Point, s: Int): Boolean {
            val next = base.moveInDir(dir)
            val ch = map[posForMap(next)]
            if (ch == '#') return false
            val old = marks[next]
            assert(old != s)
            return if (old != s + 1) {
                marks[next] = s + 1
                true
            } else {
                false
            }
        }

        // Just choose one of the directions.
        var leftLimitNext = multiplier/2 * map.width + 1

        val limitHitEvents = mutableListOf<Pair<Int, Long>>()

        for (s in 0 until enoughSteps) {
            var reachedLimit = false
            for (i in 0 until marks.height) {
                for (j in 0 until marks.width) {
                    val base = i x j
                    if (marks[base] == s) {
                        step(UP, base, s)
                        if (step(LEFT, base, s) && i == leftLimitNext) {
                            assert(!reachedLimit)
                            reachedLimit = true
                            leftLimitNext -= map.width
                        }
                        step(DOWN, base, s)
                        step(RIGHT, base, s)
                    }
                }
            }

            if (reachedLimit) {
                val past = s + 1
                val count = marks.sumOf { if (it == s + 1) 1L else 0L }
                limitHitEvents += past to count
                if (limitHitEvents.size == enoughExtension) {
                    break
                }
            }
        }

        if (limitHitEvents.size < 5) {
            error("not enough steps!")
        }

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
