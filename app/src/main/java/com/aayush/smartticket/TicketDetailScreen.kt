@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TicketDetailScreen(
    navController: NavController,
    booking: Booking
) {
    // 🔒 Disable system back (UTS-style)
    BackHandler {
        navController.navigate("myBookings") {
            popUpTo("home") { inclusive = false }
            launchSingleTop = true
        }
    }
    val bookedOn = remember(booking.createdAt) {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(booking.createdAt))
    }

    // ✅ Derived passenger count (single source of truth)
    val passengerText = buildString {
        append("Adults: ${booking.adults}")
        if (booking.children > 0) {
            append(", Children: ${booking.children}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {

        /* ---------- HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp)
        ) {

            IconButton(
                onClick = {
                    navController.navigate("myBookings") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Train Ticket",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "QuickBoard",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        /* ---------- CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFFF4F6F8))
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "LOCAL TRAIN",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${booking.from} → ${booking.to}",
                        fontWeight = FontWeight.Medium
                    )


                    Divider(Modifier.padding(vertical = 12.dp))

                    SummaryRow("Booked On", bookedOn)
                    SummaryRow("Passengers", passengerText)
                    SummaryRow("Ticket Type", booking.ticketType)
                    SummaryRow("Train Type", booking.trainType)
                    SummaryRow("Class", booking.ticketClass)
                    SummaryRow("Total Fare", "₹${booking.fare}", bold = true)
                    SummaryRow("Status", booking.status, bold = true)
                }
            }

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Scan at Entry Gate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(12.dp))

                val qrBitmap = remember(booking.id) {
                    generateQrCodeBitmap(
                        """
TYPE:${booking.mode}
FROM:${booking.from}
TO:${booking.to}
TICKET_TYPE:${booking.ticketType}
TRAIN_TYPE:${booking.trainType}
CLASS:${booking.ticketClass}
ADULTS:${booking.adults}
CHILDREN:${booking.children}
FARE:${booking.fare}
STATUS:${booking.status}
BOOKED_ON:$bookedOn
        """.trimIndent()
                    )
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(220.dp)
                            .padding(16.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate("myBookings") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/* ---------- QR CODE GENERATOR ---------- */

fun generateQrCodeBitmap(content: String): Bitmap {
    val size = 512
    val bits = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bits[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
            )
        }
    }
    return bitmap
}
