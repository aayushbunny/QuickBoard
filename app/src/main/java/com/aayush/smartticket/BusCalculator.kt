package com.aayush.smartticket

import kotlin.math.abs
import kotlin.math.ceil

fun calculateBusFare(
    routeStops: List<String>,
    fromStop: String,
    toStop: String,
    adults: Int,
    children: Int,
    busType: String
): Int {
    if (fromStop.isBlank() || toStop.isBlank()) return 0
    if (fromStop == toStop) return 0
    if (adults <= 0) return 0

    val fromIndex = routeStops.indexOf(fromStop)
    val toIndex = routeStops.indexOf(toStop)

    if (fromIndex == -1 || toIndex == -1) return 0

    val stopCount = abs(fromIndex - toIndex)

    val baseAdultFare = when {
        stopCount <= 3 -> 10
        stopCount <= 7 -> 15
        stopCount <= 12 -> 20
        else -> 25
    }

    val adjustedAdultFare =
        if (busType == "AC") ceil(baseAdultFare * 1.5).toInt()
        else baseAdultFare

    val adultTotal = adults * adjustedAdultFare
    val childTotal = children * (adjustedAdultFare / 2)

    return adultTotal + childTotal
}
