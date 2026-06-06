package com.aayush.smartticket

data class SecurityLog(
    val id: String = "",
    val email: String = "",
    val success: Boolean = false,
    val device: String = "",
    val timestamp: Long = 0L,
    val sessionId: String = ""
)
