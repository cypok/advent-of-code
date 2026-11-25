package year2024

import utils.*

// Task description:
//   https://adventofcode.com/2024/day/9

fun main() = runAoc {
    example {
        answer1(1928)
        answer2(2858)
        "2333133121414131402"
    }

    example("don't try to move files to the end") {
        answer1(1928)
        answer2(2858)
        "23331331214141314029"
    }

    solution1 {
        val line = lines.first()
        val content = mutableListOf<Int>()
        run {
            var nextIsFile = true
            var nextFileIdx = 0
            for (digitCh in line) {
                val digit = digitCh.digitToInt()
                val filler =
                    if (nextIsFile) {
                        nextFileIdx.also { nextFileIdx++ }
                    } else {
                        -1
                    }
                nextIsFile = !nextIsFile

                repeat(digit) {
                    content.add(filler)
                }
            }
        }

        var j = content.size - 1
        while (content[j] == -1) {
            j--
        }
        for (i in 0 until content.size) {
            if (content[i] != -1) continue
            if (i >= j) break

            content[i] = content[j]
            do {
                content[j] = -1
                j--
            } while (content[j] == -1)
        }
        content
            .takeWhile { it != -1 }
            .withIndex()
            .sumOf { [idx, block] ->
                1L * idx * block
            }
    }

    data class AreaDesc(var pos: Int, var size: Int, val id: Int)

    solution2 {
        val line = lines.first()

        val freeList = mutableListOf<AreaDesc>()
        val fileList = mutableListOf<AreaDesc>()
        run {
            var curPos = 0
            var nextIsFile = true
            var nextFileIdx = 0
            for (digitCh in line) {
                val size = digitCh.digitToInt()
                val [id, list] =
                    if (nextIsFile) {
                        nextFileIdx.also { nextFileIdx++ } to fileList
                    } else {
                        -1 to freeList
                    }
                nextIsFile = !nextIsFile

                list += AreaDesc(curPos, size, id)
                curPos += size
            }
        }

        fileList.asReversed().sumOf { file ->
            val filePos = freeList
                .asSequence()
                .takeWhile { it.pos < file.pos }
                .firstOrNull { it.size >= file.size }
                ?.let { free ->
                    free.pos.also {
                        free.pos += file.size
                        free.size -= file.size
                    }
                }
                ?: file.pos
            (0 until file.size).sumOf { 1L * (filePos + it) * file.id }
        }
    }
}