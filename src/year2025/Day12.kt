package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/12

fun main() = runAoc {
    example("with changed lines to make it more like real input") {
        answer1(2)
        """
            0:
            ###
            ##.
            ##.

            1:
            ###
            ##.
            .##

            2:
            .##
            ###
            ##.

            3:
            ##.
            ###
            ##.

            4:
            ###
            #..
            ###

            5:
            ###
            .#.
            ###

            4x4: 0 0 0 0 1 0
            12x5: 1 0 1 0 0 2
            12x5: 1 9 1 9 9 2
        """
    }

    solution1 {
        val boxSize = 3
        val shapesAndAreas = lines.splitByEmptyLines().toList()
        val shapes = shapesAndAreas.dropLast(1).withIndex().map { (i, desc) ->
            check(i == desc[0].numbersAsInts().single())
            val shapeMap = Array2D.fromLines(desc.drop(1))
            check(shapeMap.width == boxSize && shapeMap.height == boxSize)
            shapeMap.count { it == '#' }.toIntExact()
        }
        shapesAndAreas.last().count { desc ->
            val nums = desc.numbersAsInts()
            val width = nums[0]
            val height = nums[1]
            val shapeCounts = nums.drop(2)
            check(shapeCounts.size == shapes.size)
            if (shapeCounts.sum() <= (width/boxSize) * (height/boxSize)) {
                // Just put them next to each other in independent 3x3 boxes.
                true
            } else if ((shapeCounts zip shapes).sumOf { (c, s) -> c * s } > width * height) {
                // Not enough space ever for perfect placement.
                false
            } else {
                error("oops, too hard")
            }
        }
    }
}