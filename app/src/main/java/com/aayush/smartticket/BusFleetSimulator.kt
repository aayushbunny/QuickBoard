package com.aayush.smartticket

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*

data class SimulatedBus(
    val busNumber: String,
    var position: LatLng
)

class BusFleetSimulator {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun start(onUpdate: (List<SimulatedBus>) -> Unit) {

        val buses = mutableListOf<SimulatedBus>()

        busRoutes.forEach { (busNumber, stops) ->
            val firstStop = stopCoordinates[stops.firstOrNull()]
            if (firstStop != null) {
                buses.add(SimulatedBus(busNumber, firstStop))
            }
        }

        scope.launch {
            while (isActive) {

                buses.forEach { bus ->

                    val routeStops = busRoutes[bus.busNumber] ?: return@forEach

                    val nextStopName = routeStops.random()
                    val nextLatLng = stopCoordinates[nextStopName]

                    if (nextLatLng != null) {
                        bus.position = nextLatLng
                    }
                }

                onUpdate(buses.map { it.copy() })

                delay(2000)
            }
        }
    }
}
