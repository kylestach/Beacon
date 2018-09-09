package com.hackathon.fellas.homedepotapp

import android.util.Log
import android.widget.TextView
import java.lang.Math.exp
import java.lang.Math.sqrt
import java.util.*
import kotlin.collections.ArrayList

class NavigationMap(private val instructionsCallback: (String, Double?) -> Unit) {
    class Signal(var strength: Int, var timestamp: Long, var signalFilt: Double, var inRangeCount: Int = 0)

    val nodes = arrayListOf(
            Node(0.0, 0.0, arrayListOf(Connection(1, 0.0))),
            Node(0.0, 3.0, arrayListOf(Connection(0, 0.0), Connection(2, -90.0))),
            Node(3.0, 0.0, arrayListOf(Connection(1, 90.0)))
    )

    class Node(val x: Double, val y: Double, val connections: ArrayList<Connection>, var lastSignal: Signal? = null)
    class Connection(val to: Int, val angle: Double)
    class Path(var nodes: ArrayList<Int>)

    val queuedNavigationInstructions = LinkedList<String>()

    // The index of the next node in the path
    var pathIdx = 0

    var currentPath: Path? = Path(arrayListOf(0, 1, 2))

    fun calculatePath(start: Int, end: Int): Path {
        var prevTable = ArrayList<Int>(nodes.size)
        val queue = PriorityQueue<Pair<Double, Pair<Int, Int>>>()
        queue.add(Pair(0.0, Pair(start, 1)))
        var numNodesTotal = 0
        while (!queue.isEmpty()) {
            val next = queue.remove()
            val nextDist = next.first
            val nextNode = next.second.first
            val numNodes = next.second.second
            if (nextNode == end) {
                numNodesTotal = numNodes
                break
            }
            for (conn in nodes[nextNode].connections) {
                prevTable[conn.to] = nextNode
                queue.add(Pair(
                        nextDist + sqrt(
                                Math.pow(nodes[conn.to].x - nodes[nextNode].x, 2.0)
                                        + Math.pow(nodes[conn.to].y - nodes[nextNode].y, 2.0)
                        ), Pair(conn.to, numNodes + 1)))
            }
        }
        val result = Path(ArrayList(numNodesTotal))
        var curr = end
        for (i in 0 until numNodesTotal) {
            result.nodes[numNodesTotal - i - 1] = curr
            curr = prevTable[curr]
        }
        result.nodes[0] = curr
        return result
    }

    fun updateSignalReading(beaconId: Int, strength: Int) {
        val timeConstant = 1.5

        if (beaconId < 0 || beaconId > nodes.size) {
            throw IllegalArgumentException("Beacon $beaconId does not exist!")
        }

        if (nodes[beaconId].lastSignal != null) {
            val dt = (System.currentTimeMillis() - nodes[beaconId].lastSignal!!.timestamp) / 1000.0
            val k = exp(-dt / timeConstant)
            val signalFilt = k * nodes[beaconId].lastSignal!!.signalFilt + (1 - k) * strength
            nodes[beaconId].lastSignal = Signal(strength, System.currentTimeMillis(), signalFilt, nodes[beaconId].lastSignal!!.inRangeCount)
        } else {
            nodes[beaconId].lastSignal = Signal(strength, System.currentTimeMillis(), strength.toDouble(), 0)
        }

        if (currentPath != null && pathIdx != -1) {
            val pathNext = currentPath!!.nodes[pathIdx]
            if (pathNext == beaconId) {
                var isClosest = true
                for (node in nodes) {
                    if (node.lastSignal != null)
                    if (node != nodes[beaconId] && nodes[beaconId].lastSignal!!.signalFilt - 5 < node.lastSignal!!.signalFilt && node.lastSignal!!.timestamp + 1000 > System.currentTimeMillis()) {
                        isClosest = false
                    }
                }
                if (isClosest) {
                    nodes[beaconId].lastSignal!!.inRangeCount++
                } else {
                    nodes[beaconId].lastSignal!!.inRangeCount = 0
                }
                Log.v("BEACONPATH", "${nodes[beaconId].lastSignal!!.inRangeCount} counts for $beaconId")

                if (nodes[beaconId].lastSignal!!.inRangeCount > 10) {
                    nextNavigationInstruction()
                }
            }
        }
    }

    fun getNextConnection(): Connection? {
        if (pathIdx >= currentPath!!.nodes.size - 1) {
            return null
        }
        for (connection in nodes[currentPath!!.nodes[pathIdx]].connections) {
            if (connection.to == currentPath!!.nodes[pathIdx + 1]) {
                return connection
            }
        }
        return null
    }

    fun getPreviousConnection(): Connection? {
        if (pathIdx <= 0) {
            return null
        }
        for (connection in nodes[currentPath!!.nodes[pathIdx - 1]].connections) {
            if (connection.to == currentPath!!.nodes[pathIdx]) {
                return connection
            }
        }
        return null
    }

    fun startNavigation() {
        pathIdx = 0
        val distance = sqrt(Math.pow(nodes[pathIdx + 1].x - nodes[pathIdx].x, 2.0) + Math.pow(nodes[pathIdx + 1].y - nodes[pathIdx].y, 2.0))
        queuedNavigationInstructions.addFirst("Find the first waypoint")
        instructionsCallback("Find the first waypoint", null)
    }

    fun nextNavigationInstruction() {
        if (pathIdx < currentPath!!.nodes.size - 1) {
            val toNext = getNextConnection()
            val fromLast = getPreviousConnection()
            var angleDiff = (toNext?.angle ?: return) - (fromLast?.angle ?: 0.0)
            if (angleDiff > 180.0) {
                angleDiff -= 360.0
            } else if (angleDiff < -180.0) {
                angleDiff += 360.0
            }
            Log.v("ANGLEDIFF", angleDiff.toString())
            val distance = sqrt(Math.pow(nodes[pathIdx + 1].x - nodes[pathIdx].x, 2.0) + Math.pow(nodes[pathIdx + 1].y - nodes[pathIdx].y, 2.0))
            val directions = when {
                angleDiff > 15 -> "Turn left and continue for $distance meters"
                angleDiff < -15 -> "Turn right and continue for $distance meters"
                else -> "Continue straight for $distance meters"
            }
            queuedNavigationInstructions.addFirst(directions)
            instructionsCallback(directions, angleDiff)
            pathIdx++
        } else {
            pathIdx = 0
            queuedNavigationInstructions.addFirst("You have arrived at your destination.")
            instructionsCallback("You have arrived at your destination", null)
            return
        }
    }
}