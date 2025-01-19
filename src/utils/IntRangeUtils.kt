@file:Suppress("unused")

package utils

import utils.SetComparison.*
import kotlin.math.max
import kotlin.math.min

val IntRange.size
    get() = last - first + 1

infix fun IntRange.intersect(that: IntRange): IntRange =
    IntRange(max(this.first, that.first), min(this.last, that.last))

enum class SetComparison {
    EQUAL, SUBSET, SUPERSET, PARTIALLY_EQUAL, DISJOINT,
}

infix fun IntRange.compare(that: IntRange): SetComparison =
    when {
        this.last < that.first || this.first > that.last -> DISJOINT
        this.first == that.first && this.last == that.last -> EQUAL
        this.first <= that.first && this.last >= that.last -> SUPERSET
        this.first >= that.first && this.last <= that.last -> SUBSET
        else -> PARTIALLY_EQUAL
    }
