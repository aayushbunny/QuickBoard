package com.aayush.smartticket

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirestoreBusLocationProvider(
    private val firestore: FirebaseFirestore,
    private val busNumber: String
) : BusLocationProvider {

    private var listener: ListenerRegistration? = null

    override fun start(onUpdate: (Double, Double) -> Unit) {

        listener = firestore
            .collection("busLocations")
            .document(busNumber)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val lat = snapshot.getDouble("lat") ?: return@addSnapshotListener
                val lng = snapshot.getDouble("lng") ?: return@addSnapshotListener

                onUpdate(lat, lng)
            }
    }

    fun stop() {
        listener?.remove()
    }
}
