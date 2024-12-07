package year2024

import utils.*
import java.util.Comparator

// Task description:
//   https://adventofcode.com/2024/day/5

fun main() = runAoc {
    example {
        answer1(143)
        answer2(143)
        """
            47|53
            97|13
            97|61
            97|47
            75|29
            61|13
            75|53
            29|13
            97|29
            53|29
            61|53
            97|53
            61|29
            47|13
            75|47
            97|75
            47|61
            75|61
            47|29
            75|13
            53|13

            75,47,61,53,29
            97,61,53,29,13
            75,29,13
            75,97,47,61,53
            61,13,29
            97,13,75,29,47
        """
    }

    solution1 { solve(lines, fixing = false) }
    solution2 { solve(lines, fixing = true) }
}

private fun solve(input: List<String>, fixing: Boolean): Long {
    val (rulesLines, updates) = input.splitByEmptyLines().toList()
    val rules = rulesLines.map {
        it.numbers().let { (a, b) -> a to b }
    }.toSet()

    return updates.sumOf { update ->
        val pages = update.numbers()
        val right = isRightOrder(pages, rules)
        if (fixing) {
            if (right) {
                0
            } else {
                pages.sortedWith(comparatorByRules(rules)).middle()
            }
        } else {
            if (right) {
                pages.middle()
            } else {
                0
            }
        }.toLong()
    }
}

private fun isRightOrder(pages: List<Long>, rules: Set<Pair<Long, Long>>): Boolean {
    for (i in 0..<pages.size) {
        for (j in i+1..<pages.size) {
            if ((pages[j] to pages[i]) in rules) {
                return false
            }
        }
    }
    return true
}

private fun comparatorByRules(rules: Set<Pair<Long, Long>>) =
    Comparator<Long> { x, y ->
        if ((x to y) in rules) {
            -1
        } else if ((y to x) in rules) {
            1
        } else {
            0
        }
    }