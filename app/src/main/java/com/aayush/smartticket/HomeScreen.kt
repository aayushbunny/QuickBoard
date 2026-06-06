package com.aayush.smartticket

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    firestoreRepository: FirestoreRepository,
    onLogout: () -> Unit
) {
    val context = LocalContext.current



    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                "Location is needed to find nearby stations",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
     {
         var memory by remember { mutableStateOf<CommuteMemory?>(null) }

         LaunchedEffect(Unit) {
             try {
                 memory = firestoreRepository.getTopCommuteMemory()
             } catch (e: Exception) {
                 e.printStackTrace()
                 memory = null
             }
         }

         memory?.let { commute ->

             Card(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(16.dp)
                     .clickable {
                         navController.navigate("localTrainBooking")
                     }
             ) {
                 Column(Modifier.padding(16.dp)) {

                     Text("Your usual commute", fontWeight = FontWeight.Bold)

                     Text("${commute.from} → ${commute.to}")

                     Text("${commute.ticketType} • ${commute.trainType} • ${commute.adults} Adults")

                     Button(onClick = { navController.navigate("localTrainBooking") }) {
                         Text("Book again")
                     }
                 }
             }
         }


         // 🔵 HERO HEADER (MATCHES LOGIN)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D47A1),
                            Color(0xFF1976D2),
                            Color(0xFF1E88E5)
                        )
                    ),
                    shape = RoundedCornerShape(
                        bottomStart = 36.dp,
                        bottomEnd = 36.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "QuickBoard",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "What would you like to book?",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🚌 BUS CARD
        BookingCard(
            title = "Bus Ticket",
            subtitle = "Book city bus tickets",
            buttonText = "Book Bus Ticket",
            onClick = { navController.navigate("busBooking") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🚆 TRAIN CARD
        BookingCard(
            title = "Train Ticket",
            subtitle = "Book local train tickets",
            buttonText = "Book Train Ticket",
            onClick = { navController.navigate("localTrainBooking") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔘 SECONDARY ACTIONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { navController.navigate("myBookings") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Text("My Bookings")
            }

            OutlinedButton(
                onClick = { navController.navigate("profile") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Text("Profile")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 🚪 LOGOUT
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            Text("Logout", color = Color.White)
        }
    }
}

@Composable
private fun BookingCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                Text(buttonText)
            }
        }
    }
}
