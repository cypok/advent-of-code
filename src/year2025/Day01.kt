package year2025

import utils.*

// Task description:
//   https://adventofcode.com/2025/day/1

fun main() = runAoc {
    example {
        answer1(3)
        answer2(6)
        """
            L68
            L30
            R48
            L5
            R60
            L55
            L1
            L99
            R14
            L82
        """
    }

    solution {
        var cur = 50
        var res = 0
        for (line in lines) {
            val dir = if (line[0] == 'R') 1 else -1
            val clicks = line.substring(1).toInt()

            // It's hard to count to the left side, always to the right is easier.
            fun wrap(x: Int) = if (dir == 1) x else (100 - x).mod(100)

            val new = wrap(cur) + clicks
            cur = wrap(new).mod(100)
            if (isPart1) {
                if (cur == 0) res++
            } else {
                res += new / 100
            }
        }
        res
    }
}