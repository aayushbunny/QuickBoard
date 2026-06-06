@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.aayush.smartticket.location.StationGeoFence
import android.os.Looper
import android.os.Handler
import android.location.Location
import androidx.compose.runtime.DisposableEffect
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.Intent
import android.provider.Settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import android.widget.Toast
import android.content.Context
import androidx.navigation.NavController


/* ---------- COLORS ---------- */






enum class TicketMode { JOURNEY, PLATFORM }


/* ---------- FARE ---------- */

private fun utsSecondClassFare(stations: Int) = when {
    stations <= 2 -> 5
    stations <= 5 -> 10
    stations <= 10 -> 15
    stations <= 15 -> 20
    stations <= 20 -> 25
    stations <= 30 -> 30
    stations <= 40 -> 35
    else -> 40
}


val westernLine = StationData.stations.filter { it.line == "Western" }
val centralLine = StationData.stations.filter { it.line == "Central" }
val harbourLine = StationData.stations.filter { it.line == "Harbour" }
fun distance(
    stations: List<Station>,
    from: Station,
    to: Station
): Int {
    val i = stations.indexOfFirst { it.name == from.name }
    val j = stations.indexOfFirst { it.name == to.name }
    return kotlin.math.abs(i - j)
}




fun calculateInterchangeDistance(
    from: Station,
    to: Station
): Int {

    // SAME LINE
    if (from.line == to.line) {
        val lineStations = when (from.line) {
            "Western" -> westernLine
            "Central" -> centralLine
            "Harbour" -> harbourLine
            else -> StationData.stations
        }
        return distance(lineStations, from, to)
    }

    val dadarW = StationData.stations.find {
        it.name == "Dadar" && it.line == "Western"
    }!!

    val dadarC = StationData.stations.find {
        it.name == "Dadar" && it.line == "Central"
    }!!

    val kurla = StationData.stations.find {
        it.name == "Kurla"
    }!!
    return when {

        // WESTERN → CENTRAL
        from.line == "Western" && to.line == "Central" -> {
            distance(westernLine, from, dadarW) +
                    distance(centralLine, dadarC, to)
        }

        // CENTRAL → WESTERN
        from.line == "Central" && to.line == "Western" -> {
            distance(centralLine, from, dadarC) +
                    distance(westernLine, dadarW, to)
        }

        // CENTRAL → HARBOUR
        from.line == "Central" && to.line == "Harbour" -> {
            distance(centralLine, from, kurla) +
                    distance(harbourLine, kurla, to)
        }

        // HARBOUR → CENTRAL
        from.line == "Harbour" && to.line == "Central" -> {
            distance(harbourLine, from, kurla) +
                    distance(centralLine, kurla, to)
        }

        else -> 0
    }
}



private fun calculateUTSFare(
    stations: Int,
    adults: Int,
    children: Int,
    travelClass: TravelClass,
    trainType: String,
    ticketType: String,
    concession: ConcessionType
): Int {
    if (stations <= 0 || adults <= 0) return 0

    var base = utsSecondClassFare(stations)

    if (travelClass == TravelClass.FIRST) base *= 3
    if (trainType == "AC EMU TRAIN") base = (base * 1.3).toInt()

    var total = adults * base + children * (base / 2)

    if (ticketType == "RETURN") total *= 2

    // ✅ APPLY CONCESSION DISCOUNT
    if (concession != ConcessionType.NONE) {
        total -= (total * concession.discountPercent / 100)
    }

    return total.coerceAtLeast(5)
}

fun distance(a: Station, b: Station, stations: List<Station>): Int {
    val i = stations.indexOfFirst { it.name == a.name }
    val j = stations.indexOfFirst { it.name == b.name }
    return kotlin.math.abs(i - j)
}






/* ---------- MAIN SCREEN ---------- */

@Composable
fun LocalTrainBookingScreen(
    navController: NavController? = null,
    prefillFrom: String? = null,
    prefillTo: String? = null,
    prefillPassengers: Int? = null,
    prefillTicketType: String? = null,
    prefillTrainType: String? = null
) {
    var travelClass by rememberSaveable { mutableStateOf<TravelClass?>(null) }

    var locationChecked by remember { mutableStateOf(false) }


    var showClassSheet by remember { mutableStateOf(false) }
    var showTrainTypeSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }

    var from by remember {
        mutableStateOf(StationData.stations.find { it.name== prefillFrom })
    }

    var to by remember {
        mutableStateOf(StationData.stations.find { it.name == prefillTo })
    }

    var adults by rememberSaveable {
        mutableStateOf(prefillPassengers ?: 1)
    }

    var ticketType by rememberSaveable {
        mutableStateOf(prefillTicketType ?: "JOURNEY")
    }

    var trainType by rememberSaveable {
        mutableStateOf(prefillTrainType ?: "")
    }


    fun distanceInSameLine(
        from: Station,
        to: Station,
        stations: List<Station>
    ): Int {
        val i = stations.indexOfFirst { it.name== from.name}
        val j = stations.indexOfFirst { it.name== to.name}
        return kotlin.math.abs(i - j)
    }

    var blockReason by remember { mutableStateOf(StationGeoFence.BlockReason.NONE) }

    val scope = rememberCoroutineScope()
    val firestoreRepository = remember { FirestoreRepository() }
    val fusedClient: com.google.android.gms.location.FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var nearStation by remember { mutableStateOf(false) }
    var locationAvailable by remember { mutableStateOf(false) }

    var mode by rememberSaveable { mutableStateOf(TicketMode.JOURNEY) }



    var persons by rememberSaveable { mutableStateOf(1) }

    var platformStation by remember { mutableStateOf<Station?>(null) }




    var availConcession by rememberSaveable { mutableStateOf(false) }
    var selectedConcession by rememberSaveable { mutableStateOf(ConcessionType.NONE) }

    var children by rememberSaveable { mutableStateOf(0) }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
    fun updateLocation() {

        // 1. Permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationAvailable = false
            locationChecked = true
            nearStation = false
            blockReason = StationGeoFence.BlockReason.NONE
            return
        }

        // 2. Location service ON?
        if (!isLocationServiceEnabled(context)) {
            locationAvailable = false
            locationChecked = true
            nearStation = false
            blockReason = StationGeoFence.BlockReason.NONE
            return
        }

        // 3. GPS provider ON?
        if (!isGpsEnabled(context)) {
            locationAvailable = false
            locationChecked = true
            nearStation = false
            blockReason = StationGeoFence.BlockReason.NONE
            return
        }

        // 4. Get location
        fusedClient.lastLocation.addOnSuccessListener { location ->

            locationChecked = true

            if (location != null) {
                locationAvailable = true
                userLocation = location.latitude to location.longitude

                val reason = StationGeoFence.checkUserLocation(
                    location.latitude,
                    location.longitude
                )

                blockReason = reason
                nearStation = reason != StationGeoFence.BlockReason.NONE

            } else {
                // GPS ON but no cached location
                locationAvailable = true
                nearStation = false
                blockReason = StationGeoFence.BlockReason.NONE
            }
        }
    }



    LaunchedEffect(mode) {
        if (mode == TicketMode.PLATFORM) {
            persons = 1
            adults = 1
            children = 0
            travelClass = TravelClass.SECOND
            trainType = "ORDINARY"
            ticketType = "JOURNEY"
        }
    }

    DisposableEffect(Unit) {

        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                updateLocation()
                handler.postDelayed(this, 2_000) // ✅ every 2 seconds
            }
        }

        handler.post(runnable)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }


    val isAcEmuSelected = trainType == "AC EMU TRAIN"

    val stationCount = if (from != null && to != null) {
        calculateInterchangeDistance(from!!, to!!)
    } else 0

    val fare =
        if (mode == TicketMode.PLATFORM) {
            if (platformStation != null) persons * 10 else 0
        } else {
            calculateUTSFare(
                stations = stationCount,
                adults = adults,
                children = children,
                travelClass = travelClass ?: TravelClass.SECOND,
                trainType = trainType,
                ticketType = ticketType,
                        concession = if (availConcession) selectedConcession else ConcessionType.NONE
            )
        }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    )
    {

         if (locationChecked && !locationAvailable) {


            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Location is turned off",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        "Please enable GPS to book train tickets.",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Button(
                        onClick = { openLocationSettings() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Enable Location")
                    }
                }
            }
        }


        /* MODE */
        Surface(shape = RoundedCornerShape(50), color = SoftBlue) {
            Row(Modifier.padding(4.dp)) {
                ModeChip("JOURNEY", mode == TicketMode.JOURNEY) {
                    mode = TicketMode.JOURNEY
                }
                ModeChip("PLATFORM", mode == TicketMode.PLATFORM) {
                    mode = TicketMode.PLATFORM
                }
            }
        }


        Spacer(Modifier.height(8.dp))

        SectionCard {
            SectionLabel("Stations")

            if (mode == TicketMode.JOURNEY) {

                SelectField(
                    value = from?.let { "${it.name} (${it.line})"} ?: "",
                    placeholder = "From Station"
                )
                { showFromPicker = true }

                Spacer(Modifier.height(8.dp))

                SelectField(
                    value = to?.let { "${it.name} (${it.line})" } ?: "",
                    placeholder = "To Station"
                ) { showToPicker = true }

            } else {
                // PLATFORM MODE
                SelectField(
                    value = platformStation?.let { "${it.name} (${it.line})" } ?: "",
                    placeholder = "Station Name"
                )
                { showFromPicker = true }
            }
        }

        Spacer(Modifier.height(4.dp))

        /* PASSENGERS */
        SectionCard {


            if (mode == TicketMode.PLATFORM) {

                PassengerRow(
                    label = "Persons",
                    count = persons,
                    onMinus = { if (persons > 1) persons-- },
                    onPlus = { if (persons < 4) persons++ }
                )

            } else {

                PassengerRow(
                    label = "Adults",
                    count = adults,
                    onMinus = { if (adults > 1) adults-- },
                    onPlus = { if (adults < 4) adults++ }
                )

                Spacer(Modifier.height(4.dp))

                PassengerRow(
                    label = "Children",
                    count = children,
                    onMinus = { if (children > 0) children-- },
                    onPlus = { if (children < 4) children++ }
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        if (mode == TicketMode.JOURNEY) {
            SectionCard {
                SectionLabel("Concession")
                ConcessionSection(
                    enabled = availConcession,
                    onEnabledChange = {
                        availConcession = it
                        if (!it) {
                            selectedConcession = ConcessionType.NONE
                            LocalTrainBookingState.concession = null
                        }
                    },
                    selected = selectedConcession,
                    onSelect = {
                        selectedConcession = it
                        LocalTrainBookingState.concession = it.label
                    }
                )


            }
        }

            Spacer(Modifier.height(4.dp))



        if (mode == TicketMode.JOURNEY) {

            SectionCard {
                SectionLabel("Ticket Type")

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BusTypeChip(
                        text = "Journey",
                        selected = ticketType == "JOURNEY"
                    ) { ticketType = "JOURNEY" }

                    BusTypeChip(
                        text = "Return",
                        selected = ticketType == "RETURN"
                    ) { ticketType = "RETURN" }
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionCard {
                TicketDropdownCard(
                    title = "Class",
                    value = travelClass?.let {
                        if (it == TravelClass.FIRST) "First" else "Second"
                    } ?: "",
                    placeholder = "Select Class",
                    onClick = { showClassSheet = true }
                )
            }


            Spacer(Modifier.height(4.dp))

            var trainExpanded by remember { mutableStateOf(false) }
            SectionCard {
                TicketDropdownCard(
                    title = "Train Type",
                    value = when (trainType) {
                        "ORDINARY" -> "Ordinary"
                        "AC EMU TRAIN" -> "AC EMU"
                        else -> ""
                    },
                    placeholder = "Select Train Type",
                    enabled = true,
                    helperText =
                        if (travelClass == TravelClass.SECOND && trainType.isNotBlank())
                            ""
                        else null,
                    onClick = { showTrainTypeSheet = true }
                )
            }
            Spacer(Modifier.height(8.dp))

            /* ---------- SUMMARY ---------- */

            @Composable
            fun JourneySummaryCard(
                mode: TicketMode,
                from: String,
                to: String,
                travelClass: String,
                trainType: String,
                ticketType: String,
                adults: Int,
                children: Int,
                fare: Int,
                concession: String?
            )
            {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)

                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Journey Summary", fontWeight = FontWeight.Bold)

                        SummaryRow("Mode", mode.name)
                        SummaryRow("From", from.ifBlank { "-" })
                        SummaryRow("To", to.ifBlank { "-" })
                        SummaryRow("Class", travelClass)
                        SummaryRow("Train Type", trainType)
                        SummaryRow("Ticket Type", ticketType)
                        SummaryRow("Adults", adults.toString())
                        SummaryRow("Children", children.toString())
                        if (!concession.isNullOrBlank()) {
                            SummaryRow("Concession", concession)
                        }


                        Divider()

                        SummaryRow("Fare", "₹$fare", bold = true)

                    }
                }

            }
            Spacer(Modifier.height(8.dp))
            if (mode == TicketMode.JOURNEY && from != null && to != null && from != to) {

                JourneySummaryCard(
                    mode = mode,
                    from = "${from!!.name} (${from!!.line})",
                    to = "${to!!.name} (${to!!.line})",
                    travelClass = travelClass?.name ?: "-",
                    trainType = trainType,
                    ticketType = ticketType,
                    adults = adults,
                    children = children,
                    fare = fare,
                            concession = if (availConcession) selectedConcession.label else null
                )

            }


        }

        if (!locationAvailable) {
            Text(
                "Location is turned off. Please enable GPS to book tickets.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        else if (nearStation) {
            Text(
                when (blockReason) {
                    StationGeoFence.BlockReason.PLATFORM ->
                        "You are on the platform. Booking is not allowed."
                    StationGeoFence.BlockReason.STATION ->
                        "You are inside station premises. Move outside to book."
                    else -> ""
                },
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }



        /* 🔽 PAY BAR — COMMON FOR BOTH MODES */
        BottomPayBar(
            amount = fare,
            enabled = locationAvailable && !nearStation && (
                    if (mode == TicketMode.JOURNEY) {
                        from != null && to != null && from != to && fare > 0
                    } else {
                        platformStation != null && persons > 0
                    }
                    )
        )

         {
             if (availConcession && selectedConcession == ConcessionType.NONE) {
                 Toast.makeText(context, "Please select concession type", Toast.LENGTH_SHORT).show()
                 return@BottomPayBar
             }

            if (nearStation) {
                Toast.makeText(
                    context,
                    "Move at least 40 meters away from the station to book ticket",
                    Toast.LENGTH_LONG
                ).show()
                return@BottomPayBar
            }

            LocalTrainBookingState.mode = mode.name
            if (trainType.isBlank()) trainType = "ORDINARY"
            if (travelClass == null) travelClass = TravelClass.SECOND

            if (mode == TicketMode.PLATFORM) {
                LocalTrainBookingState.from = platformStation!!.name
                LocalTrainBookingState.to = platformStation!!.name

                LocalTrainBookingState.stationName = platformStation!!.name
                LocalTrainBookingState.adults = persons
                LocalTrainBookingState.children = 0
                LocalTrainBookingState.ticketType = "PLATFORM"
                LocalTrainBookingState.trainType = "ORDINARY"
                LocalTrainBookingState.ticketClass = "SECOND"
            } else {
                LocalTrainBookingState.from = from!!.name
                LocalTrainBookingState.to = to!!.name

                LocalTrainBookingState.adults = adults
                LocalTrainBookingState.children = children
                LocalTrainBookingState.ticketType = ticketType
                LocalTrainBookingState.trainType = trainType
                LocalTrainBookingState.ticketClass = travelClass?.name ?: "-"
            }

            LocalTrainBookingState.fare = fare

            if (activity == null) {
                Toast.makeText(
                    context,
                    "Unable to start payment. Please reopen app.",
                    Toast.LENGTH_LONG
                ).show()
                return@BottomPayBar
            }

            try {
                RazorpayHelper.startPayment(
                    activity = activity,
                    amountInRupees = fare*100,   // ✅ ONLY fare
                    description =
                        if (mode == TicketMode.PLATFORM)
                            "Platform Ticket - ${platformStation!!.name}"
                        else
                            "${from!!.name} → ${to!!.name}"
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Payment error: ${e.message}", Toast.LENGTH_LONG).show()
            }


        }
            if (showFromPicker) {
                StationPickerBottomSheet(
                    title = "Select Station",
                    stations = StationData.stations,
                    onSelect = {
                        if (mode == TicketMode.JOURNEY) {
                            from = it
                        } else {
                            platformStation = it
                        }
                        showFromPicker = false
                    },
                    onDismiss = { showFromPicker = false }
                )
            }


            if (showToPicker) {
                StationPickerBottomSheet(
                    title = "Select To Station",
                    stations = StationData.stations,
                    onSelect = {
                        to = it
                        showToPicker = false
                    },
                    onDismiss = { showToPicker = false }
                )
            }
        }
        // Train Type Bottom Sheet
        if (showTrainTypeSheet) {
            TrainTypeBottomSheet(
                travelClass = travelClass ?: TravelClass.SECOND,
                selected = trainType,
                onSelect = {
                    trainType = it
                    if (it == "AC EMU TRAIN") {
                        travelClass = TravelClass.FIRST
                    }
                },
                onDismiss = {
                    showTrainTypeSheet = false
                }
            )
        }

// Class Bottom Sheet
        if (showClassSheet) {
            ClassBottomSheet(
                selected = travelClass ?: TravelClass.SECOND,
                onSelect = {
                    travelClass = it
                    if (it == TravelClass.SECOND) {
                        trainType = "ORDINARY"
                    }
                },
                onDismiss = {
                    showClassSheet = false
                }
            )
        }


}
fun isLocationServiceEnabled(context: android.content.Context): Boolean {
    val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE)
            as android.location.LocationManager

    return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
}
fun isGpsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
}


