package com.hackathon.fellas.homedepotapp

class SharedData {
    companion object {
        val tools: Map<String, BluetoothNode> = mapOf(
                Pair("Fiberglass Claw Hammer", BluetoothNode.NODE1),
                Pair("Steel Drilling Hammer", BluetoothNode.NODE1),
                Pair("Mallet Hammer", BluetoothNode.NODE1),
                Pair("Diamond Tip Magnet Screwdriver", BluetoothNode.NODE2),
                Pair("Multi-bit Screwdriver", BluetoothNode.NODE2),
                Pair("Phillips Head Screwdriver", BluetoothNode.NODE2),
                Pair("Panel Board Nails", BluetoothNode.NODE3),
                Pair("Sinker Nails", BluetoothNode.NODE3),
                Pair("Smooth Shank Common Nails", BluetoothNode.NODE3),
                Pair("Drywall Screws", BluetoothNode.NODE4),
                Pair("Deck Screws", BluetoothNode.NODE4)
        )
        const val CHOSEN_NODE = "com.hackathon.fellas.CHOSEN_NODE"
    }
}