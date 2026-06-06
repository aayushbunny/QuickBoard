    @file:OptIn(ExperimentalMaterial3Api::class)

    package com.aayush.smartticket
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.ui.platform.LocalView
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.layout.onGloballyPositioned
    import androidx.core.view.drawToBitmap
    import android.graphics.Bitmap

    import android.graphics.Color as AndroidColor
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.asImageBitmap
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.google.zxing.BarcodeFormat
    import com.google.zxing.qrcode.QRCodeWriter
    import kotlinx.coroutines.launch
    import java.text.SimpleDateFormat
    import java.util.*
    import androidx.compose.ui.platform.LocalView


    @Composable
    fun TrainTicketScreen(
        navController: NavController,
        booking: Booking,
        onCancelClick: () -> Unit
    )
    {
        val scrollState = rememberScrollState()
        var captured by remember { mutableStateOf(false) }


        val context = LocalContext.current
        var ticketBitmap by remember { mutableStateOf<Bitmap?>(null) }
        val view = LocalView.current


        val scope = rememberCoroutineScope()
        val firestoreRepository = remember { FirestoreRepository() }

        var showCancelDialog by remember { mutableStateOf(false) }

        val bookedOn = remember(booking.createdAt) {
            SimpleDateFormat(
                "dd MMM yyyy • hh:mm a",
                Locale.getDefault()
            ).format(Date(booking.createdAt))
        }

        val passengerText = buildString {
            append("Adults: ${booking.adults}")
            if (booking.children > 0) {
                append(", Children: ${booking.children}")
            }
        }

        val statusColor =
            if (booking.status == "CANCELLED") Color(0xFFD32F2F)
            else Color(0xFF2E7D32)

        /* ---------- SAFE DISPLAY VALUES ---------- */

        val ticketType = booking.ticketType.ifBlank { "JOURNEY" }
        val trainType = booking.trainType.ifBlank { "ORDINARY" }
        val ticketClass = booking.ticketClass.ifBlank { "SECOND" }
        val fareText = "₹${booking.fare.coerceAtLeast(0)}"

        /* ---------- QR ---------- */

        val qrData = """
            TYPE:${booking.mode}
            FROM:${'$'}{booking.from} (${'$'}{booking.fromCode})
            TO:${'$'}{booking.to} (${'$'}{booking.toCode})
            TICKET_TYPE:$ticketType
            TRAIN_TYPE:$trainType
            CLASS:$ticketClass
            ADULTS:${booking.adults}
            CHILDREN:${booking.children}
            FARE:${booking.fare}
            STATUS:${booking.status}
            BOOKED_ON:$bookedOn
        """.trimIndent()

        val qrBitmap = remember(qrData) {
            generateQrCode(qrData)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(0xFFF5F8FB))
        ) {

            /* ---------- HEADER ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                        ),
                        shape = RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp
                        )
                    )
            ) {

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "QuickBoard",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- TICKET CARD ---------- */

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .onGloballyPositioned {
                        if (!captured) {
                            captured = true
                            view.post {
                                ticketBitmap = view.drawToBitmap()
                            }
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
            Column(
                    modifier = Modifier
                        .background(Color(0xFFEFF3F7))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "LOCAL TRAIN",
                        color = Color(0xFF0D47A1),
                        fontWeight = FontWeight.Bold
                    )
                Text(
                    text = "${stationDisplay(booking.from)} → ${stationDisplay(booking.to)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )



                Divider()

                    TicketRow("Booked On", bookedOn)
                    TicketRow("Passengers", passengerText)
                    TicketRow("Ticket Type", ticketType)
                    TicketRow("Train Type", trainType)
                    TicketRow("Class", ticketClass)
                if (!booking.concession.isNullOrBlank()) {
                    TicketRow("Concession", booking.concession)
                }

                TicketRow("Total Fare", fareText)
// ⏱ Journey validity info



                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status", color = Color.Gray)
                        Text(
                            booking.status,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                Text(
                    text = "JOURNEY SHOULD COMMENCE WITHIN 1 HOUR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                    Spacer(Modifier.height(10.dp))

                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- CANCEL ---------- */
            if (booking.status == "CONFIRMED") {
                Button(
                    onClick = { showCancelDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cancel Ticket", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))
            }

            /* ---------- DONE ---------- */
            OutlinedButton(
                onClick = {
                    ticketBitmap?.let {
                        shareBitmap(context, it)
                        // or shareBitmapAsPdf(context, it)
                    }
                },
                enabled = ticketBitmap != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = if (ticketBitmap == null) "Preparing ticket…" else "Share Ticket",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("myBookings") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }


        Spacer(Modifier.height(16.dp))

        /* ---------- CANCEL DIALOG ---------- */
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Cancel Train Ticket?") },
                text = { Text("This ticket will be marked as cancelled.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                firestoreRepository.cancelBooking(booking.id)
                                navController.navigate("myBookings") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        }
                    ) { Text("Yes, Cancel") }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }

    /* ---------- HELPERS ---------- */

    @Composable
    private fun TicketRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }

    private fun generateQrCode(content: String, size: Int = 400): Bitmap {
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x,
                    y,
                    if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                )
            }
        }
        return bitmap
    }
