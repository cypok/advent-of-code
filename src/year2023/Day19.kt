package year2023

import utils.*

@Suppress("DEPRECATION")
fun main() = test(
    ::solve1,
    ::solve2,
)

private data class Rule(
    val name: String,
    val commands: List<Command>,
)

private sealed class Command {
    abstract val nextRuleName: String

    abstract fun test(p: Part): Boolean
    abstract fun partition(p: PartRange): Pair<PartRange?, PartRange?>

    data class Unconditional(
        override val nextRuleName: String,
    ) : Command() {
        override fun test(p: Part) = true
        override fun partition(p: PartRange) = Pair(p, null)
    }

    data class Conditional(
        val checkedComponent: Char,
        val condition: Char,
        val value: Int,
        override val nextRuleName: String,
    ) : Command() {

        override fun test(p: Part): Boolean {
            val actualValue = p[checkedComponent]!!
            return when (condition) {
                '<' -> actualValue < value
                '>' -> actualValue > value
                else -> error(this)
            }
        }

        override fun partition(p: PartRange): Pair<PartRange?, PartRange?> {
            val actualRange = p[checkedComponent]!!
            val posAndNeg = when (condition) {
                '<' -> listOf(IntRange(MIN_VALUE, value - 1), IntRange(value, MAX_VALUE))
                '>' -> listOf(IntRange(value + 1, MAX_VALUE), IntRange(MIN_VALUE, value))
                else -> error(this)
            }
            val (pos, neg) = posAndNeg.map { filtered ->
                (p + (checkedComponent to (filtered intersect actualRange))).validOrNull
            }
            return Pair(pos, neg)
        }
    }
}


private typealias Part = Map<Char, Int>
private typealias PartRange = Map<Char, IntRange>

private val PartRange.isValid: Boolean
    get() = values.all { !it.isEmpty() }

private val PartRange.validOrNull: PartRange?
    get() = if (isValid) this else null

private val PartRange.cardinality: Long
    get() = values.productOf { it.size.toLong() }


private const val START_RULE = "in"
private const val MIN_VALUE = 1
private const val MAX_VALUE = 4000
private val WHOLE_RANGE = IntRange(MIN_VALUE, MAX_VALUE)

private fun solve1(input: List<String>): Long {
    val (rules, parts: List<Part>) = parse(input)

    tailrec fun applyRule(part: Part, rule: String): Boolean =
        when (rule) {
            "R" -> false
            "A" -> true
            else -> applyRule(part, rules[rule]!!.commands.first { it.test(part) }.nextRuleName)
        }

    return parts.filter { applyRule(it, START_RULE) }.sumOf { it.values.sum().toLong() }
}

private fun solve2(input: List<String>): Long {
    val (rules, _) = parse(input)

    val acceptedParts = mutableListOf<PartRange>()

    fun applyRule(part: PartRange, rule: String) {
        when (rule) {
            "R" -> {}
            "A" -> acceptedParts += part
            else -> {
                fun applyCmds(p: PartRange, cmds: List<Command>) {
                    val cmd = cmds.first()
                    val (pos, neg) = cmd.partition(p)
                    pos?.let { applyRule(it, cmd.nextRuleName) }
                    neg?.let { applyCmds(it, cmds.drop(1)) }
                }

                applyCmds(part, rules[rule]!!.commands)
            }
        }
    }

    val initialPart = "xmas".associate { it to WHOLE_RANGE }
    applyRule(initialPart, START_RULE)

    return acceptedParts.sumOf { it.cardinality }
}

private fun parse(input: List<String>): Pair<Map<String, Rule>, List<Part>> {
    val (ruleStrs, partStrs) = input.splitByEmptyLines().map { it.toList() }.toList()

    val ruleRegex = """(.+)\{(.+)}""".toRegex()
    val condCmdRegex = """(.)([<>])(\d+):(.+)""".toRegex()
    val rules = ruleStrs.associate { ruleStr ->
        val (name, cmdStrs) = ruleRegex.matchEntire(ruleStr)!!.destructured
        val cmds = cmdStrs.split(',').map { cmdStr ->
            when (val m = condCmdRegex.matchEntire(cmdStr)) {
                null -> Command.Unconditional(cmdStr)
                else -> {
                    val vs = m.groupValues
                    Command.Conditional(vs[1][0], vs[2][0], vs[3].toInt(), vs[4])
                }
            }
        }
        name to Rule(name, cmds)
    }

    val partRegex = """\{(.+)}""".toRegex()
    val partComponentRegex = """(.)=(\d+)""".toRegex()
    val parts: List<Part> = partStrs.map { partStr ->
        partRegex.matchEntire(partStr)!!.groupValues[1].split(",").associate { compStr ->
            val (name, value) = partComponentRegex.matchEntire(compStr)!!.destructured
            name[0] to value.toInt()
        }
    }
    return Pair(rules, parts)
}