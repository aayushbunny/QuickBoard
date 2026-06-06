package com.aayush.smartticket

import kotlin.math.abs

object RouteCalculator {

    fun calculateFare(from: String, to: String): Int {

        val fromStation = StationData.stations.find { it.name == from }
        val toStation = StationData.stations.find { it.name == to }

        if (fromStation == null || toStation == null) return 0

        val distance = if (fromStation.line == toStation.line) {

            abs(toStation.km - fromStation.km)

        } else {
            // Interchange at Dadar

            val dadarWestern = StationData.stations.find {
                it.name == "Dadar" && it.line == "Western"
            }

            val dadarCentral = StationData.stations.find {
                it.name == "Dadar" && it.line == "Central"
            }

            if (dadarWestern == null || dadarCentral == null) return 0

            val part1 = abs(dadarWestern.km - fromStation.km)
            val part2 = abs(toStation.km - dadarCentral.km)

            part1 + part2
        }

        return calculateFareFromDistance(distance)
    }

    private fun calculateFareFromDistance(distance: Double): Int {
        return when {
            distance <= 10 -> 5
            distance <= 15 -> 10
            distance <= 20 -> 15
            distance <= 25 -> 20
            distance <= 30 -> 25
            else -> 30
        }
    }
}