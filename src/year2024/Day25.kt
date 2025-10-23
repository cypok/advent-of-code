package year2024

import utils.*
import utils.Array2D

// Task description:
//   https://adventofcode.com/2024/day/25

fun main() = runAoc {
    solution1 {
        val locksAndKeys =
            lines.splitByEmptyLines()
                .map { Array2D.fromLines(it) }

        val (_, height) =
            locksAndKeys
                .map { it.width to it.height }
                .distinct()
                .single()

        val pairs =
            locksAndKeys
                .groupBy { it[0, 0] }
                .values
                .cartesianProduct()

        pairs.count { (l, k) ->
            (l.cols zip k.cols).all { (lc, kc) ->
                lc.count { it == '#' } + kc.count { it == '#' } <= height
            }
        }
    }

    example {
        answer1(3)
        """
            #####
            .####
            .####
            .####
            .#.#.
            .#...
            .....

            #####
            ##.##
            .#.##
            ...##
            ...#.
            ...#.
            .....

            .....
            #....
            #....
            #...#
            #.#.#
            #.###
            #####

            .....
            .....
            #.#..
            ###..
            ###.#
            ###.#
            #####

            .....
            .....
            .....
            #....
            #.#..
            #.#.#
            #####
        """
    }
}