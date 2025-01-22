package year2024

import utils.*

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
            // Heavily based on the code of the machine, which loops by dividing rA by 8 again and again

            fun tryIt(curValue: Long, checkedOuts: Int): Long? {
                if (checkedOuts == instructions.size) {
                    return curValue
                }

                val nextExpected = instructions.subList(instructions.size - checkedOuts - 1, instructions.size)
                for (addend in 0 until 8) {
                    val nextValue = curValue * 8 + addend
                    val out = interpret(nextValue)
                    if (out.size == nextExpected.size) {
                        if (out[0] == nextExpected[0]) {
                            assert(out == nextExpected)
                            tryIt(nextValue, checkedOuts + 1)?.let {
                                return it
                            }
                        }
                    } else {
                        assert(out == nextExpected.subList(1, nextExpected.size))
                    }
                }
                return null
            }
            tryIt(0L, 0)!!
        }
    }
}