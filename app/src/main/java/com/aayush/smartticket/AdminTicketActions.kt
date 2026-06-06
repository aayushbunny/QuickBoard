package com.aayush.smartticket

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminTicketActions {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun cancelBooking(bookingId: String) {
        firestore.collection("bookings")
            .document(bookingId)
            .update("status", "CANCELLED")
            .await()
    }

    suspend fun blockUser(userId: String) {
        firestore.collection("users")
            .document(userId)
            .update("blocked", true)
            .await()
    }
}
