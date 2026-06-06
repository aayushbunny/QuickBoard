package com.aayush.smartticket

object BusBookingState {
    var route: String = ""
    var isBusBooking: Boolean = false   // ✅ ADD THIS

    var busNumber: String = ""
    var fromStop: String = ""
    var toStop: String = ""
    var busType: String = ""

    var adults: Int = 0
    var children: Int = 0

    var stops: Int = 0
    var fare: Int = 0

    fun clear() {
        isBusBooking = false           // ✅ RESET FLAG
        busNumber = ""
        fromStop = ""
        toStop = ""
        adults = 0
        children = 0
        stops = 0
        fare = 0
    }
}
