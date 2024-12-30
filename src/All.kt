import utils.IS_BATCH_RUN
import utils.cartesianProduct
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.time.Year

fun main() {
    IS_BATCH_RUN = true

    val lookup = MethodHandles.lookup()
    val mainName = "main"
    val mainMT = MethodType.methodType(Class.forPrimitiveName("void"))
    var lastWasShort = false
    for (year in 2015..Year.now().value) {
        for (day in 1..25) {
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