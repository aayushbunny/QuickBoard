package com.aayush.smartticket

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.widget.Toast

@Composable
fun TicketLoaderScreen(
    navController: NavController,
    bookingId: String,
    firestoreRepository: FirestoreRepository
) {
    val scope = rememberCoroutineScope()


    var booking by remember { mutableStateOf<Booking?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val role = firestoreRepository.getUserRole(uid)
                isAdmin = role == "ADMIN"
            }

            booking = firestoreRepository.getBookingById(bookingId)

            if (booking == null) {
                error = "Ticket not found"
            }
        } catch (e: Exception) {
            error = e.message ?: "Failed to load ticket"
        } finally {
            loading = false
        }
    }

    when {
        loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Unknown error")
            }
        }

        booking != null -> {
            val safeBooking = booking!!

            val cancelTicket: () -> Unit = {
                scope.launch {
                    try {
                        firestoreRepository.cancelBooking(bookingId)

                        navController.popBackStack("myBookings", false)

                    } catch (e: Exception) {
                        e.printStackTrace()

                        Toast.makeText(
                            navController.context,
                            "Failed to cancel ticket: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }


            if (isAdmin) {
                AdminTicketScreen(
                    navController = navController,
                    bookingId = bookingId
                )

            } else {
                when (safeBooking.mode.trim().uppercase()) {
                    "TRAIN" -> TrainTicketScreen(navController, safeBooking, cancelTicket)
                    "BUS" -> BusTicketScreen(navController, safeBooking, cancelTicket)
                    "PLATFORM" -> PlatformTicketScreen(navController, safeBooking, cancelTicket)
                    else -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Unknown ticket type")
                        }
                    }
                }
            }
        }
    }
}