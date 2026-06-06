package com.aayush.smartticket

import kotlin.math.abs

object FareCalculator {

    fun calculateDistance(fromKm: Double, toKm: Double): Double {
        return abs(toKm - fromKm)
    }

    fun calculateFare(distance: Double): Int {
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