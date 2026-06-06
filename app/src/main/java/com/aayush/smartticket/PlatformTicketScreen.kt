@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

/* ---------- VALIDITY LOGIC ---------- */
private fun isPlatformTicketValid(createdAt: Long): Boolean {
    val TWO_HOURS = 2 * 60 * 60 * 1000L
    return System.currentTimeMillis() <= (createdAt + TWO_HOURS)
}

@Composable
fun PlatformTicketScreen(
    navController: NavController,
    booking: Booking,
    onCancelClick: () -> Unit
)
 {
    val isValid = isPlatformTicketValid(booking.createdAt)

    val validTillText = SimpleDateFormat(
        "hh:mm a",
        Locale.getDefault()
    ).format(Date(booking.createdAt + 2 * 60 * 60 * 1000L))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {

        /* ---------- HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(
                    color = Color(0xFF1565C0),
                    shape = RoundedCornerShape(
                        bottomStart = 40.dp,
                        bottomEnd = 40.dp
                    )
                )
        ) {

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(start = 12.dp, top = 12.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Platform Ticket",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "QuickBoard",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        /* ---------- TICKET CARD ---------- */
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFF1F4F8),
            tonalElevation = 2.dp
        ) {
            Column(Modifier.padding(18.dp)) {

                Text(
                    text = "PLATFORM ACCESS",
                    color = Color(0xFF1565C0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(Modifier.height(6.dp))

                val stationText =
                    if (booking.stationName.isNotBlank())
                        "${booking.stationName} (${booking.station})"
                    else
                        booking.station   // fallback for old tickets

                Text(
                    text = stationText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1C)
                )

                Spacer(Modifier.height(14.dp))

                TicketRowSoft(
                    label = "Passengers",
                    value = "${booking.adults} Person${if (booking.adults > 1) "s" else ""}"
                )

                TicketRowSoft("Ticket Type", "PLATFORM")

                TicketRowSoft(
                    label = "Total Fare",
                    value = "₹${booking.fare}",
                    bold = true
                )

                Divider(Modifier.padding(vertical = 10.dp))

                TicketRowSoft(
                    "Status",
                    if (isValid) "VALID" else "EXPIRED",
                    valueColor = if (isValid) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    bold = true
                )

                Text(
                    text = if (isValid)
                        "Valid till $validTillText (2 hours)"
                    else
                        "Ticket expired",
                    color = if (isValid) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        /* ---------- ACTION BUTTON ---------- */
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3F5B96)
            )
        ) {
            Text(
                text = "Back",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/* ---------- ROW STYLE ---------- */
@Composable
private fun TicketRowSoft(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1C1C1C),
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}
