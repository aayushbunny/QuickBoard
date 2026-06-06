package com.aayush.smartticket



data class Station(
    val name: String,
    val line: String,
    val km: Double
)

object StationData {

    val stations =
        WesternLine.stations +
                CentralLine.stations +
                HarbourLine.stations

    fun getStationsByName(name: String): List<Station> {
        return stations.filter { it.name == name }
    }
}