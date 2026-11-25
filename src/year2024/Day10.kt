package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/10

fun main() = runAoc {
    example("tiny") {
        answer1(4)
        answer2(13)
        """
            ..90..9
            ...1.98
            ...2..7
            6543456
            765.987
            876....
            987....
        """
    }
    example {
        answer1(36)
        answer2(81)
        """
            89010123
            78121874
            87430965
            96549874
            45678903
            32019012
            01329801
            10456732
        """
    }
    solution {
        map.valuesIndexed.sumOf { [ch, pos] ->
            if (ch != '0') return@sumOf 0L

            if (isPart1) {
                val tops = mutableSetOf<Point>()
                collectTops(pos, 0, tops)
                tops.size.toLong()
            } else {
                collectTops(pos, 0, null)
            }
        }
    }
}

private fun SolutionContext.collectTops(start: Point, expectedHeight: Int, tops: MutableSet<Point>?): Long =
    if (start !in map || map[start] != expectedHeight.digitToChar()) {
        0L
    } else if (expectedHeight == 9) {
        tops?.let {it += start }
        1L
    } else {
        Dir.entries.sumOf { dir ->
            collectTops(start.moveInDir(dir), expectedHeight + 1, tops)
        }
    }
