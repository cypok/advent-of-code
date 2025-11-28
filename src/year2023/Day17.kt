package year2023

import utils.*
import utils.Dir.*
import year2022.main2
import java.util.PriorityQueue
import kotlin.math.sign

@Suppress("DEPRECATION")
fun main() {
    main3()
    main2()
    main3()
}

fun main3() = runAoc {
    if (false) solution1 {
        solve(lines, 0, 3)
    }
    solution2 {
        solve(lines, 4, 10)
    }
}

data class Arrow(val dst: Point, val dir: Dir, val len: Int, val heat: Long, val prev: Arrow?) : Comparable<Arrow> {
    override fun compareTo(other: Arrow): Int =
        (this.heat - other.heat).sign
}

private val DIR_COUNT = Dir.entries.size

private class Mark(maxLen: Int) {
    val heatByDirAndLen = Array(DIR_COUNT * (maxLen + 1)) { Long.MAX_VALUE }

    operator fun get(arr: Arrow): Long {
        return heatByDirAndLen[arr.dir.ordinal + DIR_COUNT * arr.len]
    }

    operator fun set(arr: Arrow, heat: Long) {
        heatByDirAndLen[arr.dir.ordinal + DIR_COUNT * arr.len] = heat
    }
}

private fun solve(input: List<String>, minLen: Int, maxLen: Int): Long {
    val heatMap = Array2D.fromLines(input)

    val marksMap = Array2D.of(heatMap.height, heatMap.width) { Mark(maxLen) }

    val cc4 = HelperJava.CC4
    val cc5 = HelperKotlin.CC5_slow
    val cc6 = HelperKotlin.CC6_fast
    val ccX = HelperKotlin.CCX
    val queue = PriorityQueue<Arrow>(ccX)

    fun isWorthy(arr: Arrow): Boolean {
        val prevHeat = marksMap[arr.dst][arr]
        return arr.heat < prevHeat
    }

    val initialHeat = 0L
    fun go(arr: Arrow) {
        if (arr.len > maxLen) return
        if (heatMap.getOrNull(arr.dst) == null) return
        if (!isWorthy(arr)) return

        queue += arr
    }

    go(Arrow(0 x 1, RIGHT, 1, initialHeat, null))
    go(Arrow(1 x 0, DOWN, 1, initialHeat, null))

    while (true) {
        val arr = queue.poll()
        if (!isWorthy(arr)) continue

        val newHeat = arr.heat + heatMap[arr.dst].digitToInt()

        if (arr.len >= minLen) {
            marksMap[arr.dst][arr] = arr.heat

            if (arr.dst.row == heatMap.height - 1 && arr.dst.col == heatMap.width - 1) {
                return newHeat
            }
        }

        fun turn(newDir: Dir) {
            go(Arrow(arr.dst.moveInDir(newDir), newDir,
                1, newHeat,
                arr))
        }
        if (arr.len >= minLen) {
            turn(arr.dir.left)
            turn(arr.dir.right)
        }

        go(Arrow(arr.dst.moveInDir(arr.dir), arr.dir,
            arr.len + 1, newHeat,
            arr))
    }
}
