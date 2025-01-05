import utils.IS_BATCH_RUN
import utils.TOTAL_FAILS
import utils.cartesianProduct
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.time.LocalDate
import java.time.Month
import java.time.Year
import kotlin.time.measureTime

fun main() {
    IS_BATCH_RUN = true

    val lookup = MethodHandles.lookup()
    val mainName = "main"
    val mainMT = MethodType.methodType(Class.forPrimitiveName("void"))

    val today = LocalDate.now()

    var totalDays = 0
    var lastWasShort = false
    val totalTime = measureTime {
        years@for (year in 2015..Year.now().value) {
            for (day in 1..25) {
                val dayDate = LocalDate.of(year, Month.DECEMBER, day)
                if (dayDate > today) break@years

                val nameParts = listOf(
                    listOf("year$year."),
                    listOf("Day"),
                    listOf(day.toString(), "%02d".format(day)),
                    listOf("Kt", "")
                )
                val cls = nameParts.cartesianProduct().firstNotNullOfOrNull { parts ->
                    runCatching { lookup.findClass(parts.joinToString("")) }.getOrNull()
                }

                if (cls != null) {
                    totalDays++
                    if (lastWasShort) {
                        println()
                        lastWasShort = false
                    }
                    println("Year $year, Day $day")
                    val method = lookup.findStatic(cls, mainName, mainMT)
                    method.invokeExact()
                    println()
                } else {
                    println("Year $year, Day $day: not yet")
                    lastWasShort = true
                }
            }
        }
    }
    if (lastWasShort) {
        println()
    }
    val status = if (TOTAL_FAILS == 0) "🟢" else "🔴 ($TOTAL_FAILS failed)"
    println("Total: $totalDays days in ${totalTime.inWholeSeconds} s $status")
}