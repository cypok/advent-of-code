package utils

import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.streams.asSequence
import kotlin.time.measureTimedValue

interface AocContext {
    fun ignoreRealInput()

    fun example(description: String? = null, content: ExampleContext.() -> String)

    fun solution(code: Solution)
    fun solution1(code: Solution)
    fun solution2(code: Solution)
}

interface ExampleContext {
    fun answer1(value: Any)
    fun answer2(value: Any)
}

interface SolutionContext {
    val lines: List<String>
    val map: StringArray2D

    val isPart1: Boolean
    val isPart2: Boolean
}

typealias Solution = SolutionContext.() -> Any

private data class Example(val description: String?,
                           val codeLocation: String,
                           val input: String,
                           val answers: Map<Int, Any>)

fun runAoc(content: AocContext.() -> Unit) {
    val ctx = object : AocContext {
        var ignoreRealInput = false
        val examples = mutableListOf<Example>()
        val solutions = mutableMapOf<Int, Solution>()

        override fun ignoreRealInput() { ignoreRealInput = true }

        override fun example(description: String?, content: ExampleContext.() -> String) {
            val codeLocation = findCallerFromMainFrame().let { "line ${it.lineNumber}" }
            val answers = mutableMapOf<Int, Any>()
            val ctx = object : ExampleContext {
                override fun answer1(value: Any) = answers.putEnsuringNew(1, value)
                override fun answer2(value: Any) = answers.putEnsuringNew(2, value)
            }
            val input = ctx.content().trimIndent()
            require(answers.isNotEmpty()) { "at least one answer for any part" }
            examples += Example(description, codeLocation, input, answers)
        }

        override fun solution1(code: SolutionContext.() -> Any) = solutions.putEnsuringNew(1, code)
        override fun solution2(code: SolutionContext.() -> Any) = solutions.putEnsuringNew(2, code)

        override fun solution(code: SolutionContext.() -> Any) {
            solution1(code)
            solution2(code)
        }
    }

    ctx.content()

    val realInput = run {
        // className = year2023.Day10
        val className = findCallerFromMainFrame().className.substringBeforeLast("Kt")
        // year = year2023
        val year = className.substringBeforeLast(".Day")
        // day = Day10
        val day = "Day" + className.substringAfterLast("Day")
        val inputsDir = "src/${year.replace('.', '/')}"
        Path("$inputsDir/$day.txt")
            .also { path ->
                if (path.notExists()) {
                    downloadRealInput(
                        year.substringAfter("year").toInt(),
                        day.substringAfter("Day").toInt(),
                        path
                    )
                        .also { previewRealInput(it) }
                }
            }
    }

    for ((partNum, solution) in ctx.solutions.entries.sortedBy { it.key }) {
        fun runOne(runDesc: String, input: String, answer: Any? = null, timed: Boolean = false) {
            val solutionCtx = object : SolutionContext {
                override val lines = input.trimEnd('\n').lines()
                override val map by lazy { StringArray2D(lines) }

                override val isPart1 get() = partNum == 1
                override val isPart2 get() = partNum == 2
            }
            print("part$partNum, $runDesc: ")
            val (result, time) = measureTimedValue { runCatching { solutionCtx.solution() } }
            print(result.getOrNull()?.let {
                if (answer == null || answer.toString() == it.toString()) {
                    "$it"
                } else {
                    "$it (WRONG ANSWER, expected $answer)"
                }
            } ?: "EXCEPTION")
            if (timed && result.isSuccess) {
                var totalTime = time
                print(" (took ${time.inWholeMilliseconds}")
                if (time.inWholeSeconds <= 5) {
                    for (i in 0 until 10) {
                        if (totalTime.inWholeSeconds > 10) {
                            break
                        }
                        val (newResult, newTime) = measureTimedValue { runCatching { solutionCtx.solution() } }
                        assert(newResult == result)
                        print(", ${newTime.inWholeMilliseconds}")
                        System.out.flush()
                        totalTime += newTime
                    }
                }
                print(" ms)")
            }
            println()
            result.onFailure { it.printStackTrace(System.out) }
        }

        for (example in ctx.examples) {
            val answer = example.answers[partNum] ?: continue
            val desc = example.description ?: "at ${example.codeLocation}"
            runOne("example $desc", example.input, answer)
        }

        if (!ctx.ignoreRealInput) {
            runOne("real", realInput.readText(), timed = true)
        }
    }
}

private fun <K, V> MutableMap<K, V>.putEnsuringNew(key: K, value: V) {
    val oldValue = put(key, value)
    check(oldValue == null)
}

private fun downloadRealInput(year: Int, day: Int, outputPath: Path): String {
    val url = URI("https://adventofcode.com/$year/day/$day/input").toURL()
    try {
        val connection = url.openConnection() as HttpURLConnection
        val sessionCookie = Path(".session-cookie").readText().trim()
        connection.setRequestProperty("Cookie", "session=$sessionCookie")
        connection.setRequestProperty("User-Agent", "github.com/cypok/advent-of-code by @cypok")

        when (connection.responseCode) {
            HttpURLConnection.HTTP_OK ->
                connection.inputStream.use { input ->
                    val bytes = input.readAllBytes()
                    outputPath.writeBytes(bytes)
                    return String(bytes)
                }
            else -> throw RuntimeException("Bad response: ${connection.responseCode}, ${connection.responseMessage}")
        }
    } catch (e: Exception) {
        throw RuntimeException("Cannot download $url", e)
    }
}

private fun previewRealInput(input: String) {
    val height = 5
    val width = 40
    val lines = input.lines()
    println("========= REAL INPUT PREVIEW ==============")
    lines.take(height).map {
        println(it.take(width) + (if (it.length > width) "..." else ""))
    }
    if (lines.size > height) {
        println("...")
    }
    println("===========================================")
}

private val MAIN_CLASS_PATTERN = Regex(""".*Day\d+(?:Kt)?""")
private fun findCallerFromMainFrame(): StackWalker.StackFrame =
    StackWalker.getInstance().walk { frames ->
        frames.asSequence()
            .firstOrNull { MAIN_CLASS_PATTERN.matches(it.className) }
            ?: error("this function should be called from DayNN class")
    }

@Deprecated("use runAoc() DSL")
fun test(part1: (List<String>) -> Any) =
    runAoc {
        solution1 { part1(lines) }
    }

@Deprecated("use runAoc() DSL")
fun test(part1: (List<String>) -> Any,
         part2: ((List<String>) -> Any)) =
    runAoc {
        solution1 { part1(lines) }
        solution2 { part2(lines) }
    }
