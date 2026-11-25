package year2020

import utils.*

// Task description:
//   https://adventofcode.com/2020/day/2

fun main() = runAoc {
    example {
        answer1(2)
        answer2(1)
        """
            1-3 a: abcde
            1-3 b: cdefg
            2-9 c: ccccccccc
        """
    }
    solution {
        lines.count { line ->
            val [pos1Str, pos2Str, chStr, pass] = line.split(""":? |-""".toRegex())
            val pos1 = pos1Str.toInt()
            val pos2 = pos2Str.toInt()
            val ch = chStr.single()
            if (isPart1) {
                pass.count { it == ch } in pos1..pos2
            } else {
                (pass.getOrNull(pos1 - 1) == ch) != (pass.getOrNull(pos2 - 1) == ch)
            }
        }
    }
}