package com.aayush.smartticket

data class AdminBooking(
    val id: String,
    val userId: String,
    val from: String,
    val to: String,
    val fare: Double,
    val status: String,
    val createdAt: Long,
    val mode: String,
    val station: String = "",
    val ticketType: String = "",
    val trainType: String = "",
    val ticketClass: String = "",
    val concession: String? = null

)

