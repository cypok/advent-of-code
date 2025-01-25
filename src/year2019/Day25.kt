package year2019

import utils.Dir
import utils.cartesianProduct
import utils.numbers
import utils.runAoc
import utils.shouldNotReachHere
import year2019.IntCodeComputer.AsciiApi

// Task description:
//   https://adventofcode.com/2019/day/25

fun main() = runAoc {
    @Suppress("LocalVariableName")
    solution1 {
        fun logging(str: String) = printExtra(str)

        fun <R> isolatedRun(action: AsciiApi.() -> R): R =
            runAsciiIntCode({/* too much, skip full log */}, action)

        val COMMAND_REQUEST = "Command?"
        val WEIGHING_SUCCESS = "You may proceed"
        val WEIGHING_FAILURE = "you are ejected back"

        class Room(val name: String, val neighbors: MutableMap<Dir, Room?>) {
            override fun toString() =
                name + neighbors.entries.joinToString(", ", " {", "}") { "${it.key.asDoor()[0]}:${it.value?.name ?: "???"}" }
        }

        val rooms = mutableMapOf<String, Room>()
        val itemLocations = mutableMapOf<Item, Room>()

        fun AsciiApi.scanCmdOutput(): String {
            expectLine("")
            return scanLine()
        }

        fun AsciiApi.expectCmdOutput(expected: String) {
            check(scanCmdOutput() == expected)
        }

        fun AsciiApi.tryEnterRoom(): Triple<Room, List<Item>, String> {
            expectCmdOutput("")
            expectLine("")
            val name = scanLine().substringAfter("== ").substringBefore(" ==")
            scanLine() // description
            expectLine("")
            expectLine("Doors here lead:")
            val doors = scanLinesWhile { it.isNotEmpty() }.map { it.substringAfter("- ").asDir() }

            val room = rooms.getOrPut(name) {
                val neighbors = doors.associateWith { null as Room? }.toMutableMap()
                Room(name, neighbors)
            }

            val (items, msg) = when (val r = scanLine()) {
                "Items here:" -> {
                    val items = scanLinesWhile { it.isNotEmpty() }
                        .map { it.substringAfter("- ") }
                    items to scanLine()
                }

                else -> emptyList<Item>() to r
            }

            return Triple(room, items, msg)
        }

        logging("")
        logging("Scanning the rooms and building the map.")
        isolatedRun {
            fun tryVisitRoomAndNeighbors(prev: Pair<Room, Dir>?): Boolean {
                while (true) {
                    val (room, items, msg) = tryEnterRoom()
                    if (prev != null) {
                        val (prevRoom, prevDir) = prev

                        fun <K, V> MutableMap<K, V>.putOrCheckTheTheSame(key: K, value: V) {
                            val old = this[key]
                            if (old == null) {
                                this[key] = value
                            } else {
                                assert(old == value)
                            }
                        }

                        prevRoom.neighbors.putOrCheckTheTheSame(prevDir, room)
                        room.neighbors.putOrCheckTheTheSame(prevDir.opposite, prevRoom)
                    }
                    items.forEach { itemLocations[it] = room }

                    when {
                        COMMAND_REQUEST == msg -> {
                            val nextDir = room.neighbors.entries.find { it.value == null }?.key ?: return true
                            printLine(nextDir.asDoor())
                            if (tryVisitRoomAndNeighbors(room to nextDir)) {
                                printLine(nextDir.opposite.asDoor())
                            }
                        }

                        WEIGHING_FAILURE in msg -> {
                            return false
                        }

                        else -> error(msg)
                    }
                }
                shouldNotReachHere()
            }

            tryVisitRoomAndNeighbors(null)
        }

        val routeToRoom = buildMap<Room, List<Dir>> {
            val queue = ArrayDeque<Pair<Room, List<Dir>>>()
            queue += rooms.values.first() to emptyList()

            while (queue.isNotEmpty()) {
                val (cur, dirs) = queue.removeFirst()
                if (cur !in this) {
                    this[cur] = dirs

                    cur.neighbors.forEach { (d, r) ->
                        queue += r!! to (dirs + d)
                    }
                } // else already visited
            }
        }

        fun expectNoError(msg: String) =
            check(msg == COMMAND_REQUEST)

        fun AsciiApi.expectRoom() =
            tryEnterRoom().also { expectNoError(it.third) }

        fun AsciiApi.goRoute(route: List<Dir>) {
            route.forEach { dir ->
                printLine(dir.asDoor())
                expectRoom()
            }
        }

        fun AsciiApi.tryTakeItem(item: Item): String {
            printLine("take $item")
            expectCmdOutput("You take the $item.")
            expectLine("")
            return scanLine()
        }

        fun AsciiApi.takeOrDropItem(item: Item, take: Boolean) {
            val cmd = if (take) "take" else "drop"
            printLine("$cmd $item")
            expectCmdOutput("You $cmd the $item.")
            expectLine("")
            expectNoError(scanLine())
        }

        val goodItemLocations = itemLocations.filter { (item, room) ->
            logging("")
            logging("Trying to take the $item at ${room.name}.")
            val failureMsg = isolatedRun {
                expectRoom()
                goRoute(routeToRoom[room]!!)
                val msg = tryTakeItem(item)
                if (msg != COMMAND_REQUEST) {
                    return@isolatedRun msg
                }

                // Check that we can leave the room with the item.
                val safeDir = routeToRoom[room]!!.lastOrNull()?.opposite ?: room.neighbors.keys.first()
                printLine(safeDir.asDoor())
                val moveOutput = scanCmdOutput()
                if (moveOutput.isNotEmpty()) {
                    return@isolatedRun moveOutput
                }

                null
            }
            logging(
                if (failureMsg != null) {
                    "The $item cannot be taken. $failureMsg"
                } else {
                    "The $item can be taken."
                }
            )
            failureMsg == null
        }

        logging("")
        logging("Taking all the items and passing the pressure-sensitive floor.")
        isolatedRun {
            expectRoom()

            goodItemLocations.forEach { (item, room) ->
                val there = routeToRoom[room]!!
                goRoute(there)

                tryTakeItem(item).also { expectNoError(it) }

                val back = there.map { it.opposite }.asReversed()
                goRoute(back)
            }
            val allItems = goodItemLocations.keys.toList()

            val checkpointRoom = rooms["Security Checkpoint"]!!
            val weighingRoom = rooms["Pressure-Sensitive Floor"]!!
            val doorToWeighingRoom = weighingRoom.neighbors.entries.single()
                .also { check(it.value == checkpointRoom) }
                .key.opposite.asDoor()

            goRoute(routeToRoom[checkpointRoom]!!)

            val inventory = allItems.map { true }.toMutableList()
            List(allItems.size) { listOf(true, false) }.cartesianProduct().forEach { itemIdsToTry ->
                itemIdsToTry.withIndex().forEach { (idx, take) ->
                    val item = allItems[idx]
                    if (take != inventory[idx]) {
                        takeOrDropItem(item, take)
                        inventory[idx] = take
                    }
                }

                if (false) {
                    printLine("inv")
                    val nonEmpty = inventory.any { it }
                    expectCmdOutput(
                        if (nonEmpty) "Items in your inventory:" else "You aren't carrying any items."
                    )
                    scanLinesWhile { it.isNotEmpty() }
                    expectNoError(scanLine())
                }

                printLine(doorToWeighingRoom)
                val msg = tryEnterRoom().third
                when {
                    WEIGHING_FAILURE in msg -> {
                        // Ok, we'll try another inventory.
                        expectRoom()
                    }

                    WEIGHING_SUCCESS in msg -> {
                        val passingItems = inventory.withIndex()
                            .mapNotNull { (i, take) -> allItems[i].takeIf { take } }
                            .joinToString(", ")
                        logging("We've passed the weighing check with $passingItems.")
                        return@isolatedRun scanLinesUntilEnd().last().numbers().single()
                    }

                    else -> error(msg)
                }
            }

            shouldNotReachHere()
        }
    }
}

typealias Item = String

private val doorByDir = mapOf(
    Dir.UP to "north",
    Dir.DOWN to "south",
    Dir.LEFT to "west",
    Dir.RIGHT to "east",
)
private val dirByDoor = doorByDir.entries
    .associate { (dir, door) -> door to dir }

fun Dir.asDoor() = doorByDir[this]!!
fun String.asDir() = dirByDoor[this]!!
