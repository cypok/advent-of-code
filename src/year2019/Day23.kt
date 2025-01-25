package year2019

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import utils.runAoc
import utils.toIntExact

// Task description:
//   https://adventofcode.com/2019/day/23

fun main() = runAoc {
    measureRunTime()
    solution { runBlocking {
        fun debug(id: String, str: String) { if (false) println("$id: $str") }
        fun debug(addr: Int, str: String) { debug("#%02d".format(addr), str) }

        val count = 50

        val result = Channel<Long>(capacity = 1)

        val inputBuffers = List(count) { ArrayDeque<Pair<Long, Long>>() }

        val alives = (0 until count).toMutableSet()

        fun isAlive(addr: Int) =
            !isPart2 || addr in alives

        fun setAlive(addr: Int) {
            if (!isPart2) return

            alives.add(addr)
        }

        fun sendPacketNotNat(dst: Int, x: Long, y: Long) {
            setAlive(dst)
            val buf = inputBuffers[dst]
            buf.addLast(x to y)
        }

        val nat = object {
            private var packet: Pair<Long, Long>? = null
            private var lastY: Long? = null

            fun setThePacket(x: Long, y: Long) {
                if (isPart1) {
                    result.trySend(y)
                } else {
                    packet = x to y
                    debug("NAT", "input       $x, $y")
                }
            }

            fun sendThePacket() {
                val (x, y) = packet!!
                debug("NAT", "IDLE!  #00: $x, $y")
                if (y == lastY) {
                    result.trySend(y)
                } else {
                    sendPacketNotNat(0, x, y)
                    lastY = y
                }
            }
        }

        fun setStuck(addr: Int) {
            if (!isPart2) return

            alives.remove(addr).also { assert(it) }
            debug(addr, "stuck")

            if (alives.isEmpty()) {
                assert(inputBuffers.all { it.isEmpty() })

                nat.sendThePacket()
            }
        }

        fun sendPacket(dst: Int, x: Long, y: Long) {
            if (dst == 255) {
                nat.setThePacket(x, y)
            } else {
                sendPacketNotNat(dst, x, y)
            }
        }

        val theNetwork = launch {
            repeat(count) { addr ->
                launch {
                    val pc = intCodeComputer()

                    var ownAddressRead = false
                    val inputBuffer = inputBuffers[addr]
                    var inputFirstHalf: Long? = null

                    var failedInputs = 0

                    val outputBuffer = Array(3) { 0L }
                    var outputPos = 0

                    pc.run(
                        input = {
                            if (!ownAddressRead) {
                                assert(isAlive(addr))
                                ownAddressRead = true
                                addr.toLong()

                            } else if (inputBuffer.isNotEmpty()) {
                                setAlive(addr)
                                failedInputs = 0
                                if (inputFirstHalf == null) {
                                    val fst = inputBuffer.first().first
                                    inputFirstHalf = fst
                                    fst
                                } else {
                                    val fst = inputFirstHalf
                                    val snd = inputBuffer.removeFirst().second
                                    debug(addr, "input       $fst, $snd")
                                    inputFirstHalf = null
                                    snd
                                }

                            } else {
                                failedInputs++
                                if (failedInputs == 2) {
                                    setStuck(addr)
                                }
                                yield()
                                -1
                            }
                        },
                        output = { e ->
                            run {
                                // after some failed inputs we are not expecting any new output
                                assert(isAlive(addr))

                                outputBuffer[outputPos++] = e
                                if (outputPos == 3) {
                                    val (d, x, y) = outputBuffer
                                    val destId = if (d == 255L) "NAT" else "#%02d".format(d)
                                    debug(addr, "output $destId: $x, $y")
                                    sendPacket(d.toIntExact(), x, y)
                                    outputPos = 0
                                }
                            }
                        }
                    )
                }
            }
        }

        result.receive().also {
            theNetwork.cancel()
        }
    }}
}