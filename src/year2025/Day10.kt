package year2025

import utils.*
import java.util.Arrays

// Task description:
//   https://adventofcode.com/2025/day/10

fun main() = runAoc {
    example {
        answer1(7)
        answer2(33)
        """
            [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
            [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
            [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
        """
    }

    solution {
        lines.sumOf { machineDesc ->
            val buttons = machineDesc.substringAfter("] ").substringBefore(" {").split(" ").map { it.numbersAsInts() }

            if (isPart1) {
                val targetState = machineDesc.substringBefore("]").substringAfter("[").map {
                    when (it) {
                        '.' -> false
                        '#' -> true
                        else -> error(it)
                    }
                }.toBooleanArray()
                buttons.map { listOf(false, true) }.cartesianProduct().minOf { buttonPresses ->
                    val state = BooleanArray(targetState.size)
                    (buttons zip buttonPresses).forEach { (b, p) ->
                        if (p) b.forEach { state[it] = !state[it] }
                    }
                    if (state contentEquals targetState) buttonPresses.count { it } else Int.MAX_VALUE
                }

            } else {
                println(machineDesc)
                val targetState = machineDesc.substringAfter("{").numbersAsInts().toIntArray()
                val maxPressesOfButton = buttons.map {
                    it.minOf { targetState[it] }
                }
                maxPressesOfButton.map { 0..it }.cartesianProduct().minOf { buttonPresses ->
                    val state = IntArray(targetState.size)
                    (buttons zip buttonPresses).forEach { (b, p) ->
                        b.forEach { state[it] += p }
                    }
                    if (state contentEquals targetState) buttonPresses.sum() else Int.MAX_VALUE
                }
            }
        }
    }
}