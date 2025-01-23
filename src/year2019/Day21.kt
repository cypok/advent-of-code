package year2019

import utils.*
import year2019.IntCodeComputer.AsciiApi.AsciiResult.*

// Task description:
//   https://adventofcode.com/2019/day/21

fun main() = runAoc {
    solution {
        val prog = IntCodeComputer(intCode).launchAsAscii(::printExtra)

        fun Int.regName() = 'A' + this - 1

        fun String.prepareScript(): String =
            replace("[1-9]".toRegex()) { it.value.single().digitToInt().regName().toString() }
                .replace(" *;.*".toRegex(), "")
                .replace("\n+".toRegex(), "\n")
                .replace("^\n".toRegex(), "")

        fun IntCodeComputer.AsciiApi.printScript(instructions: String) =
            printLine(instructions.trimIndent().prepareScript())

        prog.expectLine("Input instructions:")

        // Jump only if can jump and has something to jump over, don't jump without a necessity.
        // Jumping without a necessity fails on the input:
        //   /^\ /^\ /^\
        //  @...@...@...@....
        //  #####.#.#.##@####
        //   123456789

        // Need to jump: if there is something to jump over.
        prog.printScript("""
            OR  1 J  ; J = 1
            AND 2 J  ; J = 12
            AND 3 J  ; J = 123
            NOT J J  ; J = !(123)
        """)

        // Can jump: there is a safe path after the jump.
        if (isPart1) {
            prog.printScript("""
                OR 4 T  ; T = 4
            """)
        } else {
            prog.printScript("""
                OR  6 T  ; T = 6
                OR  9 T  ; T = 6 || 9
                AND 5 T  ; T = 5 && (6 || 9) = 56 || 59
                OR  8 T  ; T = 8 || 56 || 59
                AND 4 T  ; T = 48 || 456 || 459
            """)
        }
        prog.printScript("""
            AND T J  ; J = !(123) && (4 && ...)
        """)
        prog.printScript(if (isPart1) "WALK" else "RUN")
        prog.expectLine("")
        prog.expectLine(if (isPart1) "Walking..." else "Running...")
        prog.expectLine("")

        when (val res = prog.read()) {
            is Num -> res.value
                .also { prog.expectEnd() }
            is Str -> {
                check(res.value == "")
                prog.expectLine("Didn't make it across:")
                while (prog.readLineOrEnd() != null) {
                    // consume all
                }
                wrongAnswer
            }
            End -> error("unexpected end")
        }
    }
}