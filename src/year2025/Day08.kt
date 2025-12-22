package year2025

import utils.*
import java.util.PriorityQueue
import kotlin.math.sqrt

// Task description:
//   https://adventofcode.com/2025/day/8

fun main() = runAoc {
    measureRunTime()

    example {
        answer1(40, param = 10)
        answer2(25272)
        """
            162,817,812
            57,618,57
            906,360,560
            592,479,940
            352,342,300
            466,668,158
            542,29,236
            431,825,988
            739,650,466
            52,470,668
            216,146,977
            819,987,18
            117,168,530
            805,96,715
            346,949,466
            970,615,88
            941,993,340
            862,61,35
            984,92,344
            425,690,689
        """
    }

    fun SolutionContext.genericSolution(
        union: (Point3, Point3) -> Unit,
        allSets: (List<Point3>) -> Collection<Collection<Point3>>,
        setSize: (List<Point3>) -> Int,
    ): Long {
        val boxes = lines.map {
            val (x, y, z) = it.numbersAsInts()
            Point3(x, y, z)
        }
        val pairs = boxes.combinations()
            .map { (a, b) -> BoxPair(a, b) }
            // sort of comparables works faster than sort with comparator
            .let { PriorityQueue(it.toList()) }

        if (isPart1) {
            val connNum = (exampleParam as? Int) ?: 1000
            repeat(connNum) {
                val (a, b) = pairs.poll()
                union(a, b)
            }
            return allSets(boxes)
                .map { it.size.toLong() }
                .sortedDescending()
                .take(3)
                .productOf { it }

        } else {
            while (true) {
                val (a, b) = pairs.poll()!!
                union(a, b)
                if (setSize(boxes) == boxes.size) {
                    return 1L * a.x * b.x
                }
            }
        }
    }


    solution("disjoint-set") {
        val djSet = DisjointSet<Point3>()
        genericSolution(
            djSet::union,
            { it.groupBy { djSet.find(it) }.values },
            { djSet.size(it.first()) },
        )
    }

    solution("straightforward sets") {
        val sets = mutableListOf<MutableSet<Point3>>()

        fun find(a: Point3): MutableSet<Point3>? =
            sets.find { a in it }

        fun union(a: Point3, b: Point3) {
            val sa = find(a)
            val sb = find(b)
            if (sa != null && sb != null) {
                if (sa != sb) {
                    sa += sb
                    sets -= sb
                }
            } else if (sa != null) {
                sa += b
            } else if (sb != null) {
                sb += a
            } else {
                sets += mutableSetOf(a, b)
            }
        }

        genericSolution(
            ::union,
            { sets },
            { sets.first().size },
        )
    }
}

private class Point3(val x: Int, val y: Int, val z: Int) : Comparable<Point3> {
    override fun toString(): String = "$x,$y,$z"

    override fun compareTo(other: Point3): Int {
        if (this.x != other.x) return this.x compareTo other.x
        if (this.y != other.y) return this.y compareTo other.y
        return this.z compareTo other.z
    }

    infix fun straightDistanceTo(other: Point3): Double {
        val dx = (other.x - this.x).toDouble()
        val dy = (other.y - this.y).toDouble()
        val dz = (other.z - this.z).toDouble()
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

private data class BoxPair(val a: Point3, val b: Point3) : Comparable<BoxPair> {
    private val dist = a straightDistanceTo b

    override fun compareTo(other: BoxPair): Int =
        this.dist compareTo other.dist
}