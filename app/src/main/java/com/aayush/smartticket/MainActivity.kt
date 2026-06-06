    package com.aayush.smartticket

    import java.util.UUID
    import android.os.Bundle
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.systemBarsPadding
    import androidx.compose.material3.Surface
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.core.view.WindowCompat
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.NavHostController
    import androidx.navigation.NavType
    import androidx.navigation.compose.*
    import androidx.navigation.navArgument
    import com.aayush.smartticket.ui.theme.SmartTicketTheme
    import com.google.firebase.auth.FirebaseAuth
    import com.razorpay.Checkout
    import com.razorpay.PaymentResultListener
    import kotlinx.coroutines.launch
    import android.Manifest
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.core.content.ContextCompat
    import android.content.pm.PackageManager
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.ui.platform.LocalContext
    import android.net.Uri


    class MainActivity : ComponentActivity(), PaymentResultListener {

        private val firestoreRepository = FirestoreRepository()
        private val authRepository = AuthRepository()

        private lateinit var navController: NavHostController
        private var pendingBookingId by mutableStateOf<String?>(null)


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            WindowCompat.setDecorFitsSystemWindows(window, false)
            Checkout.preload(applicationContext)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            setTheme(R.style.Theme_SmartTicketApp)

            setContent {
                SmartTicketTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        navController = rememberNavController()
                        val isLoggedIn = authRepository.isUserLoggedIn()
                        var isAdmin by remember { mutableStateOf(false) }
                        val firestoreRepository = remember { FirestoreRepository() }

                        val context = LocalContext.current



                        // 🔁 Navigate AFTER booking saved
                        LaunchedEffect(pendingBookingId) {
                            pendingBookingId?.let { id ->
                                navController.navigate("ticket_loader/$id")
                                {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                                pendingBookingId = null
                            }
                        }


                        NavHost(
                            navController = navController,
                            startDestination = Routes.SPLASH


                        ) {
                            composable(Routes.ADMIN_SECURITY) {
                                AdminSecurityScreen(navController)
                            }
                            composable(Routes.SPLASH) {
                                SplashScreen(
                                    navController = navController,
                                    isLoggedIn = isLoggedIn,
                                    isAdmin = isAdmin
                                )
                            }


                            composable("login") {
                                LoginScreen(navController, authRepository)
                            }
                            composable(Routes.ADMIN_HOME) {
                                AdminHomeScreen(navController)
                            }

                            composable(Routes.ADMIN_USERS) {
                                AdminUsersScreen(navController)
                            }

                            composable(Routes.ADMIN_BOOKINGS) {
                                AdminBookingsScreen(navController, firestoreRepository)
                            }

                            composable(Routes.ADMIN_REVENUE) {
                                AdminRevenueScreen(navController)
                            }
                            composable(
                                route = "rebook_train/{from}/{to}/{adults}/{ticketType}/{trainType}"
                            ) { backStackEntry ->

                                LocalTrainBookingScreen(
                                    navController = navController,
                                    prefillFrom = backStackEntry.arguments?.getString("from")?.let { Uri.decode(it) },
                                    prefillTo = backStackEntry.arguments?.getString("to")?.let { Uri.decode(it) },
                                    prefillPassengers = backStackEntry.arguments?.getString("adults")?.toIntOrNull(),
                                    prefillTicketType = backStackEntry.arguments?.getString("ticketType")?.let { Uri.decode(it) },
                                    prefillTrainType = backStackEntry.arguments?.getString("trainType")?.let { Uri.decode(it) }
                                )
                            }


                            composable("rebook_bus/{route}/{from}/{to}/{adults}/{busType}") { backStackEntry ->

                                BusBookingScreen(
                                    navController = navController,
                                    prefillRoute = backStackEntry.arguments?.getString("route"),
                                    prefillFrom = backStackEntry.arguments?.getString("from"),
                                    prefillTo = backStackEntry.arguments?.getString("to"),
                                    prefillPassengers = backStackEntry.arguments?.getString("adults")?.toIntOrNull(),
                                    prefillBusType = backStackEntry.arguments?.getString("busType")
                                )
                            }



                            composable("signup") {
                                SignupScreen(navController, authRepository)
                            }

                            composable("home") {
                                HomeScreen(
                                    navController,
                                    firestoreRepository
                                ) {
                                    authRepository.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            }

                            composable("busBooking") {
                                BusBookingScreen(navController)
                            }

                            composable("localTrainBooking") {
                                LocalTrainBookingScreen(navController = navController)
                            }

                            composable("myBookings") {
                                MyBookingsScreen(navController, firestoreRepository)
                            }
                            composable("profile") {
                                ProfileScreen(
                                    navController = navController,
                                    firestoreRepository = firestoreRepository,
                                    onLogout = {
                                        authRepository.logout()
                                        navController.navigate("login") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("editProfile") {
                                EditProfileScreen(
                                    navController = navController
                                )
                            }



                            composable("roleCheck") {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid

                                LaunchedEffect(Unit) {
                                    if (uid == null) {
                                        navController.navigate("login") {
                                            popUpTo("roleCheck") { inclusive = true }
                                        }
                                        return@LaunchedEffect
                                    }

                                    val role = authRepository.getUserRole(uid)
                                    isAdmin = role == "ADMIN"

                                    navController.navigate(
                                        if (isAdmin) Routes.ADMIN_HOME else Routes.HOME
                                    )
                                    {
                                        popUpTo("roleCheck") { inclusive = true }
                                    }
                                }
                            }


                            composable(
                                "ticket_loader/{bookingId}",
                                arguments = listOf(navArgument("bookingId") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->

                                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable

                                TicketLoaderScreen(
                                    navController,
                                    bookingId,
                                    firestoreRepository
                                )
                            }

                        }
                    }
                }
            }
        }

        override fun onPaymentSuccess(paymentId: String?) {

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

            lifecycleScope.launch {
                try {

                    val booking = if (BusBookingState.fromStop.isNotBlank()) {
                        val bookingId = UUID.randomUUID().toString()
                        // 🚌 BUS BOOKING
                        Booking(
                            userId = uid,
                            mode = "BUS",
                            busNumber = BusBookingState.busNumber,
                            busType = BusBookingState.busType,   // ⭐ ADD THIS
                            from = BusBookingState.fromStop,
                            to = BusBookingState.toStop,
                            adults = BusBookingState.adults,
                            children = BusBookingState.children,
                            fare = BusBookingState.fare,
                            status = "CONFIRMED",
                            createdAt = System.currentTimeMillis(),


                        )


                    } else {
                        // 🚆 TRAIN / PLATFORM
                        val finalMode =
                            if (LocalTrainBookingState.mode == "PLATFORM") "PLATFORM" else "TRAIN"

                        Booking(
                            userId = uid,
                            mode = finalMode,

                            from = if (finalMode == "TRAIN") LocalTrainBookingState.from else "",
                            to = if (finalMode == "TRAIN") LocalTrainBookingState.to else "",

                            // ⭐ PLATFORM FIX
                            station = if (finalMode == "PLATFORM") LocalTrainBookingState.from else "",
                            stationName = if (finalMode == "PLATFORM") LocalTrainBookingState.stationName ?: "" else "",

                            adults = LocalTrainBookingState.adults,
                            children = LocalTrainBookingState.children,

                            ticketType = LocalTrainBookingState.ticketType,
                            trainType = LocalTrainBookingState.trainType,
                            ticketClass = LocalTrainBookingState.ticketClass,

                            fare = LocalTrainBookingState.fare,
                            status = "CONFIRMED",
                            createdAt = System.currentTimeMillis(),

                            concession = LocalTrainBookingState.concession
                        )
                    }

                    // ✅ SAVE FIRST
                    val bookingId =
                        firestoreRepository.saveBookingAndReturnId(booking)


                    pendingBookingId = bookingId

                    // ✅ CLEAR STATE ONLY AFTER NAVIGATION
                    BusBookingState.clear()
                    LocalTrainBookingState.clear()

                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Booking failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }


        // ❌ Razorpay error
        override fun onPaymentError(code: Int, response: String?) {
            Toast.makeText(
                this,
                "Payment failed. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }