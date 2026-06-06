@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.max
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.net.Uri




@Composable
fun BusBookingScreen(
    navController: NavController,
    prefillRoute: String? = null,
    prefillFrom: String? = null,
    prefillTo: String? = null,
    prefillPassengers: Int? = null,
    prefillBusType: String? = null
)

 {


     var busType by remember { mutableStateOf(prefillBusType ?: "Non-AC") }
     var adults by remember { mutableStateOf(prefillPassengers ?: 1) }
     var children by remember { mutableStateOf(0) }

     var selectedRoute by remember {
         mutableStateOf(prefillRoute?.let { Uri.decode(it) } ?: "")
     }

     var fromStop by remember { mutableStateOf(prefillFrom?.let { Uri.decode(it) } ?: "") }
     var toStop by remember { mutableStateOf(prefillTo?.let { Uri.decode(it) } ?: "") }

// ✅ GET ROUTE DATA
     val routeData = busRoutes[selectedRoute]

// ✅ STOPS LIST
     val availableStops = routeData ?: emptyList()

// ✅ INDEX CALCULATION
     val fromIndex = availableStops.indexOfFirst {
         it.equals(fromStop, ignoreCase = true)
     }

     val toIndex = availableStops.indexOfFirst {
         it.equals(toStop, ignoreCase = true)
     }

// ✅ FARE
     val fare =
         if (fromIndex != -1 && toIndex != -1)
             calculateBusFare(
                 routeStops = availableStops,
                 fromStop = availableStops[fromIndex],
                 toStop = availableStops[toIndex],
                 adults = adults,
                 children = children,
                 busType = busType
             )
         else 0

// ✅ STOPS COUNT
     val stopsTravelled =
         if (fromIndex != -1 && toIndex != -1)
             kotlin.math.abs(fromIndex - toIndex)
         else 0

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestoreRepository = remember { FirestoreRepository() }


     val activity = LocalContext.current as Activity




     LaunchedEffect(prefillRoute) {
         prefillRoute?.let {
             selectedRoute = Uri.decode(it)
         }
     }

     println("Selected Route: $selectedRoute")
     println("Available Routes: ${bestBusRoutes.map { it.busNumber }}")
    var showRoutePicker by remember { mutableStateOf(false) }
    var showFromStopPicker by remember { mutableStateOf(false) }
    var showToStopPicker by remember { mutableStateOf(false) }



     LaunchedEffect(prefillFrom, prefillTo) {
         prefillFrom?.let { fromStop = Uri.decode(it) }
         prefillTo?.let { toStop = Uri.decode(it) }
     }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {

        /* ---------- TOP BAR ---------- */
        TopAppBar(
            title = { Text("Bus Ticket", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)                  // 👈 IMPORTANT
                .verticalScroll(rememberScrollState()) // 👈 SCROLL
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            /* ---------- BUS ROUTE ---------- */
            SectionCard {
                SectionLabel("Bus Number")

                SelectField(
                    value = selectedRoute,
                    placeholder = "Select bus number"
                ) {
                    showRoutePicker = true
                }
            }

            /* ---------- STOPS ---------- */
            SectionCard {
                SectionLabel("Stops")

                SelectField(
                    value = fromStop,
                    placeholder = "From stop"
                ) {
                    if (selectedRoute.isNotBlank()) {
                        showFromStopPicker = true
                    }
                }

                Spacer(Modifier.height(10.dp))

                SelectField(
                    value = toStop,
                    placeholder = "To stop"
                ) {
                    if (selectedRoute.isNotBlank()) {
                        showToStopPicker = true
                    }
                }
            }

            /* ---------- PASSENGERS ---------- */
            SectionCard {
                SectionLabel("Passengers")

                PassengerRow(
                    label = "Adults",
                    count = adults,
                    onMinus = { adults = max(1, adults - 1) },
                    onPlus = { adults++ }
                )

                Spacer(Modifier.height(10.dp))

                PassengerRow(
                    label = "Children",
                    count = children,
                    onMinus = { children = max(0, children - 1) },
                    onPlus = { children++ }
                )
            }

            /* ---------- BUS TYPE ---------- */
            SectionCard {
                SectionLabel("Bus Type")

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BusTypeChip(
                        text = "Non-AC",
                        selected = busType == "Non-AC"
                    ) { busType = "Non-AC" }

                    BusTypeChip(
                        text = "AC",
                        selected = busType == "AC"
                    ) { busType = "AC" }
                }
            }

            Spacer(Modifier.weight(1f))

            /* ---------- PAY ---------- */
            if (fromStop.isNotBlank() && toStop.isNotBlank()) {
                val stopsTravelled = kotlin.math.abs(
                    availableStops.indexOf(fromStop) -
                            availableStops.indexOf(toStop)
                )

                Text(
                    text = "$stopsTravelled stops journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (
                selectedRoute.isNotBlank() &&
                fromStop.isNotBlank() &&
                toStop.isNotBlank() &&
                fromStop != toStop
            ) {
                BusJourneySummaryCard(
                    route = selectedRoute,
                    fromStop = fromStop,
                    toStop = toStop,
                    adults = adults,
                    children = children,
                    stops = stopsTravelled,
                    busType = busType,
                    fare = fare
                )
            }

            BottomPayBar(
                amount = fare,
                enabled = fare > 0
            ) {

                // ✅ STEP 2 START — SAVE BUS STATE
                BusBookingState.isBusBooking = true
                BusBookingState.busNumber = selectedRoute.ifBlank { "Unknown" }
                BusBookingState.route = "$fromStop → $toStop"
                BusBookingState.fromStop = fromStop
                BusBookingState.toStop = toStop
                BusBookingState.adults = adults
                BusBookingState.children = children
                BusBookingState.fare = fare
                BusBookingState.busType = busType   // ✅ ADD HERE

                // ✅ STEP 2 END

                // 🔵 OPEN RAZORPAY
                RazorpayHelper.startPayment(
                    activity = activity,
                    amountInRupees = fare,
                    description = "Bus $selectedRoute • $fromStop → $toStop"
                )
            }

            /* ---------- BOTTOM SHEETS ---------- */

            if (showRoutePicker) {
                BottomSheetPicker(
                    title = "Select Bus Route",
                    items = busRoutes.keys.toList()
                ) { route ->
                    selectedRoute = route
                    fromStop = ""
                    toStop = ""
                    showRoutePicker = false
                }
            }

            if (showFromStopPicker) {
                BottomSheetPicker(
                    title = "Select Boarding Stop",
                    items = availableStops
                ) { stop ->
                    fromStop = stop
                    showFromStopPicker = false
                }
            }

            if (showToStopPicker) {
                BottomSheetPicker(
                    title = "Select Drop Stop",
                    items = availableStops.filter { it != fromStop }
                ) { stop ->
                    toStop = stop
                    showToStopPicker = false
                }
            }
        }
    }
}