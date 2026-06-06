package com.aayush.smartticket

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }


    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signup(email: String, password: String) {
        val result = auth
            .createUserWithEmailAndPassword(email, password)
            .await()

        val uid = result.user?.uid
            ?: throw IllegalStateException("User ID is null")

        val userData = hashMapOf(
            "email" to email,
            "role" to "USER", // default role
            "createdAt" to System.currentTimeMillis()
        )

        firestore
            .collection("users")
            .document(uid)
            .set(userData)
            .await()
    }

    // ✅ ADD THIS FUNCTION
    suspend fun getUserRole(uid: String): String {
        val snapshot = firestore
            .collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.getString("role") ?: "USER"
    }

    fun logout() {
        auth.signOut()
    }
}
