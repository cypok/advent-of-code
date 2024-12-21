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

    /*
<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
v<<A>>^A<A>AvA<^AA>A<vAAA>^A
<A^A>^^AvvvA
029A


029A:
<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
980A:
<v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
179A:
<v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
456A:
<v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
379A:

<v<A>>^AvA^A <v A<  AA>>^A A  vA<^A >AAv A^A   <vA>^AA<A>A<v<A>A>^AAAvA<^A>A <-- their
   <   A > A    v   <<   A A   >  ^  AA  > A
       ^   A             < <         ^^    A
           3                               7

v<<A>>^AvA^A v<<A>>^AA v<A<A>>^A  A vAA^<A>A   v<A>^AA<A>Av<A<A>>^AAAvA^<A>A <-- my
   <   A > A    <   AA   v <   A  A  >>  ^ A
       ^   A        ^^         <  <        A
           3                               7


     */

    solution1 {

        /**
         * ```
         * +---+---+---+
         * | 7 | 8 | 9 |
         * +---+---+---+
         * | 4 | 5 | 6 |
         * +---+---+---+
         * | 1 | 2 | 3 |
         * +---+---+---+
         *     | 0 | A |
         *     +---+---+
         * ```
         */
        fun numPadCoords(ch: Char): Point =
            when (ch) {
                '7' -> 0 x 0
                '8' -> 0 x 1
                '9' -> 0 x 2
                '4' -> 1 x 0
                '5' -> 1 x 1
                '6' -> 1 x 2
                '1' -> 2 x 0
                '2' -> 2 x 1
                '3' -> 2 x 2
                '0' -> 3 x 1
                'A' -> 3 x 2
                else -> error(ch)
            }

        /**
         * ```
         *     +---+---+
         *     | ^ | A |
         * +---+---+---+
         * | < | v | > |
         * +---+---+---+
         * ```
         */
        fun dirPadCoords(ch: Char): Point =
            when (ch) {
                '^' -> 0 x 1
                'A' -> 0 x 2
                '<' -> 1 x 0
                'v' -> 1 x 1
                '>' -> 1 x 2
                else -> error(ch)
            }

        fun genCommands(codeVariants: List<List<Char>>, numPad: Boolean): List<List<Char>> {
            val coords = if (numPad) ::numPadCoords else ::dirPadCoords
            val badPos = if (numPad) 3 x 0 else 0 x 0
            val res = mutableListOf<List<Char>>()
            for (code in codeVariants) {
                val legs = mutableListOf<List<List<Char>>>()
                (listOf('A') + code).windowed(2).forEach { (fromCh, toCh) ->
                    val fromPos = coords(fromCh)
                    val toPos = coords(toCh)

                    val variants = mutableListOf<List<Char>>()
                    fun iter(curPos: Point, prevs: List<Char>) {
                        if (curPos == toPos) {
                            variants += prevs + 'A'
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

                    legs += variants

                    // val cmd = if (numPad) {
                    //     "" +
                    //             (if (dy < 0) "^".repeat(-dy) else "") +
                    //             (if (dx < 0) "<".repeat(-dx) else "") +
                    //             (if (dx > 0) ">".repeat(dx) else "") +
                    //             (if (dy > 0) "v".repeat(dy) else "") +
                    //             "A"
                    // } else {
                    //     "" +
                    //             (if (dx > 0) ">".repeat(dx) else "") +
                    //             (if (dy < 0) "^".repeat(-dy) else "") +
                    //             (if (dy > 0) "v".repeat(dy) else "") +
                    //             (if (dx < 0) "<".repeat(-dx) else "") +
                    //             "A"
                    // }
                    // cmds.addAll(cmd.toList())
                }

                val paths = legs.cartesianProduct()
                val paths2 = paths.map { it.flatten() }
                res += paths2
            }
            val min = res.minOf { it.size }
            return res.filter { it.size == min }
        }

        //println()
        lines.sumOf { code ->
            //println(code)
            val nextCode1 = genCommands(listOf(code.toList()), true)
            //println(nextCode1.first().joinToString(""))
            val nextCode2 = genCommands(nextCode1, false)
            //println(nextCode2.first().joinToString(""))
            val nextCode3 = genCommands(nextCode2, false)
            //println(nextCode3.first().joinToString(""))
            nextCode3.first().size * code.numbers().single()
        }
    }
}