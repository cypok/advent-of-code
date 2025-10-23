package year2023

import utils.*

@Suppress("DEPRECATION")
fun main() = test(
    ::part1,
    ::part2,
)

private val NODE_REGEX = Regex("""(.*) = \((.*), (.*)\)""")

private fun parse(input: List<String>): Pair<String, Map<String, Pair<String, String>>> {
    return Pair(
        input[0],
        input.asSequence().drop(2).map { line ->
            val (src, left, right) = NODE_REGEX.matchEntire(line)!!.destructured
            src to Pair(left, right)
        }.toMap()
    )
}

private fun applyInstructions(
    instructions: String,
    network: Map<String, Pair<String, String>>,
    start: String
): String {
    return instructions.fold(start) { cur, dir ->
        val node = network[cur]!!
        when (dir) {
            'L' -> node.first
            'R' -> node.second
            else -> error(dir)
        }
    }
}

private fun part1(input: List<String>): Long {
    val (instructions, network) = parse(input)

    var current = "AAA"
    var steps = 0L
    do {
        current = applyInstructions(instructions, network, current)
        steps += instructions.length
    } while (current != "ZZZ")

    return steps
}

private fun part2(input: List<String>): Long {
    val (instructions, network) = parse(input)

    // I noticed that inputs are small and paths are really long.
    // So there are cycles.
    // Try to calculate their lengths and start offsets.
    return network.keys.filter { it.endsWith('A') }
        .map { start ->
            var current = start
            var steps = 0L
            var reachedTargetWithSteps: Pair<String, Long>? = null
            while (true) {
                current = applyInstructions(instructions, network, current)
                steps += instructions.length

                if (current.endsWith('Z')) {
                    if (reachedTargetWithSteps == null) {
                        reachedTargetWithSteps = Pair(current, steps)
                    } else {
                        // Oh, we are lucky: because there is a single **Z in a cycle.
                        check(reachedTargetWithSteps.first == current) { "otherwise task becomes harder" }
                        // Oh, we are lucky second time: cycles have no shift and "start" at zero.
                        // So we can just use LCM to find a common cycle length.
                        val cycleLength = reachedTargetWithSteps.second
                        check(cycleLength * 2 == steps) { "otherwise task becomes harder" }
                        return@map cycleLength
                    }
                }
            }
            @Suppress("KotlinUnreachableCode") shouldNotReachHere() // Without it, the type checker fails.
        }
        .reduce(::lcm)
}
