package year2023

import utils.*
import kotlin.math.min

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private data class P(val x: Int, val y: Int, val z: Int) {
    fun moved(dz: Int) =
        P(x, y, z + dz)

    operator fun get(coordIdx: Int) = when (coordIdx) {
        0 -> x
        1 -> y
        2 -> z
        else -> error(coordIdx)
    }

    companion object {
        val COORDS = 0..2
    }
}

private data class Brick(val name: String, val p1: P, val p2: P) {
    val bottom
        get() = min(p1.z, p2.z)

    fun lower() =
        Brick(name, p1.moved(-1), p2.moved(-1))

    fun isAboveGround() =
        bottom > 0

    infix fun collideWith(that: Brick) =
        collide(this, that)

    companion object {
        fun collide(a: Brick, b: Brick): Boolean {
            return P.COORDS.all { c ->
                a.p1[c] <= b.p2[c] && b.p1[c] <= a.p2[c]
            }
        }
    }
}

private fun settleAndFindSupports(input: List<String>): Pair<MutableList<Brick>, Map<Brick, List<Brick>>> {
    val bricks = input.mapIndexed { idx, line ->
        val (x1, y1, z1, x2, y2, z2) = line.numbersAsInts()
        check(x1 <= x2 && y1 <= y2 && z1 <= z2)
        Brick(idx.toString(), P(x1, y1, z1), P(x2, y2, z2))
    }.sortedBy { it.bottom }

    assert(listOf(bricks, bricks).cartesianProduct()
        .filter { it[0] !== it[1] }
        .all { !(it[0] collideWith it[1]) })

    val supportedBy = mutableListOf<Pair<Brick, Brick>>()

    val fallenBricks = mutableListOf<Brick>()
    for (b in bricks) {
        var safe = b
        while (true) {
            val lowered = safe.lower()
            if (!lowered.isAboveGround()) {
                break
            }
            val supportingBricks = fallenBricks.filter { it collideWith lowered }
            if (supportingBricks.isNotEmpty()) {
                supportedBy += supportingBricks.map { safe to it }
                break
            }
            safe = lowered
        }
        fallenBricks += safe
    }

    return Pair(
        fallenBricks,
        supportedBy.groupBy({ it.first }, { it.second }))
}

private fun solve1(input: List<String>): Int {
    val (bricks, supportedBy) = settleAndFindSupports(input)
    val criticalBricks = supportedBy
        .filterValues { it.size == 1 }
        .values
        .flatten()
        .distinct()
    return bricks.size - criticalBricks.size
}

private fun solve2(input: List<String>): Int {
    val (bricks, supportedBy) = settleAndFindSupports(input)
    return bricks.sumOf { b ->
        val disintegrated = mutableSetOf(b)
        while (true) {
            val fallen = supportedBy.filterValues { disintegrated.containsAll(it) }.keys
            if (!disintegrated.addAll(fallen)) {
                break
            }
        }
        disintegrated.size - 1
    }
}
