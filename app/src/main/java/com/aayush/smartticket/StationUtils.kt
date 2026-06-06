package com.aayush.smartticket

fun stationDisplay(name: String): String {
    val station = StationData.stations.find { it.name == name }
    return station?.name ?: name
}