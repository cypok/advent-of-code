package year2023

import java.util.function.Function

object HelperKotlin {
    var CC5: Comparator<Arrow> = Comparator.comparing {
        it.heat
    }

    var CC6: Comparator<Arrow> = object : Comparator<Arrow> {
        private val f: (Arrow) -> Long = {
            it.heat
        }

        private val keyExtractor: (Arrow) -> Long = {
            f(it)
        }

        override fun compare(o1: Arrow, o2: Arrow): Int {
            return keyExtractor(o1).compareTo(keyExtractor(o2))
        }
    }

    val CCX = CC5
}