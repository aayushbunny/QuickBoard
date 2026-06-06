package com.aayush.smartticket

fun adminStatus(booking: AdminBooking): String {
    val now = System.currentTimeMillis()
    val createdAt = booking.createdAt

    val validityMillis = when (booking.mode.uppercase()) {
        "PLATFORM" -> 2 * 60 * 60 * 1000L
        "BUS" -> 4 * 60 * 60 * 1000L
        else -> Long.MAX_VALUE
    }

    return if (now > createdAt + validityMillis) "EXPIRED" else "CONFIRMED"
}