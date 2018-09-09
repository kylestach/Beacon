package com.hackathon.fellas.homedepotapp

class Node(val x: Double, val y: Double, val connections: ArrayList<Connection>, var lastSignal: NavigationMap.Signal? = null)
class Connection(val to: Int, val angle: Double)
class Path(var nodes: ArrayList<Int>)

enum class BluetoothNode(val item: String) {
    NODE1("hammer"),
    NODE2("screwdriver"),
    NODE3("nails"),
    NODE4("screws")
}