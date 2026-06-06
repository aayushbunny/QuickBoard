package com.aayush.smartticket

import  com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query




class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")
    private val usersCollection = firestore.collection("users")


    /* --------------------------------------------------
       BOOKINGS
    -------------------------------------------------- */

        suspend fun saveBookingAndReturnId(booking: Booking): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        val normalizedBooking = booking.copy(
            userId = uid,
            createdAt = System.currentTimeMillis(), // 🔥 IMPORTANT
            mode = booking.mode.uppercase(),
            ticketType = booking.ticketType.uppercase(),
            trainType = booking.trainType.uppercase(),
            ticketClass = booking.ticketClass.uppercase(),
            concession = booking.concession,
                    stationName = booking.stationName
        )

        return bookingsCollection.add(normalizedBooking).await().id
    }

    suspend fun getBookingById(bookingId: String): Booking? {
        val doc = bookingsCollection.document(bookingId).get().await()
        return if (doc.exists()) {
            mapDocToBooking(doc.id, doc.data ?: emptyMap())
        } else null
    }

    suspend fun getUserBookings(): List<Booking> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

        val snapshot = bookingsCollection
            .whereEqualTo("userId", uid)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
                mapDocToBooking(it.id, it.data ?: emptyMap())
        }
    }

    suspend fun cancelBooking(bookingId: String) {
        firestore.collection("bookings")
            .document(bookingId)
            .update("status", "CANCELLED")
            .await()
    }

    suspend fun deleteBooking(bookingId: String) {
        firestore.collection("bookings")
            .document(bookingId)
            .delete()
            .await()
    }


    /* --------------------------------------------------
       USER PROFILE
    -------------------------------------------------- */
    suspend fun getUserProfile(uid: String): UserProfile? {
        val doc = usersCollection.document(uid).get().await()
        return if (doc.exists()) {
            doc.toObject(UserProfile::class.java)
        } else {
            null   // ✅ do NOT create here
        }
    }
    // --------------------------------------------------
// SAVE / UPDATE USER PROFILE
// --------------------------------------------------
    suspend fun createUserProfile(
        uid: String,
        name: String,
        email: String,
        mobile: String
    ) {
        val profile = UserProfile(
            userId = uid,
            name = name,
            email = email,
            mobile = mobile,
            role = "USER",
            blocked = false,
            createdAt = System.currentTimeMillis()
        )

        usersCollection
            .document(uid)
            .set(profile)   // ✅ CREATE document
            .await()
    }
    suspend fun saveUserProfile(
        uid: String,
        name: String,
        email: String,
        mobile: String
    ) {
        val updates = mapOf(
            "name" to name,
            "email" to email,
            "mobile" to mobile,
            "updatedAt" to System.currentTimeMillis()
        )

        usersCollection
            .document(uid)
            .update(updates)   // ✅ UPDATE (not create)
            .await()
    }
    suspend fun getAllBookings(): List<AdminBooking> {
        val snapshot = firestore
            .collection("bookings")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            mapDocToAdminBooking(it.id, it.data ?: emptyMap())
        }
    }

    suspend fun cancelBookingByAdmin(bookingId: String) {
        firestore.collection("bookings")
            .document(bookingId)
            .update(
                mapOf(
                    "status" to "CANCELLED",
                    "cancelledAt" to FieldValue.serverTimestamp(),
                    "cancelledBy" to "admin"
                )
            )
            .await()
    }

    suspend fun saveCommuteMemory(booking: Booking) {

        val uid = booking.userId
        val routeId = "${booking.from}_${booking.to}_${booking.ticketType}_${booking.trainType}_${booking.ticketClass}"

        val ref = firestore
            .collection("users")
            .document(uid)
            .collection("commute_memory")
            .document(routeId)

        val doc = ref.get().await()

        if (doc.exists()) {
            ref.update(
                mapOf(
                    "useCount" to FieldValue.increment(1),
                    "lastUsedAt" to System.currentTimeMillis()
                )
            ).await()
        } else {
            val memory = CommuteMemory(
                from = booking.from,
                to = booking.to,
                ticketType = booking.ticketType,
                trainType = booking.trainType,
                ticketClass = booking.ticketClass,
                adults = booking.adults,
                useCount = 1,
                lastUsedAt = System.currentTimeMillis()
            )

            ref.set(memory).await()
        }
    }

    suspend fun getTopCommuteMemory(): CommuteMemory? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        val snapshot = firestore
            .collection("users")
            .document(uid)
            .collection("commute_memory")
            .orderBy("useCount", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(CommuteMemory::class.java)
    }


    suspend fun adminMarkExpired(bookingId: String) {
        firestore
            .collection("bookings")
            .document(bookingId)
            .update("status", "EXPIRED")
            .await()
    }

    suspend fun getUserRole(uid: String): String {
        val doc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .await()

        return doc.getString("role") ?: "USER"
    }


    suspend fun isMobileExists(mobile: String): Boolean {
        val snapshot = usersCollection
            .whereEqualTo("mobile", mobile)
            .limit(1)
            .get()
            .await()

        return !snapshot.isEmpty
    }



    /* --------------------------------------------------
       FIRESTORE → BOOKING MAPPER
    -------------------------------------------------- */





    private fun mapDocToAdminBooking(
        id: String,
        data: Map<String, Any>
    ): AdminBooking {

        val fromRaw =
            (data["from"] as? String)?.takeIf { it.isNotBlank() }
                ?: (data["fromPlace"] as? String).orEmpty()

        val toRaw =
            (data["to"] as? String)?.takeIf { it.isNotBlank() }
                ?: (data["toPlace"] as? String).orEmpty()

        return AdminBooking(
            id = id,
            userId = data["userId"] as? String ?: "",
            from = fromRaw,
            to = toRaw,
            fare = (data["fare"] as? Number)?.toDouble() ?: 0.0,
            status = data["status"] as? String ?: "",
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
            mode = (data["mode"] as? String ?: "TRAIN").uppercase(),
            station = data["station"] as? String ?: "",

            ticketType = (data["ticketType"] as? String ?: "JOURNEY"),
            trainType  = (data["trainType"] as? String ?: "ORDINARY"),
            ticketClass = (data["ticketClass"] as? String ?: "SECOND"),
            concession = data["concession"] as? String
        )
    }

    private fun mapDocToBooking(
        id: String,
        data: Map<String, Any>
    ): Booking {

        val mode = (data["mode"] as? String ?: "TRAIN").uppercase()
        val isPlatform = mode == "PLATFORM"

        val fromRaw =
            (data["from"] as? String)?.takeIf { it.isNotBlank() }
                ?: (data["fromPlace"] as? String).orEmpty()

        val toRaw =
            (data["to"] as? String)?.takeIf { it.isNotBlank() }
                ?: (data["toPlace"] as? String).orEmpty()

        return Booking(
            id = id,
            userId = data["userId"] as? String ?: "",

            busNumber = data["busNumber"] as? String ?: "",
            busType = data["busType"] as? String ?: "",

            mode = mode,

            from = if (isPlatform) data["station"] as? String ?: "" else fromRaw,
            to = if (isPlatform) data["station"] as? String ?: "" else toRaw,

            // ⭐ PLATFORM FIX
            station = if (isPlatform) data["station"] as? String ?: "" else "",
            stationName = data["stationName"] as? String ?: "", // 🔥 ADD THIS

            adults = (data["adults"] as? Number)?.toInt() ?: 1,
            children = (data["children"] as? Number)?.toInt() ?: 0,

            ticketType = data["ticketType"] as? String ?: "JOURNEY",
            trainType = data["trainType"] as? String ?: "ORDINARY",
            ticketClass = data["ticketClass"] as? String ?: "SECOND",

            fare = (data["fare"] as? Number)?.toInt() ?: 0,
            status = data["status"] as? String ?: "CONFIRMED",
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,

            concession = data["concession"] as? String
        )
    }







}









