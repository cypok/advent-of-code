package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/2

fun main() = runAoc {
    example {
        answer1(1227775554)
        answer2(4174379265)
        """
            11-22,95-115,998-1012,1188511880-1188511890,222220-222224,
            1698522-1698528,446443-446449,38593856-38593862,565653-565659,
            824824821-824824827,2121212118-2121212124
        """
    }

    example("ababa") {
        answer1(0)
        answer2(0)
        """
            98989-98989
        """
    }

    fun isInvalid(s: String, partSize: Int, partCount: Int) =
        s.length == partSize * partCount &&
                (0 until (s.length - partSize)).all {
                    s[it] == s[it + partSize]
                }

    fun isInvalid1(s: String) =
        isInvalid(s, s.length / 2, 2)

    fun isInvalid2(s: String) =
        (1 .. s.length/2).any { partSize ->
            isInvalid(s, partSize, s.length / partSize)
        }

    solution {
        lines.sumOf { line ->
            line.numbers().disjointPairs().sumOf { (l, r) ->
                (l..-r).sumOf {
                    val s = it.toString()
                    if (isPart1 && isInvalid1(s) || isPart2 && isInvalid2(s)) it else 0
                }
            }
        }
    }
}