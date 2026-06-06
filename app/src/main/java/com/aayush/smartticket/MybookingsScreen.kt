@file:OptIn(ExperimentalMaterial3Api::class)
package com.aayush.smartticket

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.net.Uri

import kotlinx.coroutines.launch


@Composable
fun MyBookingsScreen(
    navController: NavController,
    firestoreRepository: FirestoreRepository
) {

    val scope = rememberCoroutineScope()

    var selectedFilter by rememberSaveable {
        mutableStateOf(TicketFilter.ALL)
    }

    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    LaunchedEffect(Unit) {
        bookings = firestoreRepository
            .getUserBookings()
            .sortedByDescending { it.createdAt }
    }
    val context = LocalContext.current

    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    fun deleteBooking(booking: Booking) {
        scope.launch {
            try {
                firestoreRepository.deleteBooking(booking.id)

                bookings = bookings.filterNot { it.id == booking.id }

                Toast.makeText(
                    context,
                    "Ticket deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to delete ticket",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    if (selectedBooking != null) {
        AlertDialog(
            onDismissRequest = { selectedBooking = null },
            title = { Text("Delete Ticket") },
            text = { Text("Are you sure you want to delete this ticket?") },

            confirmButton = {
                TextButton(
                    onClick = {
                        deleteBooking(selectedBooking!!)
                        selectedBooking = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },

            dismissButton = {
                TextButton(onClick = { selectedBooking = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(title = { Text("My Bookings") })

        TicketFilterRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )


        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookings found")
            }
        }
        else {
            val filteredBookings = when (selectedFilter) {
                TicketFilter.ALL -> bookings
                TicketFilter.TRAIN -> bookings.filter { it.mode == "TRAIN" }
                TicketFilter.BUS -> bookings.filter { it.mode == "BUS" }
                TicketFilter.PLATFORM -> bookings.filter { it.mode == "PLATFORM" }
                else -> bookings
            }


            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBookings) { booking ->
                    val isBusExpiredForActions =
                        booking.mode == "BUS" &&
                                System.currentTimeMillis() >
                                (booking.createdAt + (4 * 60 * 60 * 1000))


                    BookingItem(
                        booking = booking,
                        onClick = {
                            navController.navigate("ticket_loader/${booking.id}")
                        },
                        onLongClick = {
                            selectedBooking = booking
                        }

                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isBusExpiredForActions =
                            booking.mode == "BUS" &&
                                    System.currentTimeMillis() >
                                    (booking.createdAt + (4 * 60 * 60 * 1000))

                        Button(
                            onClick = {
                                navController.navigate("ticket_loader/${booking.id}")
                            }
                        ) {
                            Text("View")
                        }



                        if (booking.mode == "TRAIN" || booking.mode == "BUS") {
                            OutlinedButton(
                                onClick = {

                                    if (booking.mode == "TRAIN") {

                                        val from = Uri.encode(booking.from ?: "")
                                        val to = Uri.encode(booking.to ?: "")
                                        val ticketType = Uri.encode(booking.ticketType ?: "")
                                        val trainType = Uri.encode(booking.trainType ?: "")

                                        navController.navigate(
                                            "rebook_train/$from/$to/${booking.adults}/$ticketType/$trainType"
                                        )

                                    } else if (booking.mode == "BUS") {

                                        val route = Uri.encode(booking.busNumber ?: "")
                                        val from = Uri.encode(booking.from ?: "")
                                        val to = Uri.encode(booking.to ?: "")
                                        val busType = Uri.encode(booking.busType ?: "Non-AC")

                                        navController.navigate(
                                            "rebook_bus/$route/$from/$to/${booking.adults}/$busType"
                                        )
                                    }
                                }
                            ) {
                                Text("Rebook 🔁")
                            }
                        }


                    }

                }}
        }
    }


}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookingItem(
    booking: Booking,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

    // 🚉 Platform ticket expiry: 2 hours
    val platformExpiryTime = remember(booking.createdAt) {
        booking.createdAt + (2 * 60 * 60 * 1000)
    }

    val isPlatformExpired = booking.mode == "PLATFORM" &&
            System.currentTimeMillis() > platformExpiryTime

    val dateText = remember(booking.createdAt) {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(booking.createdAt))
    }

    // ✅ BUS EXPIRY LOGIC (CORRECT)
    val busExpiryTime = remember(booking.createdAt) {
        booking.createdAt + (4 * 60 * 60 * 1000)
    }

    val isBusExpired = booking.mode == "BUS" &&
            System.currentTimeMillis() > busExpiryTime

    val displayStatus = when {
        isBusExpired -> "EXPIRED"
        isPlatformExpired -> "EXPIRED"
        else -> booking.status
    }

    val expiryText = remember(busExpiryTime) {
        SimpleDateFormat(
            "hh:mm a",
            Locale.getDefault()
        ).format(Date(busExpiryTime))
    }


    val isTrain = booking.mode == "TRAIN"
    val isBus = booking.mode == "BUS"
    val isPlatform = booking.mode == "PLATFORM"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),

                elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                text = when {
                    isTrain -> "${booking.from} → ${booking.to}"
                    isBus -> booking.busNumber?.takeIf { it.isNotBlank() }
                        ?.let { "Bus $it" }
                        ?: "${booking.from} → ${booking.to}"

                    isPlatform -> booking.station.ifBlank { "Platform Ticket" }
                    else -> "Ticket"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(dateText, fontSize = 13.sp, color = Color.Gray)
             
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                TicketTag(
                    text = when {
                        isTrain -> "TRAIN"
                        isBus -> "BUS"
                        isPlatform -> "PLATFORM"
                        else -> "TICKET"
                    },
                    color = when {
                        isTrain -> Color(0xFF1976D2)
                        isBus -> Color(0xFF6A1B9A)
                        isPlatform -> Color(0xFF2E7D32)
                        else -> Color.Gray
                    }
                )
                TicketTag(
                    text = displayStatus,
                    color = when (displayStatus) {
                        "CONFIRMED" -> Color(0xFF2E7D32)
                        "EXPIRED" -> Color(0xFFC62828)
                        else -> Color.Gray
                    }
                )





            }

            Spacer(Modifier.height(10.dp))

            Text(
                "₹${booking.fare}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TicketTag(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun TicketFilterRow(
    selectedFilter: TicketFilter,
    onFilterSelected: (TicketFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TicketFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                enabled = true, // 🔒 force enabled
                label = {
                    Text(filter.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            )
        }
    }
}

