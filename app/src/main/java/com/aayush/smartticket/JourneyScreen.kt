package com.aayush.smartticket

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun JourneyScreen(
    fromStation: String,
    onFromStationClick: () -> Unit,

    toStation: String,
    onToStationClick: () -> Unit,

    travelClass: TravelClass,
    onClassChange: (TravelClass) -> Unit,

    adults: Int,
    onAdultPlus: () -> Unit,
    onAdultMinus: () -> Unit,

    child: Int,
    onChildPlus: () -> Unit,
    onChildMinus: () -> Unit
) {

    /* ---------- CLASS PICKER STATE ---------- */

    var showClassSheet by remember { mutableStateOf(false) }

    val classOptions = listOf(
        "Second Class",
        "First Class"
    )

    val classText =
        if (travelClass == TravelClass.SECOND) "Second Class" else "First Class"

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        /* ---------- From / To ---------- */

        StationField(
            label = "From Station",
            value = fromStation,
            onClick = onFromStationClick
        )

        StationField(
            label = "To Station",
            value = toStation,
            enabled = fromStation.isNotBlank(),
            onClick = onToStationClick
        )

        /* ---------- Class (UTS Style) ---------- */

        StationField(
            label = "Class",
            value = classText,
            onClick = { showClassSheet = true }
        )

        /* ---------- Passengers ---------- */

        PassengerStepper(
            label = "Adults",
            count = adults,
            onPlus = onAdultPlus,
            onMinus = onAdultMinus
        )

        PassengerStepper(
            label = "Child",
            count = child,
            onPlus = onChildPlus,
            onMinus = onChildMinus
        )
    }
    /* ---------- CLASS BOTTOM SHEET ---------- */

    if (showClassSheet) {
        BottomSheetPicker(
            title = "Select Class",
            items = classOptions
        ) { selected ->

            onClassChange(
                if (selected == "First Class") TravelClass.FIRST
                else TravelClass.SECOND
            )

            showClassSheet = false
        }
    }
}