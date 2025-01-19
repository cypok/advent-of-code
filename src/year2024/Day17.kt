package year2024

import utils.*
import kotlin.math.max

// Task description:
//   https://adventofcode.com/2024/day/17

fun main() = runAoc {
    example {
        answer1("4,6,3,5,6,3,5,2,1,0")
        """
            Register A: 729
            Register B: 0
            Register C: 0

            Program: 0,1,5,4,3,0
        """
    }
    example {
        answer1("0,3,5,4,3,0")
        answer2(117440)
        """
            Register A: 117440
            Register B: 0
            Register C: 0

            Program: 0,3,5,4,3,0
        """
    }
    solution {
        val (raOriginal, rbOriginal, rcOriginal) = lines.take(3).map { it.numbers().first() }
        val instructions = lines.drop(4).first().numbersAsInts()

        fun Long.asShiftArg(): Int {
            check(this >= 0)
            return coerceAtMost(64).toInt()
        }

        fun interpret(raInitial: Long): List<Int> {
            var ra = raInitial
            var rb = rbOriginal
            var rc = rcOriginal

            fun comboOperand(operandValue: Int): Long =
                when (operandValue) {
                    0, 1, 2, 3 -> operandValue.toLong()
                    4 -> ra
                    5 -> rb
                    6 -> rc
                    7 -> error("reserved")
                    else -> error("unexpected $operandValue")
                }

            val output = mutableListOf<Int>()

            var ip = 0
            while (ip < instructions.size) {
                val opcode = instructions[ip++]
                val literalOperand = instructions[ip++]

                when (opcode) {
                    // adv
                    0 -> ra = ra ushr comboOperand(literalOperand).asShiftArg()
                    // bdv
                    6 -> rb = ra ushr comboOperand(literalOperand).asShiftArg()
                    // cdv
                    7 -> rc = ra ushr comboOperand(literalOperand).asShiftArg()

                    // bxl
                    1 -> rb = rb xor literalOperand.toLong()
                    // bxc
                    4 -> rb = rb xor rc

                    // bst
                    2 -> rb = comboOperand(literalOperand).mod(8).toLong()

                    // jnz
                    3 -> if (ra != 0L) ip = literalOperand

                    // out
                    5 -> output += comboOperand(literalOperand).mod(8)

                    else -> error("unexpected $opcode")
                }
            }

            return output
        }

        if (isPart1) {
            interpret(raOriginal).joinToString(",")

        } else {
            val outsCount = instructions.size

            // Heavily based on the code of the machine, which loops by dividing rA by 8 again and again
            var curLow = 1L.shl(3 * (outsCount - 1)) // 8^(outsCount-1)
            var curHigh = 1L.shl(3 * (outsCount)) // 8^(outsCount)

            var curIdx = outsCount - 1

            while (true) {
                if (curIdx == -1) {
                    return@solution curLow + 1
                }

                val step = max(1, ((curHigh - curLow) / 100))
                val outs =
                    ((curLow until curHigh step step) + (curHigh - 1)).map { ra ->
                        val out = interpret(ra)
                        check(out.size == instructions.size)
                        ra to out
                    }

                fun isGood(out: List<Int>): Boolean =
                    (curIdx until instructions.size).all { idx -> out[idx] == instructions[idx] }

                val bads = outs.countWhile { (_, out) -> !isGood(out) }
                if (bads > 0) {
                    val goods = outs.drop(bads).countWhile { (_, out) -> isGood(out) }
                    check(bads + goods < outs.size)
                    curLow = outs[bads - 1].first
                    curHigh = outs[bads + goods].first
                } // else just go to the next digit
                curIdx--
            }
        }
    }
}