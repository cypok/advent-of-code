package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/21

fun main() = runAoc {
    example {
        answer1(126384)
        """
            029A
            980A
            179A
            456A
            379A
        """
    }

    solution {
        val numPad = """
            7 8 9
            4 5 6
            1 2 3
            X 0 A
        """.trimIndent()

        val dirPad = """
            X ^ A
            < v >
        """.trimIndent()

        // 6 -> 1x2
        fun buildCoordsMapping(pad: String): Map<Char, Point> =
            pad.trim().lines().withIndex().flatMap { [row, line] ->
                line.split(' ').withIndex().map { [col, ch] ->
                    ch.single() to (row x col)
                }
            }.toMap()

        // (2,9) -> [ >^^A, ^>^A, ^^>A ]
        fun calcAllLegVariants(pad: Map<Char, Point>): Map<Pair<Char, Char>, List<String>> {
            val chars = pad.keys - 'X'
            val badPos = pad['X']
            return chars.flatMap { fromCh ->
                chars.map { toCh ->
                    val fromPos = pad[fromCh]!!
                    val toPos = pad[toCh]!!

                    val variants = mutableListOf<String>()
                    fun iter(curPos: Point, prevs: List<Char>) {
                        if (curPos == toPos) {
                            variants += (prevs + 'A').joinToString("")
                            return
                        }
                        if (curPos == badPos) {
                            return
                        }

                        val dx = toPos.col - curPos.col
                        val dy = toPos.row - curPos.row

                        if (dx > 0) {
                            iter(curPos.moveInDir(Dir.RIGHT), prevs + '>')
                        } else if (dx < 0) {
                            iter(curPos.moveInDir(Dir.LEFT), prevs + '<')
                        }

                        if (dy > 0) {
                            iter(curPos.moveInDir(Dir.DOWN), prevs + 'v')
                        } else if (dy < 0) {
                            iter(curPos.moveInDir(Dir.UP), prevs + '^')
                        }
                    }
                    iter(fromPos, listOf())

                    (fromCh to toCh) to variants
                }
            }.toMap()
        }

        //                              >^^A
        // 029A -> [  [ <A ], [ ^A ], [ ^>^A ], [ vvvA ]  ]
        //                              ^^>A
        val legVariantsNum = calcAllLegVariants(buildCoordsMapping(numPad))
        val legVariantsDir = calcAllLegVariants(buildCoordsMapping(dirPad))
        fun getNextLevelLegVariants(leg: String): List<List<String>> {
            val legVariantsMap = if (leg[0].isDigit()) legVariantsNum else legVariantsDir
            return (listOf('A') + leg.asIterable()).windowedPairs().map { legVariantsMap[it]!! }
        }

        // (029A, 1) -> 2 + 2 + (4 for any) + 4 = 12
        val stepsCache = mutableMapOf<Pair<String, Int>, Long>()
        fun countSteps(leg: String, levelsNum: Int): Long =
            if (levelsNum == 0) {
                leg.length.toLong()
            } else {
                stepsCache.getOrPut(leg to levelsNum) {
                    getNextLevelLegVariants(leg).sumOf { legVariants ->
                        legVariants.minOf { legVariant ->
                            countSteps(legVariant, levelsNum - 1)
                        }
                    }
                }
            }

        val levels = if (isPart1) 3 else 26
        lines.sumOf { it.numbers().single() * countSteps(it, levels) }
    }
}