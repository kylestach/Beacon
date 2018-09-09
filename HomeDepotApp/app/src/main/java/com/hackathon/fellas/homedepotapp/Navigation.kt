package com.hackathon.fellas.homedepotapp

import android.util.Log
import java.lang.Math.exp
import java.lang.Math.sqrt
import java.util.*
import kotlin.collections.ArrayList

class NavigationMap(private val instructionsCallback: (String, Double?) -> Unit) {
    private val nodes = arrayListOf(
            Node(0.0, 0.0, 90.0, arrayListOf(Connection(1, 0.0)), "hammer"),
            Node(0.0, 3.0, 0.0, arrayListOf(Connection(0, -180.0), Connection(2, -90.0)), "screwdriver"),
            Node(3.0, 3.0, 0.0, arrayListOf(Connection(1, 90.0)), "nail")
    )

    class Signal(var timestamp: Long, var signalFilt: Double, var inRangeCount: Int = 0)
    class Node(val x: Double, val y: Double, val initAngle: Double, val connections: ArrayList<Connection>, val name: String, var lastSignal: Signal? = null)
    class Connection(val to: Int, val angle: Double)
    class Path(var nodes: ArrayList<Int>)

    private var goalBeacon: Int = 0
    private var currentBeacon: Int? = null

    private var isDone = false

    // The index of the next node in the path
    private var pathIdx = -1

    private var currentPath: Path? = null

    @Suppress("unused")
    fun calculatePath(start: Int, end: Int): Path {
        val prevTable = ArrayList<Int>(nodes.size)
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

    private fun goToBeacon(beaconId: Int) {
        currentPath = Path(arrayListOf())
        if (currentBeacon != null) {
            when {
                currentBeacon == beaconId -> {
                    instructionsCallback("You are already at the destination", null)
                    isDone = true
                    pathIdx = -1
                }
                beaconId > currentBeacon!! -> {
                    currentPath!!.nodes.addAll(currentBeacon!!..beaconId)
                    pathIdx = 0
                }
                else -> {
                    currentPath!!.nodes.addAll(currentBeacon!! downTo beaconId)
                    pathIdx = 0
                }
            }
            currentAngle = nodes[currentBeacon!!].initAngle
        }
    }

    fun setGoal(beaconId: Int) {
        goalBeacon = beaconId
    }

    fun updateSignalReading(beaconId: Int, uncalibratedStrength: Int) {
        if (isDone) {
            return
        }

        val strength = uncalibratedStrength + when (beaconId) {
            0 -> 48
            1 -> 35
            else -> 45
        }
        val timeConstant = 0.5

        if (beaconId < 0 || beaconId > nodes.size) {
            throw IllegalArgumentException("Beacon $beaconId does not exist!")
        }

        if (nodes[beaconId].lastSignal != null) {
            val dt = (System.currentTimeMillis() - nodes[beaconId].lastSignal!!.timestamp) / 1000.0
            val k = exp(-dt / timeConstant)
            val signalFilt = k * nodes[beaconId].lastSignal!!.signalFilt + (1 - k) * strength
            nodes[beaconId].lastSignal = Signal(System.currentTimeMillis(), signalFilt, nodes[beaconId].lastSignal!!.inRangeCount)
        } else {
            nodes[beaconId].lastSignal = Signal(System.currentTimeMillis(), strength.toDouble(), 0)
        }
        var isClosest = true

        for (node in nodes) {
            if (node.lastSignal != null)
                if (node != nodes[beaconId] &&
                        nodes[beaconId].lastSignal!!.signalFilt - 2 < node.lastSignal!!.signalFilt
                        && node.lastSignal!!.timestamp + 1000 > System.currentTimeMillis()) {
                    isClosest = false
                }
        }
        if (isClosest) {
            nodes[beaconId].lastSignal!!.inRangeCount++
        } else {
            nodes[beaconId].lastSignal!!.inRangeCount = 0
        }
        if (currentBeacon == null && nodes[beaconId].lastSignal!!.inRangeCount > 5) {
            currentBeacon = beaconId
            goToBeacon(goalBeacon)
            Log.v("DARN", "Beacon $currentBeacon -> $goalBeacon")
        }
        if (currentPath != null && pathIdx != -1) {
            val nodeNext = currentPath!!.nodes[pathIdx]
            if (nodeNext == beaconId) {
                Log.v("BEACONPATH", "${nodes[beaconId].lastSignal!!.inRangeCount} counts for $beaconId")

                if (nodes[beaconId].lastSignal!!.inRangeCount == 10) {
                    nextNavigationInstruction()
                }
            }
        }
    }

    private fun getNextConnection(): Connection? {
        if (pathIdx >= currentPath!!.nodes.size - 1 || currentPath == null || pathIdx < 0) {
            return null
        }
        for (connection in nodes[currentPath!!.nodes[pathIdx]].connections) {
            if (connection.to == currentPath!!.nodes[pathIdx + 1]) {
                return connection
            }
        }
        return null
    }

    fun startNavigation() {
        pathIdx = 0
        instructionsCallback("Find the first waypoint", null)
    }

    private var currentAngle: Double = 0.0

    private fun nextNavigationInstruction() {
        if (pathIdx < currentPath!!.nodes.size - 1) {
            val toNext = getNextConnection()
            var angleDiff = (toNext?.angle ?: return) - currentAngle
            if (angleDiff > 180.0) {
                angleDiff -= 360.0
            } else if (angleDiff < -180.0) {
                angleDiff += 360.0
            }
            currentAngle = toNext.angle
            val distance = sqrt(Math.pow(nodes[pathIdx + 1].x - nodes[pathIdx].x, 2.0) + Math.pow(nodes[pathIdx + 1].y - nodes[pathIdx].y, 2.0))

            if (pathIdx == 0) {
                val directions = "Face the  ${nodes[currentPath!!.nodes[pathIdx]].name}s, " + when {
                    angleDiff > 165 || angleDiff < -165 -> "turn around and "
                    angleDiff > 15 -> "turn left and "
                    angleDiff < -15 -> "turn right and "
                    else -> "and "
                } + "continue straight for ${Math.round(distance)} meters"
                instructionsCallback(directions, angleDiff)
            } else {
                Log.v("ANGLEDIFF", angleDiff.toString())
                val directions = when {
                    angleDiff > 15 -> "Turn left and continue for ${Math.round(distance)} meters"
                    angleDiff < -15 -> "Turn right and continue for ${Math.round(distance)} meters"
                    else -> "Continue straight for ${Math.round(distance)} meters"
                }
                instructionsCallback(directions, angleDiff)
            }
            pathIdx++
        } else {
            var angleDiff = nodes[currentPath!!.nodes[pathIdx]].initAngle - currentAngle
            if (angleDiff > 180.0) {
                angleDiff -= 360.0
            } else if (angleDiff < -180.0) {
                angleDiff += 360.0
            }
            val instructions = "You have arrived at your destination. The ${nodes[currentPath!!.nodes[pathIdx]].name}s are " + when {
                angleDiff > 15.0 -> "on your left."
                angleDiff < -15.0 -> "on your right."
                else -> "in front of you."
            }
            instructionsCallback(instructions, angleDiff)
            isDone = true
            return
        }
    }
}
