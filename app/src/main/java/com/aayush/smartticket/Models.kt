package com.aayush.smartticket

/* ---------------- USER PROFILE ---------------- */

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",

    // 🔐 AUTH / LOGIN INFO
    val isPhoneVerified: Boolean = false,   // future OTP login
    val loginProvider: String = "EMAIL",    // EMAIL / PHONE
    val role: String = "USER",      // ✅ ADD
    val blocked: Boolean = false,
    // 🖼️ PROFILE
    val profileImageUrl: String = "",

    // ⏱️ META
    val createdAt: Long = System.currentTimeMillis()
)


/* ---------------- ENUMS ---------------- */

enum class TicketCategory {
    JOURNEY,
    PLATFORM
}

enum class JourneyMode {
    SUBURBAN,
    ALL_STATIONS
}

enum class JourneyType {
    SINGLE,
    RETURN
}


enum class TravelClass {
    SECOND,
    FIRST
}

/* ---------------- SESSION ---------------- */

object BookingSession {
    var pendingBooking: Booking? = null
}
