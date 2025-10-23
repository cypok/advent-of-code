package year2023

import utils.*

@Suppress("DEPRECATION")
fun main() = test(
    { process(it, withJoker = false) },
    { process(it, withJoker = true) },
)

private enum class HandType {
    // weakest
    HIGH_CARD,
    ONE_PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    FIVE_OF_A_KIND,
    // strongest
}

private data class HandInfo(
    val hand: String,
    val type: HandType,
    val labelsStrength: Long,
    val score: Long,
) : Comparable<HandInfo> {
    override fun compareTo(other: HandInfo): Int =
        comparator.compare(this, other)

    companion object {
        private val comparator =
            Comparator.comparing<HandInfo, HandType> { it.type }
                .thenComparing<Long> { it.labelsStrength }
    }
}

private fun process(input: List<String>, withJoker: Boolean): Long {
    fun detectType(hand: String): HandType {
        val groups = hand.groupBy { it }.toMutableMap()
        val jokersCount = if (withJoker) groups.remove('J')?.size ?: 0 else 0
        val groupedCards = groups.values.sortedBy { -it.size }
        val szs = groupedCards.map { it.size }
        return if (szs.getOrElse(0) { 0 } + jokersCount == 5) {
            HandType.FIVE_OF_A_KIND
        } else if (szs[0] + jokersCount == 4) {
            HandType.FOUR_OF_A_KIND
        } else if (szs[0] + jokersCount == 3) {
            if (szs[1] == 2) {
                HandType.FULL_HOUSE
            } else {
                HandType.THREE_OF_A_KIND
            }
        } else if (szs[0] + jokersCount == 2) {
            if (szs[1] == 2) {
                HandType.TWO_PAIR
            } else {
                HandType.ONE_PAIR
            }
        } else {
            HandType.HIGH_CARD
        }
    }

    fun labelStrength(l: Char): Int =
        if (l.isDigit()) l.digitToInt()
        else when (l) {
            'T' -> 10
            'J' -> if (withJoker) 1 else 11
            'Q' -> 12
            'K' -> 13
            'A' -> 14
            else -> error(l)
        }

    fun labelsStrength(hand: String): Long {
        val q = 15
        val strengths = hand.map { labelStrength(it) }
        assert(strengths.all { it in 0 until q })
        return strengths.fold(1) { acc, s -> acc * q + s }
    }

    return input.map {
        val (hand, score) = it.words()
        HandInfo(hand, detectType(hand), labelsStrength(hand), score.toLong())
    }
        .sorted()
        .withIndex()
        .sumOf { (idx, handInfo) -> handInfo.score * (idx + 1) }
}
