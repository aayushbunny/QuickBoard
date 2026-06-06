package com.aayush.smartticket

import kotlinx.coroutines.*

class SimulatedBusLocationProvider : BusLocationProvider {

    private val route = listOf(
        19.0760 to 72.8777,
        19.0775 to 72.8790,
        19.0790 to 72.8805,
        19.0810 to 72.8830,
        19.0835 to 72.8860
    )

    private var job: Job? = null

    override fun start(onUpdate: (Double, Double) -> Unit) {

        job = CoroutineScope(Dispatchers.Default).launch {

            while (isActive) {
                for (point in route) {
                    onUpdate(point.first, point.second)
                    delay(1200)
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}
