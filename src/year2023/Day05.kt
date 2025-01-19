package year2023

import utils.*

fun main() = test(
    ::solve1,
    ::solve2,
)

private fun rangeByFirstAndLast(first: Long, last: Long) =
    LongRange(first, last)

private fun rangeByFirstAndCount(first: Long, count: Long) =
    rangeByFirstAndLast(first, first + count - 1)

private fun LongRange.count(): Long =
    last - first + 1

private fun LongRange.shifted(distance: Long): LongRange =
    rangeByFirstAndLast(first + distance, last + distance)

private fun processSeeds(input: List<String>, seedRanges: List<LongRange>): Long {
    val remaining = input.iterator()
    check(remaining.next().startsWith("seeds: "))
    check(remaining.next().isEmpty())

    var curIdRanges = seedRanges.toMutableList()

    while (remaining.hasNext()) {
        val prevIdRanges = curIdRanges
        val nextIdRanges = curIdRanges.toMutableList()

        remaining.next().let {
            check(it.endsWith(" map:"))
            if (false) {
                println("${it.substringBefore('-')} ids:")
                println(curIdRanges)
            }
        }
        while (remaining.hasNext()) {
            val line = remaining.next()
            if (line.isEmpty()) break

            val (dst, src, count) = line.numbers()
            var i = 0
            while (i < prevIdRanges.size) {
                val idRange = prevIdRanges[i]

                val srcRange = rangeByFirstAndCount(src, count)

                val kept = mutableListOf<LongRange>()
                fun keep(part: LongRange) = kept.add(part)

                var toMove: LongRange? = null
                fun move(part: LongRange) {
                    assert(toMove == null)
                    toMove = part
                }

                if (srcRange.last < idRange.first || idRange.last < srcRange.first) {
                    keep(idRange)

                } else if (srcRange.first <= idRange.first && idRange.last <= srcRange.last) {
                    move(rangeByFirstAndLast(
                        idRange.first,
                        idRange.last
                    ))

                } else if (srcRange.first <= idRange.first) {
                    assert(srcRange.last in idRange)
                    move(rangeByFirstAndLast(
                        idRange.first,
                        srcRange.last
                    ))
                    keep(rangeByFirstAndLast(
                        srcRange.last + 1,
                        idRange.last
                    ))

                } else if (idRange.last <= srcRange.last) {
                    assert(srcRange.first in idRange)
                    keep(rangeByFirstAndLast(
                        idRange.first,
                        srcRange.first - 1
                    ))
                    move(rangeByFirstAndLast(
                        srcRange.first,
                        idRange.last
                    ))

                } else {
                    assert(idRange.first < srcRange.first && srcRange.last < idRange.last)
                    keep(rangeByFirstAndLast(
                        idRange.first,
                        srcRange.first - 1
                    ))
                    move(rangeByFirstAndLast(
                        srcRange.first,
                        srcRange.last
                    ))
                    keep(rangeByFirstAndLast(
                        srcRange.last + 1,
                        idRange.last
                    ))
                }

                if (toMove != null) {
                    assert(kept.all { !it.isEmpty() })
                    assert(!toMove.isEmpty())
                    assert(kept.sumOf { it.count() } + toMove.count() == idRange.count())

                    assert(nextIdRanges[i] == prevIdRanges[i]) { "don't move range twice" }
                    prevIdRanges[i] = toMove
                    nextIdRanges[i] = toMove.shifted(dst - src)

                    prevIdRanges.addAll(kept)
                    nextIdRanges.addAll(kept)
                }

                i++
            }
        }
        curIdRanges = nextIdRanges
    }

    return curIdRanges.minOf { it.first }
}

private fun solve1(input: List<String>): Long {
    val seeds = input[0]
        .substringAfter("seeds: ")
        .words()
        .map { rangeByFirstAndCount(it.toLong(), 1) }

    return processSeeds(input, seeds)
}

private fun solve2(input: List<String>): Long {
    val seeds = input[0]
        .substringAfter("seeds: ")
        .numbers()
        .chunked(2)
        .map { (start, count) -> rangeByFirstAndCount(start, count) }

    return processSeeds(input, seeds)
}
