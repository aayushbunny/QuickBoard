@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect


@Composable
fun AdminRevenueScreen(navController: NavController) {

    val repo = remember { AdminRepository() }

    var todayRevenue by remember { mutableStateOf(0.0) }
    var monthlyRevenue by remember { mutableStateOf(0.0) }
    var totalRevenue by remember { mutableStateOf(0.0) }
    var totalBookings by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            todayRevenue = repo.getTodayRevenueAmount()
            monthlyRevenue = repo.getMonthlyRevenue()
            totalRevenue = repo.getTotalRevenue()
            totalBookings = repo.getTotalBookings()
        } catch (e: Exception) {
            e.printStackTrace()
            todayRevenue = 0.0
            monthlyRevenue = 0.0
            totalRevenue = 0.0
            totalBookings = 0
        } finally {
            loading = false
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Revenue Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Revenue Analytics", style = MaterialTheme.typography.titleLarge)

            if (loading) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator()
                return@Column
            }

            DashboardCard("Today's Revenue", "₹ %.2f".format(todayRevenue))
            DashboardCard("This Month", "₹ %.2f".format(monthlyRevenue))
            DashboardCard("Total Revenue", "₹ %.2f".format(totalRevenue))
            DashboardCard("Total Bookings", totalBookings.toString())

            val avg = if (totalBookings == 0) 0.0 else totalRevenue / totalBookings
            DashboardCard("Avg Ticket Value", "₹ %.2f".format(avg))
        }
    }
}
