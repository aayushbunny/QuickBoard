package com.aayush.smartticket

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FieldValue

class AdminRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getTotalUsers(): Int =
        firestore.collection("users").get().await().size()

    suspend fun getTotalBookings(): Int =
        firestore.collection("bookings")
            .whereEqualTo("status", "CONFIRMED")
            .get().await().size()

    suspend fun getAllUsers(): List<User> {
        val snapshot = firestore.collection("users").get().await()
        return snapshot.documents.map { doc ->
            User(
                id = doc.id,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                blocked = doc.getBoolean("blocked") ?: false
            )
        }
    }

    suspend fun getRecentBookings(limit: Int = 10): List<AdminBooking> {
        val snapshot = firestore.collection("bookings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents.map {
            AdminBooking(
                id = it.id,
                userId = it.getString("userId") ?: "",
                from = it.getString("fromStation") ?: "",
                to = it.getString("toStation") ?: "",
                fare = it.getDouble("fare") ?: 0.0,
                status = it.getString("status") ?: "",
                createdAt = it.getLong("createdAt") ?: 0L,
                mode = it.getString("mode") ?: "TRAIN",
                station = it.getString("station") ?: ""

            )
        }
    }


    suspend fun blockUser(userId: String) {
        firestore.collection("users").document(userId)
            .update("blocked", true).await()
    }

    suspend fun unblockUser(userId: String) {
        firestore.collection("users").document(userId)
            .update("blocked", false).await()
    }

    suspend fun deleteUserPermanently(userId: String) {
        firestore.collection("users").document(userId).delete().await()

        FirebaseFunctions.getInstance("us-central1")
            .getHttpsCallable("deleteUserAuth")
            .call(hashMapOf("uid" to userId))
            .await()
    }

    suspend fun getTodayBookingsCount(): Int {
        val todayStart = getTodayStartMillis()
        return firestore.collection("bookings")
            .whereGreaterThanOrEqualTo("createdAt", todayStart)
            .get().await().size()
    }

    suspend fun getTodayRevenueAmount(): Double {
        val todayStart = getTodayStartMillis()
        val snap = firestore.collection("bookings")
            .whereEqualTo("status", "CONFIRMED")
            .whereGreaterThanOrEqualTo("createdAt", todayStart)
            .get().await()

        return snap.documents.sumOf { it.getDouble("fare") ?: 0.0 }
    }

    suspend fun getFailedPaymentsToday(): Int {
        val todayStart = getTodayStartMillis()
        return firestore.collection("bookings")
            .whereEqualTo("status", "FAILED")
            .whereGreaterThanOrEqualTo("createdAt", todayStart)
            .get().await().size()
    }

    suspend fun getActiveUsersToday(): Int {
        val todayStart = getTodayStartMillis()
        val snap = firestore.collection("bookings")
            .whereGreaterThanOrEqualTo("createdAt", todayStart)
            .get().await()

        return snap.documents.mapNotNull { it.getString("userId") }.toSet().size
    }

    suspend fun getLoginHistory(limit: Int = 50): List<SecurityLog> {
        val snap = firestore.collection("security_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()

        return snap.documents.map {
            SecurityLog(
                id = it.id,
                email = it.getString("email") ?: "",
                success = it.getBoolean("success") ?: false,
                device = it.getString("device") ?: "",
                timestamp = it.getLong("timestamp") ?: 0L,
                 sessionId = it.getString("sessionId") ?: ""
            )
        }
    }

    suspend fun getFailedLoginsToday(): Int {
        val todayStart = getTodayStartMillis()
        return firestore.collection("security_logs")
            .whereEqualTo("success", false)
            .whereGreaterThanOrEqualTo("timestamp", todayStart)
            .get().await().size()
    }

    suspend fun logLoginAttempt(userId: String?, email: String, success: Boolean) {
        val data = hashMapOf(
            "userId" to userId,
            "email" to email,
            "success" to success,
            "device" to android.os.Build.MODEL,
            "timestamp" to System.currentTimeMillis(),
            "sessionId" to java.util.UUID.randomUUID().toString()
        )

        firestore.collection("security_logs").add(data).await()
    }

    suspend fun getMonthlyRevenue(): Double {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val snap = firestore.collection("bookings")
            .whereEqualTo("status", "CONFIRMED")
            .whereGreaterThanOrEqualTo("createdAt", cal.timeInMillis)
            .get().await()

        return snap.documents.sumOf { it.getDouble("fare") ?: 0.0 }
    }

    suspend fun getTotalRevenue(): Double {
        val snap = firestore.collection("bookings")
            .whereEqualTo("status", "CONFIRMED")
            .get().await()

        return snap.documents.sumOf { it.getDouble("fare") ?: 0.0 }
    }

    suspend fun cancelBookingByAdmin(bookingId: String) {
        firestore.collection("bookings").document(bookingId)
            .update(
                mapOf(
                    "status" to "CANCELLED",
                    "cancelledBy" to "admin",
                    "cancelledAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
