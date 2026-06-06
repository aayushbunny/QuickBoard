package com.aayush.smartticket

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import androidx.compose.foundation.clickable






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController)
 {

    val repo = remember { AdminRepository() }
    val scope = rememberCoroutineScope()

    var totalUsers by remember { mutableStateOf(0) }
    var totalBookings by remember { mutableStateOf(0) }
    var todayRevenue by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(true) }
    var recentBookings by remember { mutableStateOf<List<AdminBooking>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                recentBookings = repo.getRecentBookings()
                totalUsers = repo.getTotalUsers()
                totalBookings = repo.getTotalBookings()
                todayRevenue = repo.getTodayRevenueAmount()

            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recent Bookings",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(recentBookings) { booking ->
                    BookingRow(booking)
                }
            }




            if (loading) {
                CircularProgressIndicator()
                return@Column
            }

            DashboardCard("Total Users", totalUsers.toString())
            DashboardCard("Total Bookings", totalBookings.toString())
            DashboardCard( "Today's Revenue", value = "₹ %.2f".format(todayRevenue),
                onClick = {
                    navController.navigate("admin_revenue")
                }
            )

        }
    }
}


@Composable
fun DashboardCard(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null)
                    Modifier.clickable { onClick() }
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}





@Composable
fun BookingRow(booking: AdminBooking) {

    val scope = rememberCoroutineScope()
    val actionRepo = remember { AdminTicketActions() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("${booking.from} → ${booking.to}", fontWeight = FontWeight.Bold)
                    Text("User: ${booking.userId}", style = MaterialTheme.typography.bodySmall)
                }
                Text("₹${booking.fare}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(
                    onClick = {
                        scope.launch {
                            actionRepo.cancelBooking(booking.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel Ticket")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            actionRepo.blockUser(booking.userId)
                        }
                    }
                ) {
                    Text("Block User")
                }
            }
        }
    }
}
