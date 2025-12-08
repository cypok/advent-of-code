package year2025

import utils.*
import java.util.PriorityQueue
import kotlin.math.sqrt

// Task description:
//   https://adventofcode.com/2025/day/8

fun main() = runAoc {
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

    solution {
        val boxes = lines.map {
            val (x, y, z) = it.numbersAsInts()
            Point3(x, y, z)
        }
        val pairs = boxes.cartesianSquare()
            .filter { (a, b) -> a < b }
            .map { (a, b) -> BoxPair(a, b) }
            // sort of comparables works faster than sort with comparator
            .let { PriorityQueue(it.toList()) }

        val djSet = DisjointSet<Point3>()
        if (isPart1) {
            val connNum = (exampleParam as? Int) ?: 1000
            repeat(connNum) {
                val (a, b) = pairs.poll()
                djSet.union(a, b)
            }
            boxes.groupBy { djSet.find(it) }
                .values
                .map { it.size.toLong() }
                .sortedDescending()
                .take(3)
                .productOf { it }

        } else {
            while (true) {
                val (a, b) = pairs.poll()!!
                djSet.union(a, b)
                if (djSet.size(a) == boxes.size) {
                    return@solution a.x * b.x
                }
            }
        }
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

private class DisjointSet<E> {
    private val nodes = HashMap<E, Node<E>>()

    private fun node(e: E) = nodes.getOrPut(e) { Node(e) }

    private fun findNode(n: Node<E>): Node<E> {
        val p = n.parent
        if (p != n) {
            return findNode(p)
                .also { n.parent = it }
        }
        return n
    }

    private fun findNode(a: E): Node<E> =
        findNode(node(a))

    fun size(e: E): Int =
        if (e in nodes) findNode(e).size
        else 1

    fun find(e: E): E =
        if (e in nodes) findNode(e).data
        else e

    fun union(a: E, b: E) {
        val pa = findNode(a)
        val pb = findNode(b)
        if (pa != pb) {
            val size = pa.size + pb.size
            if (pa.size < pb.size) {
                pa.parent = pb
                pb.size = size
            } else {
                pb.parent = pa
                pa.size = size
            }
        }
    }
}

private class Node<E>(val data: E) {
    var parent: Node<E> = this
    var size = 1 // valid only for the top-most parent
}