@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp

// ---------------- MODEL ----------------


// ---------------- SCREEN ----------------

@Composable
fun AdminSecurityScreen(navController: NavController) {

    val logs = remember {
        listOf(
            SecurityLog(
                id = "1",
                email = "admin@gmail.com",
                success = true,
                device = "Android",
                timestamp = System.currentTimeMillis(),
                sessionId = "sess1"
            ),
            SecurityLog(
                id = "2",
                email = "test@gmail.com",
                success = false,
                device = "Chrome",
                timestamp = System.currentTimeMillis(),
                sessionId = "sess2"
            ),
            SecurityLog(
                id = "3",
                email = "user@gmail.com",
                success = true,
                device = "iPhone",
                timestamp = System.currentTimeMillis(),
                sessionId = "sess3"
            )
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Center") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(logs) { log ->
                SecurityLogCard(log, logs)
            }

        }
    }
}

// ---------------- CARD ----------------

@Composable
fun SecurityLogCard(log: SecurityLog, allLogs: List<SecurityLog>)
 {
     val loginCount = remember(allLogs) {
         countLogins(allLogs, log.email)
     }

    val timeText = remember(log.timestamp) {
        java.text.SimpleDateFormat("dd MMM, HH:mm")
            .format(java.util.Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2F2F34)
        )
    ) {
        Column(Modifier.padding(14.dp)) {

            Text(
                log.email,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "${deviceIcon(log.device)} Device: ${log.device}",
                color = Color.LightGray
            )

            Text(
                "🕒 $timeText",
                color = Color.LightGray
            )
            Text(
                "🔐 Logins: $loginCount times",
                color = Color(0xFFB0BEC5),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(8.dp))

            StatusBadge(log.success)
        }
    }
}

fun deviceIcon(device: String): String =
    when {
        device.contains("android", true) -> "🤖"
        device.contains("iphone", true) -> "📱"
        device.contains("chrome", true) -> "💻"
        else -> "🖥️"
    }

@Composable
fun StatusBadge(success: Boolean) {
    val bg = if (success) Color(0xFF1B5E20) else Color(0xFF7F1D1D)
    val fg = if (success) Color(0xFF86EFAC) else Color(0xFFFCA5A5)

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (success) "SUCCESS" else "FAILED",
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun countLogins(logs: List<SecurityLog>, email: String): Int {
    return logs.count { it.email == email && it.success }
}

