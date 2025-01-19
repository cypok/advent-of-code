@file:Suppress("unused")

package utils

import kotlin.math.max
import kotlin.math.min
import kotlin.sequences.toList

data class Point(val row: Int, val col: Int) {
    val i get() = row
    val j get() = col

    override fun toString(): String = "${row}x${col}"

    fun moveInDir(dir: Dir, steps: Int = 1): Point =
        when (dir) {
            Dir.UP -> (row - steps) x col
            Dir.DOWN -> (row + steps) x col
            Dir.LEFT -> row x (col - steps)
            Dir.RIGHT -> row x (col + steps)
        }
}

infix fun Int.x(that: Int) = Point(this, that)

enum class Dir {
    UP, DOWN, LEFT, RIGHT;

    val opposite
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }

    val left
        get() = when (this) {
            UP -> LEFT
            DOWN -> RIGHT
            LEFT -> DOWN
            RIGHT -> UP
        }

    val right
        get() = left.opposite

    companion object {
        fun fromChar(ch: Char) = when (ch) {
            'U', '^' -> UP
            'D', 'v' -> DOWN
            'L', '<' -> LEFT
            'R', '>' -> RIGHT
            else -> error(ch)
        }
    }
}

class Array2D<T>(private val data: Array<Array<T>>) {

    val height: Int = data.size
    val width: Int = data[0].size

    init {
        check(data.all { it.size == width })
    }

    operator fun contains(pos: Point): Boolean =
        (pos.row in 0 ..< height) && (pos.col in 0 ..< width)

    operator fun get(row: Int, col: Int): T =
        data[row][col]

    operator fun get(pos: Point): T =
        get(pos.row, pos.col)

    operator fun get(row: Int): MutableList<T> =
        row(row)

    fun getOrNull(row: Int, col: Int): T? =
        data.getOrNull(row)?.getOrNull(col)

    fun getOrNull(pos: Point): T? =
        getOrNull(pos.row, pos.col)

    operator fun set(row: Int, col: Int, value: T): T {
        val oldValue = data[row][col]
        data[row][col] = value
        return oldValue
    }

    operator fun set(pos: Point, value: T): T =
        set(pos.row, pos.col, value)

    val rows: List<MutableList<T>> =
        object : AbstractList<MutableList<T>>() {
            override val size: Int get() = height
            override fun get(index: Int): MutableList<T> = row(index)
        }

    val cols: List<MutableList<T>> =
        object : AbstractList<MutableList<T>>() {
            override val size: Int get() = width
            override fun get(index: Int): MutableList<T> = col(index)
        }

    private abstract class RowOrCol<T> : AbstractMutableList<T>() {
        override fun add(index: Int, element: T) = throw UnsupportedOperationException()
        override fun removeAt(index: Int): T = throw UnsupportedOperationException()
    }

    fun row(row: Int): MutableList<T> =
        object : RowOrCol<T>() {
            override val size: Int get() = width
            override fun get(index: Int): T = get(row, index)
            override fun set(index: Int, element: T): T = set(row, index, element)
        }

    fun col(col: Int): MutableList<T> =
        object : RowOrCol<T>() {
            override val size: Int get() = height
            override fun get(index: Int): T = get(index, col)
            override fun set(index: Int, element: T): T = set(index, col, element)
        }

    val diagonalsRight: List<List<T>>
        get() = buildList {
            // It could be done easier by just checking out of bounds access,
            // but I wanted to practice some math.
            // And it also could be done without any array allocations like rows above,
            // but it requires even more math.
            for (j in -height+1 ..< width) {
                val iMin = max(0, -j)
                val iMax = min(height-1, -j+width-1)
                add((iMin..iMax).map { i -> get(i, j + i) })
            }
        }

    val diagonalsLeft: List<List<T>>
        get() = buildList {
            for (j in 0 ..< width+height-1) {
                val iMin = max(0, j-width+1)
                val iMax = min(height-1, j)
                add((iMin..iMax).map { i -> get(i, j - i) })
            }
        }

    val indices: Sequence<Point> = sequence {
        for (i in 0 until height) {
            for (j in 0 until width) {
                yield(i x j)
            }
        }
    }

    val valuesIndexed: Sequence<Pair<T, Point>> =
        indices.map { get(it) to it }

    fun find(ch: T): Point = find { it == ch }

    fun find(predicate: (T) -> Boolean): Point =
        valuesIndexed
            .mapNotNull { (ch, pos) -> pos.takeIf { predicate(ch) } }
            .toList()
            .also { check(it.size == 1) { it } }
            .first()

    fun sumOf(selector: (T) -> Long): Long =
        data.sumOf { it.sumOf(selector) }

    companion object {
        fun fromLines(lines: List<String>): Array2D<Char> =
            Array2D(lines.map { it.toCharArray().toTypedArray() }.toTypedArray())

        fun ofChars(height: Int, width: Int, init: Char): Array2D<Char> =
            ofChars(height, width) { _, _ -> init }

        fun ofChars(height: Int, width: Int, init: (Int, Int) -> Char): Array2D<Char> =
            Array2D(Array(height) { i -> Array(width) { j -> init(i, j) } })

        fun ofInts(height: Int, width: Int, init: Int): Array2D<Int> =
            Array2D(Array(height) { Array(width) { init } })

        fun ofBooleans(height: Int, width: Int, init: Boolean): Array2D<Boolean> =
            Array2D(Array(height) { Array(width) { init } })

        inline fun <reified T> of(height: Int, width: Int, init: () -> T): Array2D<T> =
            Array2D(Array(height) { Array(width) { init() } })
    }
}

@Deprecated("use Array2D.fromLines()")
@Suppress("FunctionName")
fun StringArray2D(lines: List<String>) = Array2D.fromLines(lines)
typealias StringArray2D = Array2D<Char>

fun Array2D<Char>.toAsciiArt(backgroundChar: Char): String {
    val linesWithSpaces = this
        .rows
        .map {
            check(backgroundChar == ' ' || ' ' !in it)
            it.joinToString("").replace(backgroundChar, ' ').trimEnd()
        }
        .dropWhile { it.isEmpty() }
        .dropLastWhile { it.isEmpty() }
        .joinToString("\n")
        .trimIndent()
        .lines()

    val width = linesWithSpaces.maxOf { it.length }
    val rectangularLines = linesWithSpaces.map { it.padEnd(width) }

    return rectangularLines.joinToString("\n") {
        assert(backgroundChar == ' ' || backgroundChar !in it)
        it.replace(' ', backgroundChar)
    }
}

fun Array2D<Char>.toAsciiArt(): String =
    // Some heuristic.
    toAsciiArt(this.row(0).groupingBy { it }.eachCount().maxBy { it.value }.key)

fun List<Char>.asString(): String =
    String(toCharArray())
