package year2019

import utils.*
import kotlin.math.abs

// Task description:
//   https://adventofcode.com/2019/day/17

fun main() = runAoc {
    // Scaffold element.
    @Suppress("LocalVariableName")
    val S = '#'

    fun findAndPatchStart(map: Array2D<Char>): Pair<Point, Dir> {
        val startPos = map.find { it in "^v<>" }
        val startDir = Dir.fromChar(map[startPos])
        map[startPos] = S
        return startPos to startDir
    }

    fun List<String>.joinWithCommas() = joinToString(",")
    fun List<List<String>>.joinWithCommas() = joinToString("\n") { it.joinWithCommas() }

    fun buildRawPath(map: Array2D<Char>, startPos: Point, startDir: Dir): List<String> =
        buildList {
            var curPos = startPos
            var curDir = startDir
            while (true) {
                val (nextDir, turnCh) = listOf(curDir.left to "L", curDir.right to "R")
                    .filter { map.getOrNull(curPos.moveInDir(it.first)) == S }
                    .also { if (it.isEmpty()) break }
                    .single()
                add(turnCh)

                val nextPos = generateSequence(curPos) { it.moveInDir(nextDir) }
                    .takeWhile { map.getOrNull(it) == S }
                    .last()
                val distance = abs(curPos.i - nextPos.i) + abs(curPos.j - nextPos.j)
                add(distance.toString())

                curPos = nextPos
                curDir = nextDir
            }
        }

    fun compressPath(path: List<String>, limit: Int): Set<List<List<String>>> {
        val remaining = path.toMutableList()

        fun replaceAll(old: List<String>, new: List<String>) {
            val size = old.size.also { assert(it == new.size) }
            var i = 0
            while (i <= remaining.size - size) {
                if (old == remaining.subList(i, i + size)) {
                    for (j in 0 until size) {
                        remaining[i + j] = new[j]
                    }
                    i += size
                } else {
                    i++
                }
            }
        }

        var nextFunctionChar = 'A'
        fun withFunction(start: Int, end: Int, action: (List<String>) -> Unit) {
            assert(start < end)
            val f = path.subList(start, end)
            if (f.joinWithCommas().length > limit) {
                return
            }
            val name = nextFunctionChar++.toString()
            val functionResult = List(f.size) { if (it == 0) name else "-" }
            replaceAll(f, functionResult)
            action(f)
            replaceAll(functionResult, f)
            // Note that there could be different ways of function application in case of overlapping,
            // e.g., apply "R8R8" to "L2R8R8R8L4".
            // We could miss some variants but I hope that it would be enough to choose only one application.
            nextFunctionChar--
        }

        return buildSet {
            for (initSize in 2..limit / 2 step 2) {
                val initStart = 0
                val initEnd = initStart + initSize
                withFunction(initStart, initEnd) { initFunction ->

                    val theEnd = remaining.indexOfLast { it in "LR" }.also { check(it != -1) } + 2
                    for (tailSize in 2..limit / 2 step 2) {
                        val tailEnd = theEnd
                        val tailStart = tailEnd - tailSize
                        withFunction(tailStart, tailEnd) { tailFunction ->

                            val midStart = 2 * remaining.asSequence().chunked(2).countWhile { (r, _) -> r !in "LR" }
                            val midSize =
                                2 * remaining.asSequence().drop(midStart).chunked(2).countWhile { (r, _) -> r in "LR" }
                            val midEnd = midStart + midSize
                            withFunction(midStart, midEnd) { midFunction ->

                                if (remaining.all { it !in "LR" }) {
                                    val mainFunction = remaining.filter { it != "-" }
                                    if (mainFunction.joinWithCommas().length <= limit) {
                                        add(listOf(mainFunction, initFunction, tailFunction, midFunction))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun SolutionContext.pc() =
        IntCodeComputer(intCode).apply {
            check(this[0] == 1L)
            if (isPart2) {
                this[0] = 2L
            }
        }

    solution { pc().runAscii(::printExtra) {

        val map = Array2D.fromLines(buildList {
            while (true) {
                val line = scanLine()
                if (line.isEmpty()) {
                    break
                }
                add(line)
            }
        })

        val (startPos, startDir) = findAndPatchStart(map)

        if (isPart1) {
            map.indices.sumOf { (i, j) ->
                if (1 <= i && i < map.height - 1 && 1 <= j && j < map.width - 1 &&
                    map[i, j] == S &&
                    map[i - 1, j] == S && map[i + 1, j] == S &&
                    map[i, j - 1] == S && map[i, j + 1] == S
                ) {
                    i * j
                } else {
                    0
                }
            }
        } else {
            val movements = compressPath(buildRawPath(map, startPos, startDir), 20).first().map { it.joinWithCommas() }

            expectLine("Main:")
            printLine(movements[0])
            expectLine("Function A:")
            printLine(movements[1])
            expectLine("Function B:")
            printLine(movements[2])
            expectLine("Function C:")
            printLine(movements[3])
            expectLine("Continuous video feed?")
            printLine("n")

            expectLine("")
            while (scanLine().isNotEmpty()) {
                // consume the map after the movement
            }

            scanNum().also { expectEnd() }
        }
    }}

    test {
        val map = Array2D.fromLines("""
                #######...#####
                #.....#...#...#
                #.....#...#...#
                ......#...#...#
                ......#...###.#
                ......#.....#.#
                ^########...#.#
                ......#.#...#.#
                ......#########
                ........#...#..
                ....#########..
                ....#...#......
                ....#...#......
                ....#...#......
                ....#####......
            """.trimIndent().lines())
        val (pos, dir) = findAndPatchStart(map)
        val rawPath = buildRawPath(map, pos, dir).joinWithCommas()
        check(rawPath == "R,8,R,8,R,4,R,4,R,8,L,6,L,2,R,4,R,4,R,8,R,8,R,8,L,6,L,2")
    }
    test {
        val variants =
            compressPath("R,8,R,8,R,4,R,4,R,8,L,6,L,2,R,4,R,4,R,8,R,8,R,8,L,6,L,2".split(","), 12)
                .map { it.joinWithCommas() }

        check(variants.contains(
                """
                    A,C,B,C,A,B
                    R,8,R,8
                    R,8,L,6,L,2
                    R,4,R,4
                """.trimIndent()))
    }
}