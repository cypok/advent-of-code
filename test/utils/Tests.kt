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
    fun testCartesianSquare() {
        assertEquals(
            listOf(
                1 to 1, 1 to 2, 1 to 3,
                2 to 1, 2 to 2, 2 to 3,
                3 to 1, 3 to 2, 3 to 3,
            ),
            listOf(1, 2, 3).cartesianSquare().toList()
        )
    }

    @Test
    fun testCombinations() {
        assertEquals(
            listOf(
                1 to 2, 1 to 3, 2 to 3,
            ),
            listOf(1, 2, 3).combinations().toList()
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
        assertEquals(listOf("abc,", "def"), "  abc,   def\t".words())

        assertEquals(listOf(37L, -42L), "37   -42".numbers())
        assertEquals(listOf(37L, -42L), " 37   -42 ".numbers())
        assertEquals(listOf(37L, -42L), "37,-42".numbers())
        assertEquals(listOf(37L, 42L), "37 -> 42".numbers())

        assertEquals(listOf(37L, 42L), "37-42".numbers())
        assertEquals(listOf(37L, -42L), "37 -42".numbers())
        assertEquals(listOf(37L, 42L), "37 - 42".numbers())
        assertEquals(listOf(-42L), "-42".numbers())
        assertEquals(listOf(-42L), " -42".numbers())
    }

    @Test
    fun testMultiSet() {
        val set = multiSetOf(10, 20, 20)

        assertEquals(1, set[10])
        assertEquals(2, set[20])
        assertEquals(0, set[30])

        fun <E> MultiSet<E>.toSetWithCounts(): Set<Pair<E, Int>> =
            grouped.map { it.elem to it.count.toIntExact() }.toSet()

        assertEquals(setOf(10 to 1, 20 to 2), set.toSetWithCounts())

        set.add(10, 100)
        assertEquals(101, set[10])

        set.add(30, 100)
        assertEquals(100, set[30])

        assertEquals(
            setOf("AA" to 6, "aa" to 12, "BB" to 3, "bb" to 6),
            multiSetOf("aa", "aa", "aA", "Aa", "Aa", "Aa", "bB", "Bb", "Bb")
                .flatMap { listOf(it.uppercase(), it.lowercase(), it.lowercase()) }
                .toSetWithCounts())
    }

    @Test
    fun testWorkList() {
        val wl = WorkList(1, 2, 3)
        assertEquals(
            listOf(1, 4, 9, 16, 25, 36, 49),
            wl.asSequence()
                .map {
                    if (it < 6) {
                        wl += it + 1
                        wl += it + 2
                    }
                    it * it
                }.toList())
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

    @Test
    fun testPoints() {
        for ((r, c) in listOf(37 to 42, 0 to 0, -10 to -20)) {
            val p = r x c
            assertEquals(r, p.row)
            assertEquals(c, p.col)
            assertEquals((r - 1) x c, p.moveInDir(Dir.UP))
            assertEquals((r + 1) x c, p.moveInDir(Dir.DOWN))
            assertEquals(r x (c - 1), p.moveInDir(Dir.LEFT))
            assertEquals(r x (c + 1), p.moveInDir(Dir.RIGHT))
        }
    }

    @Test
    fun testCyclicState() {
        val s = CyclicState(0)
        s.tick(1, 10)
        s.tick(2, 20)
        s.tick(3, 30)
        s.tick(4, 10)
        assertEquals(null, s.detectCycle())
        s.tick(5, 20)
        s.tick(6, 30)
        s.tick(7, 10)
        s.tick(8, 20)
        assertEquals(3, s.detectCycle())
        assertEquals(30, s.extrapolateUntil(9))
        assertEquals(10, s.extrapolateUntil(10))
        assertEquals(20, s.extrapolateUntil(11))
        assertEquals(10, s.extrapolateUntil(1_000_000_000_000_000L))
    }

    @Test
    fun testLinearCyclicState() {
        val s = CyclicLinearGrowingState()
        s.tick(1, 10)
        s.tick(2, 15)
        s.tick(3, 20)
        s.tick(4, 30)
        assertFalse(s.hasCycle())
        s.tick(5, 35)
        s.tick(6, 40)
        s.tick(7, 50)
        s.tick(8, 55)
        s.tick(9, 60)
        assertTrue(s.hasCycle())
        assertEquals(70, s.extrapolateUntil(10))
        assertEquals(75, s.extrapolateUntil(11))
        assertEquals(80, s.extrapolateUntil(12))
        assertEquals(6_666_666_666_666_670, s.extrapolateUntil(1_000_000_000_000_000L))
    }

    @Test
    fun testDisjointSet() {
        val s = DisjointSet<Int>()
        assertEquals(1, s.size(37))
        assertEquals(37, s.find(37))

        s.union(37, 42)
        assertEquals(2, s.size(37))
        assertEquals(2, s.size(42))
        assertEquals(s.find(37), s.find(42))
        assertNotEquals(s.find(37), s.find(99))

        s.union(10, 20)
        s.union(20, 30)
        assertEquals(3, s.size(10))

        s.union(42, 20)
        assertEquals(5, s.size(37))
        assertEquals(s.find(37), s.find(30))
    }
}
