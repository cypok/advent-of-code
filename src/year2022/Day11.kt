package year2022

import utils.*

// Task description:
//   https://adventofcode.com/2022/day/11

fun main() = runAoc {
    example {
        answer1(10605)
        answer2(2713310158)
        """
            Monkey 0:
              Starting items: 79, 98
              Operation: new = old * 19
              Test: divisible by 23
                If true: throw to monkey 2
                If false: throw to monkey 3

            Monkey 1:
              Starting items: 54, 65, 75, 74
              Operation: new = old + 6
              Test: divisible by 19
                If true: throw to monkey 2
                If false: throw to monkey 0

            Monkey 2:
              Starting items: 79, 60, 97
              Operation: new = old * old
              Test: divisible by 13
                If true: throw to monkey 1
                If false: throw to monkey 3

            Monkey 3:
              Starting items: 74
              Operation: new = old + 3
              Test: divisible by 17
                If true: throw to monkey 0
                If false: throw to monkey 1
        """
    }

    solution {
        val monkeys = run {
            lines.splitByEmptyLines().withIndex().map { (i, monkeyDesc) ->
                val (idStr, itemsStr, operationStr, test, thenIdStr, elseIdStr) = monkeyDesc
                val id = idStr.numbersAsInts().single().also { check(it == i) }
                val items = itemsStr.numbers()
                val thenId = thenIdStr.numbersAsInts().single()
                val elseId = elseIdStr.numbersAsInts().single()
                check(thenId != id && elseId != id) // to make things easier
                check(thenId != elseId) // otherwise we can optimize

                val (lStr, opStr, rStr) = operationStr.words().takeLast(3)
                check(lStr == "old")
                val operationRaw = arithOpByChar(opStr)
                val operationArgOrOld = when (rStr) {
                    "old" -> null
                    else -> rStr.toLong()
                }
                val inspectOperation = { it: Long -> operationRaw(it, operationArgOrOld ?: it) }

                val testDivider = test.trim().numbersAsInts().single()

                Monkey(id, items, inspectOperation, testDivider, thenId, elseId)
            }.toList()
        }

        // Used only for part 2, because in part 1 we have division, which is not good for modular arithmetic.
        val modulus = monkeys.map { it.testDivider.toLong() }.reduce(::lcm)

        repeat(if (isPart1) 20 else 10000) {
            for (m in monkeys) {
                m.inspectionCounter += m.items.size
                for (i in m.items) {
                    val inspected = m.inspectOperation(i)
                    val newItem = if (isPart1) inspected / 3 else inspected % modulus
                    val targetMonkey = if (newItem % m.testDivider == 0L) m.thenId else m.elseId
                    assert(targetMonkey != m.id)
                    monkeys[targetMonkey].items += newItem
                }
                m.items.clear()
            }
        }

        monkeys
            .map { it.inspectionCounter }
            .sorted()
            .takeLast(2)
            .productOf { it.toLong() }
    }
}

private class Monkey(
    val id: Int,
    items: List<Long>,
    val inspectOperation: (Long) -> Long,
    val testDivider: Int,
    val thenId: Int,
    val elseId: Int,
) {
    val items = items.toMutableList()
    var inspectionCounter = 0
}