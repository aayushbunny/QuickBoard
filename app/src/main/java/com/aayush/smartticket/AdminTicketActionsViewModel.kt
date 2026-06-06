package com.aayush.smartticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf

class AdminTicketActionsViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    var isLoading = mutableStateOf(false)
        private set
    fun cancelBooking(
        bookingId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                repository.cancelBooking(bookingId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Cancel failed")
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun isExpiredAdmin(booking: Booking): Boolean {

        val now = System.currentTimeMillis()
        val createdAt = booking.createdAt

        val validityMillis = when (booking.mode) {
            "PLATFORM" -> 2 * 60 * 60 * 1000L   // 2 hours
            "BUS" -> 4 * 60 * 60 * 1000L        // 4 hours
            else -> Long.MAX_VALUE
        }

        return now > createdAt + validityMillis
    }

    fun cancelIfActive(
        booking: Booking,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isExpiredAdmin(booking)) {
            onError("Ticket already expired")
            return
        }
        cancelBooking(booking.id, onSuccess, onError)
    }

    // ✅ THIS MUST BE SUSPEND
    suspend fun getBookingById(bookingId: String): Booking? {
        return repository.getBookingById(bookingId)
    }

    fun deleteBooking(
        bookingId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteBooking(bookingId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Delete failed")
            }
        }
    }
}