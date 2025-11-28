package year2023

object HelperKotlin {
    var CC5_slow: Comparator<Arrow> = Comparator.comparing {
        it.heat
    }

    var CC6_fast: Comparator<Arrow> = object : Comparator<Arrow> {
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

    val CCX = CC5_slow
}