#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
import utils.*

#set($YearUrl = $PACKAGE_NAME.substring(4))
#set($DayUrl = $DayTwoDigits)
#if($DayUrl.startsWith("0"))
#set($DayUrl = $DayUrl.substring(1))
#end
// Task description:
//   https://adventofcode.com/$YearUrl/day/$DayUrl

fun main() = runAoc {
    example {
        answer1(0 /* expected answer goes here */)
        """
            example
            goes
            here
        """
    }

    solution1 {
        // solution goes here
    }
}