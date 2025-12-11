package year2025

import utils.*
import year2025.Rat.Companion.ONE
import year2025.Rat.Companion.ZERO
import java.time.Instant
import kotlin.collections.map
import kotlin.math.absoluteValue
import kotlin.math.sign

// Task description:
//   https://adventofcode.com/2025/day/10

fun main() = runAoc {
    example {
        answer1(7)
        answer2(33)
        """
            [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
            [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
            [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
        """
    }

    solution2("linear algebra") {
        lines.sumOf { machineDesc ->
            // buttons = (3) (1,3) (2) (2,3) (0,2) (0,1)
            val buttons = machineDesc.substringAfter("] ").substringBefore(" {").split(" ").map { it.numbersAsInts() }
            // targetState = {3,5,4,7}
            val targetState = machineDesc.substringAfter("{").numbersAsInts().toIntArray()

            val n = buttons.size
            val m = targetState.size

            val matrix = Array(m) { Array(n + 1) { ZERO } }
            buttons.withIndex().forEach { (j, ss) -> ss.forEach { matrix[it][j] = ONE } }
            targetState.withIndex().forEach { (i, sum) -> matrix[i][n] = Rat(sum, 1) }

            val usedRow = Array(m) { false }
            val mainRowForCol = Array(n) { -1 }
            for (j in 0..<n) {
                val row = (0..<m).firstOrNull() { i -> !usedRow[i] && matrix[i][j].isNotZero }
                if (row == null) {
                    continue
                }
                mainRowForCol[j] = row
                usedRow[row] = true

                // make it start from 1
                run {
                    val elem = matrix[row][j]
                    (0..n).forEach {
                        matrix[row][it] /= elem
                    }
                }

                for (i in 0..<m) {
                    if (i == row) continue
                    val v = matrix[i][j]
                    if (v.isZero) continue
                    (0..n).forEach {
                        matrix[i][it] -= v * matrix[row][it]
                    }
                }
            }
            val sum = (0..n).map { j ->
                (0..<m).fold(ZERO) { acc, i -> acc + matrix[i][j] }
            }
            val freeVars = (0..<n).filter { j -> mainRowForCol[j] == -1 }
            val diffs = freeVars.map { j ->
                ONE - sum[j]
            } + sum[n]
            val nonFreeVars = (0..<n).filter { j -> mainRowForCol[j] != -1 }
            val nonFreeVarNonNegConditions = nonFreeVars.map { j ->
                val row = mainRowForCol[j]
                freeVars.map { jj -> -matrix[row][jj] } + matrix[row][n]
            }

            fun calcIntSum(line: List<Rat>, freeVarValues: List<Int>): Int? {
                assert(line.size == freeVarValues.size + 1)
                var value = line.last()
                for (k in freeVarValues.indices) {
                    value += line[k] * freeVarValues[k]
                }
                return if (value.isInteger) value.toIntExact() else null
            }

            // TODO: performance: optimize 0 and 1 freeVar, think about 2 and 3
            var curMin = Int.MAX_VALUE
            freeVars.map { 0..200 }.cartesianProduct().forEach { fvs ->
                val sumValue = calcIntSum(diffs, fvs)
                if (sumValue == null || sumValue > curMin) return@forEach

                val conditionsGood = nonFreeVarNonNegConditions.all { conditions ->
                    val nonFreeValue = calcIntSum(conditions, fvs)
                    nonFreeValue != null && nonFreeValue >= 0
                }
                if (conditionsGood) {
                    curMin = sumValue
                }
            }
            check(curMin < Int.MAX_VALUE)
            curMin
        }
    }

    // part 2 is too slow
    solution1("brute-force") {
        lines.withIndex().toList().stream().parallel().map { (machineIdx, machineDesc) ->
            val buttons = machineDesc.substringAfter("] ").substringBefore(" {").split(" ").map { it.numbersAsInts() }

            if (isPart1) {
                val targetState = machineDesc.substringBefore("]").substringAfter("[").map {
                    when (it) {
                        '.' -> false
                        '#' -> true
                        else -> error(it)
                    }
                }.toBooleanArray()
                buttons.map { listOf(false, true) }.cartesianProduct().minOf { buttonPresses ->
                    val state = BooleanArray(targetState.size)
                    (buttons zip buttonPresses).forEach { (b, p) ->
                        if (p) b.forEach { state[it] = !state[it] }
                    }
                    if (state contentEquals targetState) buttonPresses.count { it } else Int.MAX_VALUE
                }

            } else {
                println("#$machineIdx/${lines.size} $machineDesc   @ ${Instant.now()}")
                // buttons = (3) (1,3) (2) (2,3) (0,2) (0,1)
                // buttonsPower = 1 2 1 2 2 2
                val buttonsPower = buttons.map { it.size }
                // targetState = {3,5,4,7}
                val targetState = machineDesc.substringAfter("{").numbersAsInts().toIntArray()
                // maxPressesOfButton = 7 5 4 4 3 3
                val maxPressesOfButton = buttons.map {
                    it.minOf { targetState[it] }
                }
                // feasibleMinPresses = 3, coarse but true
                val feasibleMinPresses = targetState.min()
                // targetStatePower = 19
                val targetStatePower = targetState.sum()

                val buttonsCount = buttons.size
                var curMinPresses = Int.MAX_VALUE
                maxPressesOfButton.map { 0..it }.cartesianProduct().forEach { buttonPresses ->
                    // 1 3 0 3 1 2
                    val presses = buttonPresses.sum()
                    if (presses > curMinPresses) return@forEach
                    if (presses < feasibleMinPresses) return@forEach
                    var checkSum = 0
                    for (i in 0 until buttonsCount) {
                        val b = buttonsPower[i]
                        val p = buttonPresses[i]
                        checkSum += b * p
                        if (checkSum > targetStatePower) return@forEach
                    }
                    if (checkSum != targetStatePower) return@forEach
                    val state = IntArray(targetState.size)
                    for (i in 0 until buttonsCount) {
                        val b = buttons[i]
                        val p = buttonPresses[i]
                        for (j in 0 until b.size) {
                            val s = b[j]
                            state[s] += p
                            if (state[s] > targetState[s]) return@forEach
                        }
                    }
                    if (state contentEquals targetState) {
                        curMinPresses = presses
                    }
                }
                curMinPresses
            }
        }.toList().sum()
    }
}

private class Rat (numer: Int, denom: Int = 1) {
    private val n: Int
    private val d: Int

    init {
        require(denom != 0)
        if (numer == 0) {
            n = 0
            d = 1
        } else {
            val gcd = gcd(numer.absoluteValue, denom.absoluteValue) * denom.sign
            n = numer / gcd
            d = denom / gcd
        }
    }

    override fun toString() = if (d == 1) "$n" else "$n/$d"

    override fun equals(other: Any?): Boolean =
        (this === other) || (other is Rat && this.n == other.n && this.d == other.d)

    override fun hashCode(): Int =
        31 * d + n


    val isZero: Boolean get() = n == 0
    val isNotZero: Boolean get() = n != 0

    val isInteger: Boolean get() = d == 1
    fun toIntExact(): Int = n.also { require(isInteger) }

    operator fun unaryPlus(): Rat = this
    operator fun unaryMinus(): Rat = Rat(-n, d)

    operator fun plus(that: Rat): Rat  = Rat(this.n * that.d + that.n * this.d, this.d * that.d)
    operator fun minus(that: Rat): Rat = Rat(this.n * that.d - that.n * this.d, this.d * that.d)
    operator fun times(that: Rat): Rat = Rat(this.n * that.n                  , this.d * that.d)
    operator fun times(that: Int): Rat = Rat(this.n * that                    , this.d         )
    operator fun div(that: Rat): Rat   = Rat(this.n * that.d                  , this.d * that.n)
    operator fun div(that: Int): Rat   = Rat(this.n                           , this.d * that  )

    infix operator fun compareTo(that: Rat): Int = (this.n * that.d) compareTo (that.n * this.d)

    companion object {
        val ZERO = Rat(0)
        val ONE = Rat(1)
    }
}
