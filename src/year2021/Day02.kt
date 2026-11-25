package year2021

import utils.*
import kotlin.math.abs

// Task description:
//   https://adventofcode.com/2021/day/2

fun main() = runAoc {
    example {
        answer1(150)
        answer2(900)
        """
            forward 5
            down 5
            forward 8
            up 3
            down 8
            forward 2
        """
    }
    solution1 {
        lines.fold(0 x 0) { pos, line ->
            val [cmd, argStr] = line.split(' ')
            val arg = argStr.toInt()
            val dir = when (cmd) {
                "forward" -> Dir.RIGHT
                "down" -> Dir.DOWN
                "up" -> Dir.UP
                else -> error(cmd)
            }
            pos.moveInDir(dir, arg)
        }.let {
            it.j * abs(it.i)
        }
    }
    solution2 {
        lines.fold(Triple(0, 0, 0)) { [pos, depth, aim], line ->
            val [cmd, argStr] = line.split(' ')
            val arg = argStr.toInt()
            when (cmd) {
                "forward" -> Triple(pos + arg, depth + aim * arg, aim)
                "down" -> Triple(pos, depth, aim + arg)
                "up" -> Triple(pos, depth, aim - arg)
                else -> error(cmd)
            }
        }.let { [pos, depth, _] ->
            pos * depth
        }
    }
}