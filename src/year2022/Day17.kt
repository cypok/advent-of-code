package year2022

import utils.*
import kotlin.math.max
import kotlin.math.min

// Task description:
//   https://adventofcode.com/2022/day/17

fun main() = runAoc {
    example {
        answer1(3068)
        answer2(1514285714288)
        """
            >>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>
        """
    }

    val rockShapes = """
        ####

        .#.
        ###
        .#.

        ..#
        ..#
        ###

        #
        #
        #
        #

        ##
        ##
    """.trimIndent().lines().splitByEmptyLines()
        .map { Array2D.fromLines(it.asReversed()).map { it == '#' } }
        .toList()

    solution {
        val rocksCount = if (isPart1) 2022 else 1000000000000

        val jetDirsRaw = lines.single().map { Dir.fromChar(it) }
        val jetDirsCycleSize = jetDirsRaw.size
        val jetDirs = jetDirsRaw.cycle().iterator()
        val rocks = rockShapes.cycle().iterator()

        val enoughRocks = min(rocksCount, jetDirsCycleSize * 5L).toIntExact()

        val chamber = Array2D.ofBooleans(enoughRocks * rockShapes.maxOf { it.height }, 7, false)

        fun printChamber() {
            printExtra("---------")
            printExtra(chamber.rows
                .asReversed()
                .filter { it.any { it } }
                .joinToString("\n") { "|${it.map { if (it) '#' else '.' }.joinToString("")}|" })
            printExtra("---------")
        }

        val height = CyclicLinearGrowingState().apply { current = 0 }

        repeat(enoughRocks) { i ->
            val rock = rocks.next()
            var pos = (height.current + 3) x 2

            fun tryMove(dir: Dir): Boolean {
                val movedPos = pos.moveInDir(dir)
                val canMove = rock.valuesIndexed.all { (rc, rp) ->
                    !rc || chamber.getOrNull(movedPos + rp) == false
                }
                if (canMove) pos = movedPos
                return canMove
            }

            while (true) {
                tryMove(jetDirs.next())
                if (!tryMove(Dir.UP)) break
            }

            rock.valuesIndexed.forEach { (rc, rp) ->
                if (rc) {
                    assert(!chamber[pos + rp])
                    chamber[pos + rp] = true
                }
            }

            height.tick(i + 1, max(height.current, pos.row + rock.height))

            if ((i + 1) % jetDirsCycleSize == 0 && height.hasCycle()) {
                return@solution height.extrapolateUntil(rocksCount)
            }
        }

        check(enoughRocks.toLong() == rocksCount) { "we were unable to detect cycle, oops" }
        height.current
    }
}