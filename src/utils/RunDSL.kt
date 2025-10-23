@file:Suppress("unused")

package utils

import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.streams.asSequence
import kotlin.time.measureTimedValue

interface AocContext {
    fun ignoreRealInput()

    fun measureRunTime()

    fun example(description: String? = null, content: ExampleContext.() -> String)

    fun solution(code: Solution)
    fun solution1(code: Solution)
    fun solution2(code: Solution)

    fun test(code: Test)
}

interface ExampleContext {
    fun answer1(value: Any, param: Any? = null)
    fun answer2(value: Any, param: Any? = null)
}

interface SolutionContext {
    val lines: List<String>
    val map: Array2D<Char>

    val isPart1: Boolean
    val isPart2: Boolean

    val exampleParam: Any?

    val wrongAnswer: Any

    fun visualAnswer(answer: String): Any

    fun printExtra(arg: Any)
}

private typealias Solution = SolutionContext.() -> Any
private typealias Test = () -> Unit

private data class Example(val description: String?,
                           val codeLocation: String,
                           val input: String,
                           val answers: Map<Int, Pair<String, Any?>>)

private data class VisualAnswerWrapper(val value: String)

private val WrongAnswer = object {
    override fun toString() = "(wrong answer)"
}

// TODO: somehow rework these dirty hacks for running all days
var IS_BATCH_RUN = false
var TOTAL_FAILS = 0

fun runAoc(content: AocContext.() -> Unit) {
    val ctx = object : AocContext {
        var ignoreRealInput = false
        var measureRunTime = false
        val examples = mutableListOf<Example>()
        val solutions = mutableMapOf<Int, Solution>()
        val tests = mutableListOf<Test>()

        override fun ignoreRealInput() { ignoreRealInput = true }

        override fun measureRunTime() { if (!IS_BATCH_RUN) measureRunTime = true }

        override fun test(code: Test) { if (!IS_BATCH_RUN) tests += code }

        override fun example(description: String?, content: ExampleContext.() -> String) {
            val codeLocation = findCallerFromMainFrame().let { "line ${it.lineNumber}" }
            val answers = mutableMapOf<Int, Pair<String, Any?>>()
            val ctx = object : ExampleContext {
                override fun answer1(value: Any, param: Any?) = answers.putEnsuringNew(1, value.toString() to param)
                override fun answer2(value: Any, param: Any?) = answers.putEnsuringNew(2, value.toString() to param)
            }
            val input = ctx.content().trimIndent()
            require(answers.isNotEmpty()) { "at least one answer for any part" }
            if (!IS_BATCH_RUN) {
                examples += Example(description, codeLocation, input, answers)
            }
        }

        override fun solution1(code: SolutionContext.() -> Any) = solutions.putEnsuringNew(1, code)
        override fun solution2(code: SolutionContext.() -> Any) = solutions.putEnsuringNew(2, code)

        override fun solution(code: SolutionContext.() -> Any) {
            solution1(code)
            solution2(code)
        }
    }

    ctx.content()

    val (year, day) = guessYearAndDay()
    val (realInput, realAnswers) = prepareRealInputAndAnswers(year, day)

    val testResults = ctx.tests.map { runCatching { it() }}
    if (testResults.isNotEmpty()) {
        if (testResults.all { it.isSuccess }) {
            println("Tests passed 🟢")
        } else {
            testResults.mapNotNull { it.exceptionOrNull() }.forEach {
                println("Test failed with 🔴 EXCEPTION")
                it.printStackTrace(System.out)
            }
        }
    }

    for ((partNum, solution) in ctx.solutions.entries.sortedBy { it.key }) {
        fun runOne(
            runDesc: String,
            input: String,
            answerProvider: () -> String?,
            isExample: Boolean,
            exampleParam: Any? = null,
        ) {
            open class SilentCtx : SolutionContext {
                override val lines = input.trimEnd('\n').lines()
                override val map by lazy { Array2D.fromLines(lines) }

                override val isPart1 get() = partNum == 1
                override val isPart2 get() = partNum == 2

                override val exampleParam = exampleParam

                override val wrongAnswer = WrongAnswer

                override fun visualAnswer(answer: String) = VisualAnswerWrapper(answer)

                override fun printExtra(arg: Any) { /* nop */ }
            }
            class VerboseCtx : SilentCtx() {
                val extraPrints = mutableListOf<Any>()
                override fun printExtra(arg: Any) { if (!IS_BATCH_RUN) extraPrints += arg }
            }
            val solutionCtx = VerboseCtx()
            print("part$partNum, $runDesc: ")
            val (result, time) = measureTimedValue { runCatching { solutionCtx.solution() } }
            if (result.isSuccess) {
                // Heuristics around 100% or potentially wrong answer.
                val trivialAnswers = setOf<String>("0", "-1", "1", "")
                val wrong: Boolean

                val expected = answerProvider()
                val actualRaw = result.getOrNull()!!
                if (actualRaw is VisualAnswerWrapper) {
                    println()
                    val answer = actualRaw.value
                    println(answer)
                    wrong = answer.isBlank()
                    print("⭕ (")
                    when (expected) {
                        null -> print("unchecked")
                        else -> print("expected $expected")
                    }
                    print(")")
                } else {
                    val actual = actualRaw.toString()
                    val looksWrong = actual in trivialAnswers || actualRaw === WrongAnswer
                    print("$actual ")
                    print(when (expected) {
                        null -> {
                            wrong = looksWrong
                            "⭕ (unchecked)"
                        }
                        actual -> {
                            // Example could have a trivial answer.
                            check(isExample || !looksWrong)
                            wrong = false
                            "🟢"
                        }
                        else -> {
                            TOTAL_FAILS++
                            wrong = true
                            "🔴 (expected $expected)"
                        }
                    })
                }
                if (expected == null && !wrong) {
                    // Try to submit the answer for the real input.
                    // Note that an example always has a non-null expected answer.
                    println()
                    println("Submit? [yes/no]")
                    if (readln() == "yes") {
                        val actual =
                            if (actualRaw is VisualAnswerWrapper) {
                                println("What is the answer in the picture above?")
                                readln()
                            } else {
                                actualRaw.toString()
                            }
                        submitRealAnswer(year, day, partNum, actual)
                    }
                }
                if (!isExample) {
                    val maxTimeSec = 10
                    val maxExtraMeasurements = 30
                    var totalTime = time
                    print(" (took ${time.inWholeMilliseconds}")
                    if (ctx.measureRunTime && time.inWholeSeconds <= maxTimeSec/2 && !wrong) {
                        run measurements@{
                            val times = mutableListOf<Long>()
                            repeat(maxExtraMeasurements) {
                                val stabilized = times.takeLast(5).let { it.size >= 5 && 1.0 * it.max() / it.min() < 1.05 }
                                if (totalTime.inWholeSeconds > maxTimeSec || stabilized) {
                                    return@measurements
                                }
                                val (newResult, newTime) = measureTimedValue { runCatching { SilentCtx().solution() } }
                                check(newResult == result)
                                print(", ${newTime.inWholeMilliseconds}")
                                System.out.flush()
                                times += newTime.inWholeNanoseconds
                                totalTime += newTime
                            }
                        }
                    }
                    print(" ms)")
                }
                println()

            } else { // isFailure
                println("🔴 EXCEPTION")
                result.exceptionOrNull()!!.printStackTrace(System.out)
            }
            solutionCtx.extraPrints.forEach { println(it) }
        }

        for (example in ctx.examples) {
            val desc = example.description ?: "at ${example.codeLocation}"
            val input = example.input
            val (answer, param) = example.answers[partNum] ?: continue
            runOne(
                "example $desc",
                input,
                { answer },
                isExample = true,
                exampleParam = param,
            )
        }

        if (!ctx.ignoreRealInput) {
            val input = realInput.readText()
            val answer = { realAnswers(partNum) }
            runOne(
                "real",
                input,
                answer,
                isExample = false,
            )
        }
    }
}

private fun <K, V> MutableMap<K, V>.putEnsuringNew(key: K, value: V) {
    val oldValue = put(key, value)
    check(oldValue == null)
}

private fun prepareRealInputAndAnswers(year: Int, day: Int): Pair<Path, (Int) -> String?> {
    val realInput = getRealInputPath(year, day)
        .also { path ->
            if (path.notExists()) {
                val content = downloadRealInput(year, day)
                path.createParentDirectories()
                path.writeText(content)
                if (false) { // Doesn't seem so useful.
                    previewRealInput(content)
                }
            }
        }

    // Download them only when it's necessary.
    val availableWebAnswers = lazy { downloadAnswers(year, day) }
    fun answerProvider(partNum: Int): String? {
        val path = getAnswerPath(year, day, partNum)
        if (path.exists()) {
            return path.readText()
        }

        return availableWebAnswers.value.getOrNull(partNum - 1)
            ?.also { answer ->
                path.createParentDirectories()
                path.writeText(answer)
            }
    }

    return realInput to ::answerProvider
}

private fun getRealInputPath(year: Int, day: Int) =
    getCachedFilePath(year, day, "input")

private fun getAnswerPath(year: Int, day: Int, partNum: Int) =
    getCachedFilePath(year, day, "answer$partNum")

private fun getCachedFilePath(year: Int, day: Int, suffix: String): Path {
    val day2Digits = "%02d".format(day)
    val baseName = "inputs/year$year/Day$day2Digits"
    return Path("$baseName-$suffix.txt")
}

private fun downloadRealInput(year: Int, day: Int): String =
    webGet(year, day, "/input")

private fun downloadAnswers(year: Int, day: Int): List<String> =
    webGet(year, day, "").let { content ->
        """Your puzzle answer was <code>([^<]+)</code>""".toRegex()
            .findAll(content)
            .map { it.groupValues[1] }
            .toList()
    }

private fun webGet(year: Int, day: Int, subUrl: String): String =
    webAccess(year, day, subUrl, "GET")

private fun submitRealAnswer(year: Int, day: Int, partNum: Int, answer: String) {
    val encodedAnswer = URLEncoder.encode(answer, Charsets.UTF_8)
    val data = "level=$partNum&answer=$encodedAnswer"
    val output = run {
        val response = webAccess(year, day, "/answer", "POST", data)
        try {
            // Extract the first paragraph of <article> and remove all HTML tags.
            val articleRegex = """<article><p>(.*?)</p>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val article = articleRegex.findAll(response).single().groupValues[1]
            val tagRegex = """<[^>]+>""".toRegex()
            tagRegex.replace(article, "")
        } catch (e: Exception) {
            throw RuntimeException("Cannot parse response:\n$response", e)
        }
    }
    output.split("""((?<=[.?!])\s+|\n)""".toRegex())
        .filterNot { it.startsWith("If you're stuck") }
        .filterNot { it.startsWith("[Return to ") }
        .filterNot { it.startsWith("[Continue to ") }
        .map { "> $it"}
        .forEach { println(it) }
    if (output.startsWith("That's the right answer!")) {
        val path = getAnswerPath(year, day, partNum)
        check(path.notExists())
        path.createParentDirectories()
        path.writeText(answer)
    }
}

private fun webAccess(year: Int, day: Int, subUrl: String, method: String, postData: String? = null): String {
    val url = URI("https://adventofcode.com/$year/day/$day$subUrl").toURL()
    try {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        val sessionCookie = Path(".session-cookie").readText().trim()
        connection.setRequestProperty("Cookie", "session=$sessionCookie")
        connection.setRequestProperty("User-Agent", "github.com/cypok/advent-of-code by @cypok")
        postData?.let { data ->
            connection.doOutput = true
            connection.outputStream.use { it.write(data.encodeToByteArray()) }
        }

        when (connection.responseCode) {
            HttpURLConnection.HTTP_OK ->
                connection.inputStream.use { input ->
                    return String(input.readAllBytes())
                }
            else -> throw RuntimeException("Bad response from $url: ${connection.responseCode}, ${connection.responseMessage}")
        }
    } catch (e: Exception) {
        throw RuntimeException("Cannot access $url", e)
    }
}

private fun previewRealInput(input: String) {
    val height = 5
    val width = 40
    val lines = input.lines()
    println("========= REAL INPUT PREVIEW ==============")
    lines.take(height).forEach {
        println(it.take(width) + (if (it.length > width) "..." else ""))
    }
    if (lines.size > height) {
        println("...")
    }
    println("===========================================")
}

private fun guessYearAndDay(): Pair<Int, Int> {
    val className = findCallerFromMainFrame().className.substringBeforeLast("Kt")
    val classNameRegex = """year(\d+).Day(\d+)""".toRegex()
    val (year, day) = classNameRegex.matchEntire(className)!!
        .groupValues.drop(1).map { it.toInt() }
    return year to day
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
