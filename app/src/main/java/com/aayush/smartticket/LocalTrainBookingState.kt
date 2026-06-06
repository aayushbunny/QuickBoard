package com.aayush.smartticket

object LocalTrainBookingState {


    var mode: String = "TRAIN"

    var from: String = ""
    var to: String = ""
    var station: String = ""   // only for PLATFORM
    var stationName: String? = null
    var adults: Int = 1
    var children: Int = 0

    var ticketType: String = "JOURNEY"
    var trainType: String = "ORDINARY"
    var ticketClass: String = "SECOND"

    var fare: Int = 0

    fun clear() {
        mode = "TRAIN"
        from = ""
        to = ""
        station = ""
        adults = 1
        children = 0
        ticketType = "JOURNEY"
        trainType = "ORDINARY"
        ticketClass = "SECOND"
        fare = 0
    }
    var concession: String? = null
}
