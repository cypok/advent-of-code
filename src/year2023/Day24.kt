package year2023

import utils.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile
import kotlin.io.path.pathString
import kotlin.io.path.readText
import kotlin.io.path.writeLines
import kotlin.math.abs

// Task description:
//   https://adventofcode.com/2023/day/24

fun main() = runAoc {

    data class Vec(val x: Long, val y: Long, val z: Long)

    data class VecBuilder(val x: Long, val y: Long)
    infix fun Long.x(that: Long) = VecBuilder(this, that)
    infix fun VecBuilder.x(that: Long) = Vec(this.x, this.y, that)

    data class Stone(val p: Vec, val v: Vec)

    fun SolutionContext.parse(): List<Stone> {
        val stones = lines.map {
            val (px, py, pz, vx, vy, vz) = it.numbers()
            Stone(px x py x pz, vx x vy x vz)
        }
        return stones
    }

    fun runZ3Script(content: Sequence<String>): Result<Pair<String, String>> {
        val z3PythonScript = createTempFile("aoc-script", ".py").apply {
            writeLines(sequence {
                yield("from z3 import *")
                yieldAll(content)
            })
        }
        val stdout = createTempFile("aoc-result", ".txt")
        val stderr = createTempFile("aoc-error", ".txt")
        val pb = ProcessBuilder("python3", z3PythonScript.pathString)
            .redirectOutput(stdout.toFile())
            .redirectError(stderr.toFile())
        return runCatching {
            pb.start().waitFor(5, TimeUnit.SECONDS)
            stdout.readText().trim() to stderr.readText().trim()
        }
    }

    solution1 {

        fun intersectXY(a: Stone, b: Stone): Triple<Pair<Double, Double>, Double, Double>? {
            val apx = a.p.x
            val apy = a.p.y
            val avx = a.v.x
            val avy = a.v.y
            val bpx = b.p.x
            val bpy = b.p.y
            val bvx = b.v.x
            val bvy = b.v.y

            val div = bvx * avy - bvy * avx
            if (div == 0L) return null

            val tb = 1.0 * ((bpy - apy) * avx - (bpx - apx) * avy) / div
            val ta = if (avx != 0L) (bpx + tb*bvx - apx) / avx else (bpy + tb*bvy - apy) / avy

            val rx = apx + ta * avx
            val ry = apy + ta * avy

            assert(abs((rx - (bpx + tb * bvx))/rx) < 0.0001)
            assert(abs((ry - (bpy + tb * bvy))/ry) < 0.0001)

            return Triple(Pair(rx, ry), ta, tb)
        }

        val min = 200_000_000_000_000
        val max = 400_000_000_000_000

        val stones = parse()

        (0 until stones.size).sumOf { i ->
            val si = stones[i]
            (i + 1 until stones.size).sumOf { j ->
                val sj = stones[j]

                val (xy, ti, tj) = intersectXY(si, sj) ?: return@sumOf 0L // don't count parallel
                if (ti < 0 || tj < 0) return@sumOf 0L // don't count past

                val (x, y) = xy
                if (x < min || x > max || y < min || y > max) return@sumOf 0L // don't count out of range

                1L
            }
        }
    }

    solution2 {
        val check = runZ3Script(sequenceOf("print('it works')"))
        check.exceptionOrNull()?.let {
            printExtra(it)
            return@solution2 ignoredAnswer("python z3 unavailable")
        }
        check.getOrThrow().let { (out, err) ->
            if (out != "it works") {
                if (out.isNotBlank()) printExtra("stdout:\n$out")
                printExtra("stderr:\n$err")
                return@solution2 ignoredAnswer("python z3 unavailable")
            }
        }

        val stones = parse()

        // No interest to solve algebra myself.
        runZ3Script(sequence {
            yield("from z3 import *")
            yield("px,py,pz,vx,vy,vz = Reals('px py pz vx vy vz')")
            yield("t1,t2,t3,t4 = Reals('t1 t2 t3 t4')")
            yield("s = Solver()")
            yield("s.add(")
            for ((idx, s) in stones.take(4).withIndex()) {
                val t = "t${idx + 1}"
                yield("${s.p.x} + (${s.v.x}) * $t == px + vx * $t ,")
                yield("${s.p.y} + (${s.v.y}) * $t == py + vy * $t ,")
                yield("${s.p.z} + (${s.v.z}) * $t == pz + vz * $t ,")
            }
            yield(")")
            yield("s.check()")
            yield("m = s.model()")
            yield("print(m[px].as_long() + m[py].as_long() + m[pz].as_long())")
            yield("exit()")
        }).getOrThrow().first.toLong()
    }
}
