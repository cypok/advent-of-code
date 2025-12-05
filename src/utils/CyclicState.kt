package utils

class CyclicState<T : Any>() {
    lateinit var current: T

    private var previous = mutableListOf<T>()

    constructor(initial: T) : this() {
        current = initial
    }

    fun tick(time: Int, newValue: T) {
        current = newValue
        tick(time)
    }

    fun tick(time: Int) {
        require(previous.size + 1 == time) { "only sequential ticking, starting from one" }
        previous += current
    }

    fun takeLast(count: Int): List<T> =
        previous.takeLast(count)

    fun detectCycle(): Int? =
        detectCycle(previous)

    fun extrapolateUntil(futureTime: Long): T {
        val cycle = detectCycle()
        check(cycle != null)
        val lastTime = previous.size.toLong()
        require(futureTime > lastTime)
        return previous[
            previous.size - 1
                    - cycle
                    + ((futureTime - lastTime) % cycle).toIntExact()]
    }
}

fun <T : Any> Collection<CyclicState<T>>.tickAll(time: Int): Unit =
    forEach { it.tick(time) }

fun <T : Any> Collection<CyclicState<T>>.detectCommonCycle(): Long? =
    fold(1L as Long?) { c, s ->
        if (c == null) {
            null
        } else {
            s.detectCycle()?.let { lcm(it.toLong(), c) }
        }
    }


fun <T> detectCycle(values: List<T>, skipInitPercent: Double = 0.1): Int? {
    fun getLast(idxFromEnd: Int) =
        values[values.size - 1 - idxFromEnd]

    fun isCycle(len: Int): Boolean {
        for (i in len until (values.size * (1 - skipInitPercent)).toInt()) {
            if (getLast(i) != getLast(i % len)) {
                return false
            }
        }
        return true
    }

    return (1 until values.size / 2)
        .firstOrNull { isCycle(it) }
}

class CyclicLinearGrowingState {
    var current = Int.MIN_VALUE

    private var lastTime = 0
    private val speeds = CyclicState<Int>()

    fun tick(time: Int, newValue: Int) {
        require(time == lastTime + 1) { "only sequential ticking, starting from one" }
        if (time > 1) {
            speeds.tick(time - 1, newValue - current)
        }
        lastTime = time
        current = newValue
    }

    fun hasCycle(): Boolean =
        speeds.detectCycle() != null

    fun extrapolateUntil(futureTime: Long): Long {
        val cycle = speeds.detectCycle()!!
        val speedsProCycle = speeds.takeLast(cycle)

        val remainingTime = (futureTime - lastTime)

        return current +
                speedsProCycle.sum() * (remainingTime / cycle) +
                speedsProCycle.take(
                    (remainingTime % cycle.toLong()).toIntExact()
                ).sum()

    }
}

