package com.aayush.smartticket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketConfirmationScreen(
    navController: NavController,
    bookingId: String,     // ✅ REAL Firestore ID
    mode: String,
    from: String,
    to: String,
    passengers: Int,
    totalFare: Int
) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ticket Confirmed") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Booking Successful",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ticket ID: $bookingId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ConfirmationRow("Mode", mode)
                        ConfirmationRow("From", from)
                        ConfirmationRow("To", to)
                        ConfirmationRow("Passengers", passengers.toString())
                        ConfirmationRow("Total Fare", "₹$totalFare")
                    }
                }
            }

            Column {

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    onClick = {
                        // ✅ OPEN EXACT TICKET
                        navController.navigate("ticketLoader/$bookingId") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                ) {
                    Text("View Ticket")
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    }
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

/* ---------- HELPERS ---------- */

@Composable
private fun ConfirmationRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
