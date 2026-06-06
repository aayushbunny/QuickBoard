@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import com.aayush.smartticket.TicketFilter
import androidx.compose.runtime.saveable.rememberSaveable


import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight



private fun adminDisplayStatus(booking: AdminBooking): String {
    // 1️⃣ Cancelled has highest priority
    if (booking.status.equals("CANCELLED", ignoreCase = true)) {
        return "CANCELLED"
    }

    val now = System.currentTimeMillis()
    val createdAt = booking.createdAt

    val validityMillis = when (booking.mode.uppercase()) {
        "PLATFORM" -> 2 * 60 * 60 * 1000L // 2 hours
        "BUS" -> 4 * 60 * 60 * 1000L      // 4 hours
        else -> Long.MAX_VALUE           // TRAIN never auto-expire
    }

    return if (now > createdAt + validityMillis) {
        "EXPIRED"
    } else {
        "CONFIRMED"
    }
}
@Composable
fun AdminBookingsScreen(
    navController: NavController,
    firestoreRepository: FirestoreRepository
) {

    var bookings by remember { mutableStateOf<List<AdminBooking>>(emptyList()) }
    var selectedFilter by rememberSaveable {
        mutableStateOf(TicketFilter.ALL)
    }


    LaunchedEffect(Unit) {
        bookings = firestoreRepository.getAllBookings()
    }
    val filteredBookings = when (selectedFilter) {
        TicketFilter.ALL -> bookings
        TicketFilter.TRAIN -> bookings.filter { it.mode == "TRAIN" }
        TicketFilter.BUS -> bookings.filter { it.mode == "BUS" }
        TicketFilter.PLATFORM -> bookings.filter { it.mode == "PLATFORM" }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("All Bookings (Admin)") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ✅ FILTER CHIPS
            TicketFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // ✅ BOOKINGS LIST
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBookings) { booking ->
                    AdminBookingCardModern(booking) {
                        navController.navigate("ticket_loader/${booking.id}")
                    }
                }
            }
        }
    }

}



@Composable
fun AdminBookingCardModern(
    booking: AdminBooking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // -------- Title Row --------
            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color(0xFF007AFF),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(8.dp))

                val titleText = when (booking.mode.trim().uppercase()) {
                    "PLATFORM" -> stationDisplay(booking.station)
                    else -> "${stationDisplay(booking.from)} → ${stationDisplay(booking.to)}"
                }

                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // -------- Booking ID --------
            Text(
                text = "Booking ID: ${booking.id}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            // -------- User --------
            Text(
                text = "User: ${booking.userId}",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )

            // -------- Bottom Row --------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text("₹${booking.fare}", fontWeight = FontWeight.Bold)
                val status = adminDisplayStatus(booking)
                Text(
                    status,
                    color = when (status) {
                        "CONFIRMED" -> Color(0xFF2E7D32)
                        "EXPIRED" -> Color(0xFFD32F2F)
                        "CANCELLED" -> Color.Gray
                        else -> Color.Black
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
