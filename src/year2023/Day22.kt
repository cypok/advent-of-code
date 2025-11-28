package year2023

import utils.*
import kotlin.math.min

// Task description:
//   https://adventofcode.com/2023/day/22

private class P(val x: Int, val y: Int, val z: Int) {
    fun moved(dz: Int) =
        P(x, y, z + dz)

    override fun toString(): String = "$x,$y,$z"
}

private class Brick(val name: String, val p1: P, val p2: P) {

    val bottom
        get() = p1.z

    val height
        get() = p2.z - p1.z + 1

    fun lower(dz: Int = 1) =
        Brick(name, p1.moved(-dz), p2.moved(-dz))

    fun fall(supportHeight: Int) =
        lower(p1.z - supportHeight - 1)

    override fun toString(): String = "$name{$p1~$p2}"
}

private fun settleAndFindSupports(input: List<String>): Pair<MutableList<Brick>, Map<Brick, List<Brick>>> {
    val bricks = input.mapIndexed { idx, line ->
        val (x1, y1, z1, x2, y2, z2) = line.numbersAsInts()
        check(x1 <= x2 && y1 <= y2 && z1 <= z2)
        Brick(('A' + idx).toString(), P(x1, y1, z1), P(x2, y2, z2))
    }.sortedBy { it.bottom }

    val map = Array2D.of<Pair<Int, Brick?>>(1 + bricks.maxOf { it.p2.x }, 1 + bricks.maxOf { it.p2.y }) { 0 to null }

    val supportedBy = mutableListOf<Pair<Brick, Brick>>()

    val fallenBricks = mutableListOf<Brick>()
    for (b in bricks) {
        val supportHeight = (b.p1.x .. b.p2.x).maxOf { x ->
            (b.p1.y .. b.p2.y).maxOf { y ->
                map[x, y].first
            }
        }
        val fallen = b.fall(supportHeight)
        fallenBricks += fallen
        val supports = mutableSetOf<Brick>()
        (b.p1.x .. b.p2.x).forEach { x ->
            (b.p1.y .. b.p2.y).forEach { y ->
                val (sHeight, sBrick) = map[x, y]
                if (sHeight == supportHeight && sBrick != null) {
                    supports += sBrick
                }
                map[x, y] = (supportHeight + fallen.height) to fallen
            }
        }
        supports.forEach { supportedBy += fallen to it }
    }

    return Pair(
        fallenBricks,
        supportedBy.groupBy({ it.first }, { it.second }))
}

fun main() = runAoc {
    measureRunTime()
    example {
        answer1(5)
        answer2(7)
        """
            1,0,1~1,2,1
            0,0,2~2,0,2
            0,2,3~2,2,3
            0,0,4~0,2,4
            2,0,5~2,2,5
            0,1,6~2,1,6
            1,1,8~1,1,9
        """
    }

    solution1 {
        val (bricks, supportedBy) = settleAndFindSupports(lines)
        val criticalBricks = supportedBy
            .filterValues { it.size == 1 }
            .values
            .flatten()
            .distinct()
        bricks.size - criticalBricks.size
    }

    solution2 {
        val (bricks, supportedBy) = settleAndFindSupports(lines)
        bricks.sumOf { b ->
            val disintegrated = mutableSetOf(b)
            while (true) {
                val fallen = supportedBy.asSequence().filter { disintegrated.containsAll(it.value) }.map { it.key }
                if (!disintegrated.addAll(fallen)) {
                    break
                }
            }
            disintegrated.size - 1
        }
    }
}

