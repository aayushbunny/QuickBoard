package com.aayush.smartticket
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {

    val repo = remember { AdminRepository() }

    var activeUsers by remember { mutableStateOf(0) }
    var todayBookings by remember { mutableStateOf(0) }
    var todayRevenue by remember { mutableStateOf(0.0) }
    var failedPayments by remember { mutableStateOf(0) }
    var loadingStats by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            activeUsers = repo.getActiveUsersToday()
            todayBookings = repo.getTodayBookingsCount()
            todayRevenue = repo.getTodayRevenueAmount()
            failedPayments = repo.getFailedPaymentsToday()
        } finally {
            loadingStats = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Admin Control Center") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Welcome, Admin", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("Monitor bookings, manage users and control the system.")
                }
            }

            if (loadingStats) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LiveStatsRow(activeUsers, todayBookings, todayRevenue, failedPayments)
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminActionCard("Users", "Manage", Icons.Default.Person) {
                        navController.navigate(Routes.ADMIN_USERS)
                    }

                    AdminActionCard("Bookings", "View all", Icons.Default.List) {
                        navController.navigate(Routes.ADMIN_BOOKINGS)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminActionCard("Revenue", "Stats", Icons.Default.BarChart) {
                        navController.navigate(Routes.ADMIN_REVENUE)
                    }

                    AdminActionCard("Security", "Logins", Icons.Default.Security) {
                        navController.navigate(Routes.ADMIN_SECURITY)
                    }
                }
            }


            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun RowScope.AdminActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun LiveStatsRow(active: Int, bookings: Int, revenue: Double, failed: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatChip("👥", active.toString(), "Users")
            StatChip("🎫", bookings.toString(), "Bookings")
            StatChip("💰", "₹${"%.0f".format(revenue)}", "Revenue")
            StatChip("⚠️", failed.toString(), "Failed")
        }
    }
}

@Composable
fun StatChip(icon: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Text(icon, fontSize = 18.sp)
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
