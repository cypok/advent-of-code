package year2025

import utils.*
import kotlin.math.max

// Task description:
//   https://adventofcode.com/2025/day/5

fun main() = runAoc {
    example {
        answer1(3)
        answer2(14)
        """
            3-5
            10-14
            16-20
            12-18

            1
            5
            8
            11
            17
            32
        """
    }

    example("closure") {
        answer2(10)
        """
            13-17
            10-19
            14-18
            
            0
        """
    }

    solution {
        val (freshRangesStr, availableIdsStr) = lines.splitByEmptyLines().toList()
        val freshRanges = freshRangesStr.map {
            val (l, r) = it.numbers()
            l..-r
        }

        if (isPart1) {
            availableIdsStr.count {
                val id = it.toLong()
                freshRanges.any { id in it }
            }

        } else {
            // Start from the biggest to not have problems when next range covers multiple small ranges.
            val ranges = freshRanges.sortedByDescending { it.size }

            ranges.withIndex().sumOf { (i, ri) ->
                var l = ri.left
                var r = ri.right
                for (rj in ranges.asSequence().take(i)) {
                    if (l in rj) l = rj.right + 1
                    if (r in rj) r = rj.left - 1
                    if (l > r) break
                }
                max(r - l + 1, 0)
            }
        }
    }
}

private val LongRange.left: Long get() = first
private val LongRange.right: Long get() = last
private val LongRange.size: Long get() = last - first + 1
