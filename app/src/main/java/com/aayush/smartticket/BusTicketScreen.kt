    @file:OptIn(ExperimentalMaterial3Api::class)

    package com.aayush.smartticket
    import android.location.Geocoder
    import android.content.Context
    import com.google.maps.android.compose.Polyline
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.ui.platform.LocalView
    import androidx.core.view.drawToBitmap
    import android.graphics.Bitmap
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.layout.onGloballyPositioned
    import com.google.maps.android.compose.*
    import com.google.android.gms.maps.model.*
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import java.text.SimpleDateFormat
    import java.util.*
    import com.google.android.gms.maps.CameraUpdateFactory
    import androidx.compose.ui.draw.clip
    import com.google.maps.android.compose.GoogleMap
    import com.google.maps.android.compose.MarkerState
    import com.google.maps.android.compose.rememberCameraPositionState
    import com.google.android.gms.maps.model.LatLng
    import kotlinx.coroutines.launch

    fun lerp(start: LatLng, end: LatLng, fraction: Float): LatLng {
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lng = start.longitude + (end.longitude - start.longitude) * fraction
        return LatLng(lat, lng)
    }

    fun getLatLng(context: Context, place: String): LatLng? {
        return try {
            val geocoder = Geocoder(context)
            val list = geocoder.getFromLocationName("$place Mumbai", 1)
            if (!list.isNullOrEmpty()) {
                LatLng(list[0].latitude, list[0].longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }




    @Composable
    fun BusTicketScreen(
        navController: NavController,
        booking: Booking,
        onCancelClick: () -> Unit
    )
     {
         val scope = rememberCoroutineScope()
         val busNumber = booking.busNumber.ifBlank { "BUS" }


         val context = LocalContext.current

         var fromLatLng by remember { mutableStateOf<LatLng?>(null) }
         var toLatLng by remember { mutableStateOf<LatLng?>(null) }
         val view = LocalView.current
        var ticketBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var captured by remember { mutableStateOf(false) }

        val scrollState = rememberScrollState()



         var busLatLng by remember { mutableStateOf(LatLng(19.0760, 72.8777)) }

         val markerState = remember { MarkerState(busLatLng) }

         LaunchedEffect(busLatLng) {
             markerState.position = busLatLng
         }



         val dateText = remember(booking.createdAt) {
            SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(Date(booking.createdAt))
        }
         val bookingMode = booking.mode.trim().uppercase()

// 🚌 Bus expiry (4 hours)
         val isBusExpired = bookingMode == "BUS" &&
                 System.currentTimeMillis() >
                 booking.createdAt + (4 * 60 * 60 * 1000)

// 🚉 Platform expiry (2 hours)
         val isPlatformExpired = bookingMode == "PLATFORM" &&
                 System.currentTimeMillis() >
                 booking.createdAt + (2 * 60 * 60 * 1000)

// ✅ THIS is what UI must use
         val displayStatus = when {
             isBusExpired -> "EXPIRED"
             isPlatformExpired -> "EXPIRED"
             else -> booking.status
         }
         val cameraState = rememberCameraPositionState()

         val passengerText = buildString {
            append("Adults: ${booking.adults}")
            if (booking.children > 0) {
                append(", Children: ${booking.children}")
            }
        }

         LaunchedEffect(booking.from, booking.to) {
             fromLatLng = getLatLng(context, booking.from)
             toLatLng = getLatLng(context, booking.to)
         }

         LaunchedEffect(fromLatLng, toLatLng) {
             if (fromLatLng != null && toLatLng != null) {
                 val bounds = LatLngBounds.builder()
                     .include(fromLatLng!!)
                     .include(toLatLng!!)
                     .build()

                 cameraState.animate(
                     CameraUpdateFactory.newLatLngBounds(bounds, 100)
                 )
             }
         }



         Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6F8))
        ) {

            /* ---------- SCROLLABLE CONTENT ---------- */


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF4F6F8))
                    .verticalScroll(scrollState)
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
                            text = "Bus Ticket",
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


                Spacer(modifier = Modifier.height(24.dp))
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
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "BUS TICKET",
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1976D2)
                            )
                        }

                        Divider()

                        if (booking.busType.isNotBlank()) {
                            TicketRow("Bus Type", booking.busType)   // ✅
                        }
                        TicketRow("Bus Number", booking.busNumber.ifBlank { "N/A" })
                        TicketRow("From", booking.from)
                        TicketRow("To", booking.to)
                        TicketRow("Booked On", dateText)



                        Divider()

                        Text(
                          text = "Route Overview",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            cameraPositionState = cameraState
                        ) {

                            fromLatLng?.let {
                                Marker(
                                    state = MarkerState(position = it),
                                    title = booking.from
                                )
                            }

                            toLatLng?.let {
                                Marker(
                                    state = MarkerState(position = it),
                                    title = booking.to
                                )
                            }
                        }
                        Divider()

                        TicketRow("Passengers", passengerText, highlight = true)
                        TicketRow("Total Fare", "₹${booking.fare}", highlight = true)

                        val statusColor = when (displayStatus) {
                            "CONFIRMED" -> Color(0xFF2E7D32)
                            "EXPIRED" -> Color(0xFFC62828)
                            "CANCELLED" -> Color(0xFFC62828)
                            else -> Color.DarkGray
                        }


                        TicketRow(
                            "Status",
                            displayStatus,
                            valueColor = statusColor,
                            highlight = true
                        )



                        if (displayStatus == "CONFIRMED") {
                            Divider()
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                QrCodeBox(data = booking.id)
                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.popBackStack("myBookings", false)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006D8F)
                    )
                ) {
                    Text("Done")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        ticketBitmap?.let {
                            shareBitmap(context, it)
                        }
                    },
                    enabled = ticketBitmap != null, // ⭐ THIS LINE
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (ticketBitmap == null) "Preparing ticket…" else "Share Ticket",
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }
        }

    }
    @Composable
    private fun TicketRow(

        label: String,
        value: String,
        valueColor: Color = Color.Black,
        highlight: Boolean = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.Gray, fontSize = 14.sp)
            Text(
                value,
                fontSize = if (highlight) 16.sp else 14.sp,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                color = valueColor
            )
        }
    }