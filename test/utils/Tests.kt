package utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class Tests {

    @Test
    fun testCartesianProduct() {
        assertEquals(
            listOf(listOf(1, 3, 4), listOf(1, 3, 5), listOf(2, 3, 4), listOf(2, 3, 5)),
            listOf(listOf(1, 2), listOf(3), listOf(4, 5)).cartesianProduct()
        )
    }

    @Test
    fun testPermutations() {
        assertEquals(
            setOf(
                listOf(1, 2, 3),
                listOf(1, 3, 2),
                listOf(2, 1, 3),
                listOf(2, 3, 1),
                listOf(3, 1, 2),
                listOf(3, 2, 1),
            ),
            listOf(1, 2, 3).permutations()
        )
    }

    @Test
    fun testGcdLcm() {
        assertEquals(5L, gcd(15, 10))
        assertEquals(5L, gcd(10, 15))
        assertEquals(30L, lcm(15, 10))
        assertEquals(30L, lcm(10, 15))
        assertEquals(1L, gcd(37, 1))
        assertEquals(1L, gcd(1, 37))
        assertEquals(37L, lcm(37, 1))
        assertEquals(37L, lcm(1, 37))
    }

    @Test
    fun testCycle() {
        assertEquals(listOf(1, 2, 3, 1, 2, 3, 1), listOf(1, 2, 3).cycle().take(7).toList())
    }

    @Test
    fun testArray2DWide() {
        val map = Array2D.fromLines( """
            abcd
            efgh
        """.trimIndent().lines())
        assertEquals(listOf("abcd", "efgh"), map.rows.map { it.asString() })
        assertEquals(listOf("ae", "bf", "cg", "dh"), map.cols.map { it.asString() })

        assertEquals(listOf("e", "af", "bg", "ch", "d"), map.diagonalsRight.map { it.asString() })
        assertEquals(listOf("a", "be", "cf", "dg", "h"), map.diagonalsLeft.map { it.asString() })
    }

    @Test
    fun testArray2DNarrow() {
        val map = Array2D.fromLines("""
            ae
            bf
            cg
            dh
        """.trimIndent().lines())
        assertEquals(listOf("d", "ch", "bg", "af", "e"), map.diagonalsRight.map { it.asString() })
        assertEquals(listOf("a", "eb", "fc", "gd", "h"), map.diagonalsLeft.map { it.asString() })
    }

    @Test
    fun testArray2DDiagonalsBig() {
        val map = Array2D.fromLines("""
            abcd
            efgh
            ijkl
        """.trimIndent().lines())
        assertEquals(listOf("i", "ej", "afk", "bgl", "ch", "d"), map.diagonalsRight.map { it.asString() })
        assertEquals(listOf("a", "be", "cfi", "dgj", "hk", "l"), map.diagonalsLeft.map { it.asString() })
    }

    @Test
    fun testListSplitHard() {
        val xxs = listOf(1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9).split(0)
        assertEquals(listOf(1, 2, 3), xxs.first().toList())
        assertEquals(listOf(7, 8, 9), xxs.drop(2).first().toList())
        assertEquals(listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)), xxs.toList())
    }

    @Test
    fun testParsingUtils() {
        assertEquals(listOf("abc", "def"), "abc   def".words())

        assertEquals(listOf(37L, -42L), "37   -42".numbers())
        assertEquals(listOf(37L, -42L), " 37   -42 ".numbers())
        assertEquals(listOf(37L, -42L), "37,-42".numbers())
        assertEquals(listOf(37L, 42L), "37 -> 42".numbers())
    }

    @Test
    fun testMultiSet() {
        val set = multiSetOf(10, 20, 20)

        assertEquals(1, set[10])
        assertEquals(2, set[20])
        assertEquals(0, set[30])

        assertEquals(setOf(10 to 1L, 20 to 2L), set.grouped.map { it.elem to it.count }.toSet())

        set.add(10, 100)
        assertEquals(101, set[10])

        set.add(30, 100)
        assertEquals(100, set[30])
    }

    @Test
    fun testCountWhile() {
        assertEquals(3, listOf(-10, -5, -3, 20, -5, -7).countWhile { it < 0 })
        assertEquals(0, listOf(10, -5, -3, 20, -5, -7).countWhile { it < 0 })
        assertEquals(6, listOf(-10, -5, -3, -20, -5, -7).countWhile { it < 0 })
        assertEquals(0, listOf<Int>().countWhile { it < 0 })

        assertEquals(2, listOf(-10, -5, -3, 20, -5, -7).countLastWhile { it < 0 })
        assertEquals(0, listOf(10, -5, -3, 20, -5, 7).countLastWhile { it < 0 })
        assertEquals(6, listOf(-10, -5, -3, -20, -5, -7).countLastWhile { it < 0 })
        assertEquals(0, listOf<Int>().countLastWhile { it < 0 })
    }

    @Test
    fun testListPair() {
        assertEquals(1 to 2, listOf(1, 2).pair())
        assertThrows<Exception> { emptyList<Int>().pair() }
        assertThrows<Exception> { listOf(1).pair() }
        assertThrows<Exception> { listOf(1, 2, 3).pair() }
        assertThrows<Exception> { listOf(1, 2, 3, 4).pair() }
    }

    @Test
    fun testListDisjointPairs() {
        assertEquals(listOf(10 to 20, 30 to 40), listOf(10, 20, 30, 40).disjointPairs())
        assertThrows<Exception> { listOf(10, 20, 30).disjointPairs() }
        assertThrows<Exception> { listOf(10).disjointPairs() }
    }

    @Test
    fun testListWindowedPairs() {
        assertEquals(listOf(10 to 20, 20 to 30), listOf(10, 20, 30).windowedPairs())
        assertEquals(emptyList<Nothing>(), listOf(10).windowedPairs())
    }
}
