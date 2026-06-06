package com.aayush.smartticket

interface BusLocationProvider {
    fun start(onUpdate: (Double, Double) -> Unit)
}

