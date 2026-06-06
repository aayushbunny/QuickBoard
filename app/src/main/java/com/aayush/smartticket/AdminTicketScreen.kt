    @file:OptIn(ExperimentalMaterial3Api::class)

    package com.aayush.smartticket

    import androidx.lifecycle.viewModelScope
    import kotlinx.coroutines.launch
    import androidx.compose.ui.unit.sp
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.platform.LocalClipboardManager
    import androidx.compose.ui.text.AnnotatedString
    import androidx.compose.material.icons.filled.ContentCopy
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
    import androidx.compose.material.icons.filled.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavController
    import java.text.SimpleDateFormat
    import java.util.*
    import androidx.compose.ui.platform.LocalContext
    import android.widget.Toast

    private fun displayConcession(raw: String?): String {
        return when (raw?.uppercase()) {
            "STUDENT" -> "Student"
            "SENIOR", "SENIOR_CITIZEN" -> "Senior Citizen"
            "HANDICAP", "DIVYANG" -> "Divyang"
            null, "", "NONE" -> "No Concession"
            else -> raw
        }
    }
    private fun displayTicketType(raw: String): String {
        return when (raw.uppercase()) {
            "JOURNEY" -> "Journey"
            "RETURN" -> "Return"
            else -> raw
        }
    }

    private fun displayTrainType(raw: String): String {
        return when (raw.uppercase()) {
            "ORDINARY" -> "Ordinary"
            "AC", "AC_EMU", "ACEMU" -> "AC EMU Train"
            else -> raw
        }
    }

    private fun adminDashboardStatus(booking: Booking): String {

        // 1️⃣ Cancelled has top priority
        if (booking.status.equals("CANCELLED", ignoreCase = true)) {
            return "CANCELLED"
        }

        val now = System.currentTimeMillis()
        val createdAt = booking.createdAt

        val validityMillis = when (booking.mode.uppercase()) {
            "PLATFORM" -> 2 * 60 * 60 * 1000L // 2 hours
            "BUS" -> 4 * 60 * 60 * 1000L      // 4 hours
            else -> Long.MAX_VALUE           // TRAIN does not auto-expire
        }

        return if (now > createdAt + validityMillis) {
            "EXPIRED"
        } else {
            "CONFIRMED"
        }
    }
    private fun displayTicketClass(raw: String): String {
        return when (raw.uppercase()) {
            "SECOND", "2ND" -> "Second"
            "FIRST", "1ST" -> "First"
            "AC" -> "AC"
            else -> raw
        }
    }
    @Composable
    fun AdminTicketScreen(
        navController: NavController,
        bookingId: String,
        viewModel: AdminTicketActionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    ) {
        var selectedBooking by remember { mutableStateOf<Booking?>(null) }
        val context = LocalContext.current
        var showCancelDialog by remember { mutableStateOf(false) }

        val booking by produceState<Booking?>(initialValue = null, bookingId) {
            value = viewModel.getBookingById(bookingId)
        }
        var showDeleteDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Admin Ticket Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                )
            },
            containerColor = Color(0xFFF4F6FA)
        ) { padding ->

            booking?.let { fullBooking ->
                selectedBooking = fullBooking

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { AdminStatusCard(fullBooking) }
                    item { AdminJourneyCard(fullBooking) }
                    item { AdminUserCard(fullBooking) }
                    item { AdminSystemCard(fullBooking) }

                    item {
                        Spacer(Modifier.height(8.dp))

                        if (adminDashboardStatus(fullBooking) == "CONFIRMED") {
                            Button(
                                onClick = { showCancelDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel Booking")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Delete Booking Permanently")
                        }
                    }
                }
            }

            if (showCancelDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { Text("Cancel Booking") },
                    text = { Text("This ticket will be cancelled permanently.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showCancelDialog = false

                            selectedBooking?.let {
                                viewModel.cancelIfActive(
                                    booking = it,
                                    onSuccess = { navController.popBackStack() },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Booking?") },
                    text = { Text("This will permanently remove this booking. This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false

                            selectedBooking?.let {
                                viewModel.cancelBooking(
                                    it.id,
                                    onSuccess = { navController.popBackStack() },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                                )
                            }
                        }) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }


        }
    }


    @Composable
    private fun StatusOverviewCard(booking: Booking) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text("Status", color = Color.Gray, fontSize = MaterialTheme.typography.labelSmall.fontSize)

                    Spacer(Modifier.height(4.dp))

                    Surface(
                        color = if (booking.status == "CONFIRMED") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            booking.status,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = if (booking.status == "CONFIRMED") Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                Column(horizontalAlignment = Alignment.End) {
                    Text("Fare", color = Color.Gray)
                    Text(
                        "₹${booking.fare}",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }


    @Composable
    private fun UserDashboardCard(booking: Booking) {
        DashboardCard(title = "User", icon = Icons.Default.Person) {
            InfoRow("User ID", booking.userId)
        }
    }


    @Composable
    private fun SystemDashboardCard(booking: Booking) {
        val date = remember(booking.createdAt) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(booking.createdAt))
        }

        DashboardCard(title = "System", icon = Icons.Default.Settings) {
            InfoRow("Booking ID", booking.id)
            InfoRow("Created", date)
        }
    }
    @Composable
    private fun DashboardCard(
        title: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))

                content()
            }
        }
    }
    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
    @Composable
    private fun AdminStatusCard(booking: Booking) {

        val status = adminDashboardStatus(booking)

        Card(shape = RoundedCornerShape(20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Status", color = Color.Gray)

                    Text(
                        text = status,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "CONFIRMED" -> Color(0xFF2E7D32)
                            "EXPIRED" -> Color(0xFFD32F2F)
                            "CANCELLED" -> Color.Gray
                            else -> Color.Black
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Fare", color = Color.Gray)
                    Text("₹${booking.fare}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }


    @Composable
    private fun AdminJourneyCard(booking: Booking) {
        AdminDashboardCard("Journey") {

            AdminInfoRow("Mode", booking.mode)

            if (booking.mode == "BUS") {
                AdminInfoRow(
                    "Ticket Type",
                    if (booking.journeyType == JourneyType.RETURN)
                        "Return Journey"
                    else
                        "Single Journey"
                )


                if (booking.busNumber.isNotBlank()) {
                    AdminInfoRow("Bus Route", booking.busNumber)
                }

                if (booking.busType.isNotBlank()) {
                    AdminInfoRow("Bus Type", booking.busType)   // AC / NON-AC
                }

                AdminInfoRow("From", stationDisplay(booking.from))
                AdminInfoRow("To", stationDisplay(booking.to))

            } else if (booking.mode == "TRAIN") {

                // ✅ ADD THESE TWO LINES
                AdminInfoRow(
                    "Ticket Type",
                    displayTicketType(booking.ticketType)
                )

                AdminInfoRow(
                    "Train Type",
                    displayTrainType(booking.trainType)
                )

                AdminInfoRow(
                    "Class",
                    displayTicketClass(booking.ticketClass)
                )

                val concessionText = displayConcession(booking.concession)

                if (concessionText != "No Concession") {
                    AdminInfoRow(
                        "Concession",
                        concessionText
                    )
                }
                AdminInfoRow("From", stationDisplay(booking.from))
                AdminInfoRow("To", stationDisplay(booking.to))
            } else {

                AdminInfoRow("Station", stationDisplay(booking.station))
            }

            AdminInfoRow("Adults", booking.adults.toString())
            AdminInfoRow("Children", booking.children.toString())
        }
    }


            @Composable
    private fun AdminSystemCard(booking: Booking) {
        val date = remember(booking.createdAt) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(booking.createdAt))
        }

        AdminDashboardCard("System") {
            AdminInfoRow("Booking ID", booking.id)
            AdminInfoRow("Created", date)
        }
    }


    @Composable
    private fun AdminDashboardCard(
        title: String,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }


    @Composable
    private fun AdminInfoRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
    @Composable
    private fun AdminUserCard(booking: Booking)
     {
        val clipboard = LocalClipboardManager.current

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECEAF0))
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "User",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "User ID",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = booking.userId,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(booking.userId))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy User ID"
                        )
                    }
                }
            }
        }
    }

