package com.aayush.smartticket
data class Booking(

    val id: String = "",
    val userId: String = "",
    val mode: String = "",

    val journeyType: JourneyType = JourneyType.SINGLE,

    // BUS
    val busNumber: String = "",
    val busType: String = "",
    val route: String = "",

    // COMMON
    val from: String = "",
    val to: String = "",
    val fromCode: String = "",
    val toCode: String = "",

    val fare: Int = 0,
    val adults: Int = 0,
    val children: Int = 0,

    // PLATFORM
    val station: String = "",
    val stationName: String = "",

    // TRAIN
    val ticketClass: String = "",
    val ticketType: String = "",
    val trainType: String = "",

    val status: String = "",
    val createdAt: Long = 0L,
    val concession: String? = null
)
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val blocked: Boolean = false
)
data class CommuteMemory(
    val from: String = "",
    val to: String = "",
    val ticketType: String = "",
    val trainType: String = "",
    val ticketClass: String = "",
    val adults: Int = 1,
    val useCount: Int = 0,
    val lastUsedAt: Long = 0L
)



