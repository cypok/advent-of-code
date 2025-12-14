package utils

import kotlin.math.absoluteValue
import kotlin.math.sign

class Rat(numer: Int, denom: Int = 1) : Comparable<Rat> {
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

    override operator fun compareTo(that: Rat): Int = (this.n * that.d) compareTo (that.n * this.d)

    companion object {
        val ZERO = Rat(0)
        val ONE = Rat(1)
    }
}
