package year2019

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.*

// Task description:
//   https://adventofcode.com/2019/day/7

fun main() = runAoc {
    example {
        answer1(43210)
        """
            3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0"
        """
    }
    example {
        answer1(54321)
        """
            3,23,3,24,1002,24,10,24,1002,23,-1,23,
            101,5,23,23,1,24,23,23,4,23,99,0,0
        """
    }
    example {
        answer1(65210)
        """
            3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,
            1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0
        """
    }
    example {
        answer2(139629729)
        """
            3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,
            27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5
        """
    }
    example {
        answer2(18216)
        """
            3,52,1001,52,-5,52,3,53,1,52,56,54,1007,54,5,55,1005,55,26,1001,54,
            -5,54,1105,1,12,1,53,54,53,1008,54,0,55,1001,55,1,55,2,53,55,53,4,
            53,1001,56,-1,56,1005,56,6,99,0,0,0,0,10
        """
    }

    solution {
        val n = 5

        val basePhase = if (isPart1) 0 else n
        val phaseSettings = (basePhase until basePhase + n).toList().permutations()

        phaseSettings.maxOf { phases ->
            runBlocking {
                val chs = List(n + 1) { Channel<Long>() }
                val pcs = List(n) { IntCodeComputer(intCode) }
                for ((pc, chPair) in pcs zip chs.zipWithNext()) {
                    val (chIn, chOut) = chPair
                    launch {
                        pc.run(chIn, chOut)
                        chIn.halt()
                    }
                }
                for ((ch, phase) in chs zip phases) {
                    ch.send(phase.toLong())
                }

                var signal = 0L
                while (true) {
                    chs.first().sendOrOnHalted(signal) { break }
                    signal = chs.last().receive()
                }
                signal
            }
        }
    }
}