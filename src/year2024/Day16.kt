package year2024

import utils.*
import java.util.PriorityQueue

// Task description:
//   https://adventofcode.com/2024/day/16

fun main() = runAoc {
    example {
        answer1(7036)
        answer2(45)
        """
            ###############
            #.......#....E#
            #.#.###.#.###.#
            #.....#.#...#.#
            #.###.#####.#.#
            #.#.#.......#.#
            #.#.#####.###.#
            #...........#.#
            ###.#.#####.#.#
            #...#.....#.#.#
            #.#.#.###.#.#.#
            #.....#...#.#.#
            #.###.#.#.#.#.#
            #S..#.....#...#
            ###############
        """
    }
    example {
        answer1(11048)
        answer2(64)
        """
            #################
            #...#...#...#..E#
            #.#.#.#.#.#.#.#.#
            #.#.#.#...#...#.#
            #.#.#.#.###.#.#.#
            #...#.#.#.....#.#
            #.#.#.#.#.#####.#
            #.#...#.#.#.....#
            #.#.#####.#.###.#
            #.#.#.......#...#
            #.#.###.#####.###
            #.#.#...#.....#.#
            #.#.#.#####.###.#
            #.#.#.........#.#
            #.#.#.#########.#
            #S#.............#
            #################
        """
    }

    solution {
        data class Loc(
            val pos: Point,
            val dir: Dir
        )
        infix fun Point.x(dir: Dir) = Loc(this, dir)

        data class State(
            val pos: Point,
            val dir: Dir,
            val score: Long,
            val prevs: MutableList<State> = mutableListOf(),
            var discarded: Boolean = false
        )

        val statesQueue = PriorityQueue<State>(Comparator.comparing { it.score })
        val statesMap = mutableMapOf<Loc, State>()

        fun findPath(): State {
            fun visit(pos: Point, dir: Dir, score: Long, currentState: State?) {
                if (map[pos] == '#') return

                val loc = pos x dir
                val existingState = statesMap[loc]
                val state =
                    if (existingState == null || existingState.score > score) {
                        existingState?.let { it.discarded = true }
                        val newState = State(pos, dir, score)
                        statesQueue.add(newState)
                        statesMap.put(loc, newState)
                        newState
                    } else if (existingState.score < score) {
                        return
                    } else {
                        assert(existingState.score == score)
                        existingState
                    }
                if (currentState != null) {
                    state.prevs += currentState
                }
            }

            visit(map.find('S'), Dir.RIGHT, 0, null)

            while (true) {
                val state = statesQueue.remove()
                if (state.discarded) {
                    continue
                }

                val [p, d, s] = state
                if (map[p] == 'E') {
                    return state
                }

                visit(p.moveInDir(d), d, s + 1, state)
                visit(p, d.left, s + 1000, state)
                visit(p, d.right, s + 1000, state)
            }
        }

        fun calcAnswer(state: State): Long =
            if (isPart1) {
                state.score
            } else {
                val paths = mutableSetOf<Loc>()
                tailrec fun goBack(prevState: State) {
                    if (!paths.add(prevState.pos x prevState.dir)) return

                    val prevs = prevState.prevs
                    if (prevs.isEmpty()) return

                    // Optimize long single tracks using tailrec
                    prevs.drop(1).forEach { @Suppress("NON_TAIL_RECURSIVE_CALL") goBack(it) }
                    goBack(prevs.first())
                }
                goBack(state)
                paths.map { it.pos }.toSet().size.toLong()
            }

        calcAnswer(findPath())
    }
}