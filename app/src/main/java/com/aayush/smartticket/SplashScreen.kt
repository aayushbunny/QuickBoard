package com.aayush.smartticket

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    navController: NavHostController,
    isLoggedIn: Boolean,
    isAdmin: Boolean
) {
    val cardScale = remember { Animatable(0.85f) }
    val cardAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        cardAlpha.animateTo(1f, tween(600))
        cardScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
        delay(2500)

        val destination = when {
            isAdmin -> Routes.ADMIN_HOME
            isLoggedIn -> Routes.HOME
            else -> "login"
        }

        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFF2563EB),
            Color(0xFF1D4ED8),
            Color(0xFF1E40AF)
        )
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        AnimatedOrb(Alignment.TopStart, Color(0xFF60A5FA))
        AnimatedOrb(Alignment.BottomEnd, Color(0xFF93C5FD), 350.dp)

        FloatingIcon(R.drawable.ic_train_outline, Alignment.TopEnd)
        FloatingIcon(R.drawable.ic_bus_outline, Alignment.BottomStart)
        FloatingIcon(R.drawable.ic_map_outline, Alignment.CenterStart)

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .scale(cardScale.value)
                    .alpha(cardAlpha.value)
                    .width(300.dp)
                    .height(360.dp)
                    .background(Color.White.copy(0.12f), RoundedCornerShape(36.dp))
                    .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(36.dp))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    GlowingLogo()

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "QuickBoard",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Train & Bus Tickets",
                        color = Color.White.copy(0.8f),
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    LoadingDots()
                }
            }
        }

        Text(
            "YOUR JOURNEY STARTS HERE",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            color = Color.White.copy(0.6f),
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
    }
}
@Composable
fun GlowingLogo() {
    val glowAnim = rememberInfiniteTransition()

    val glow by glowAnim.animateFloat(
        20f, 60f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .shadow(glow.dp, RoundedCornerShape(24.dp), ambientColor = Color.White)
            .background(Color.White, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_quickboard_logo),
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(52.dp)
        )

    }
}
@Composable
fun BoxScope.AnimatedOrb(alignment: Alignment, color: Color, size: Dp = 280.dp) {

    val transition = rememberInfiniteTransition()

    val offset by transition.animateFloat(
        0f, 50f,
        infiniteRepeatable(tween(8000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .size(size)
            .align(alignment)
            .offset(x = offset.dp, y = offset.dp)
            .background(color.copy(alpha = 0.25f), CircleShape)
            .blur(120.dp)
    )
}

@Composable
fun BoxScope.FloatingIcon(res: Int, alignment: Alignment) {

    val transition = rememberInfiniteTransition()

    val offset by transition.animateFloat(
        0f, 18f,
        infiniteRepeatable(tween(3000), RepeatMode.Reverse)
    )

    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .align(alignment)
            .offset(y = offset.dp)
            .alpha(0.15f)
            .padding(40.dp)
    )
}

@Composable
fun LoadingDots() {
    val transition = rememberInfiniteTransition()

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val scale by transition.animateFloat(
                0.6f, 1.4f,
                infiniteRepeatable(
                    tween(1200, delayMillis = index * 150),
                    RepeatMode.Reverse
                )
            )

            Box(
                Modifier
                    .size(8.dp)
                    .scale(scale)
                    .background(Color.White, CircleShape)
            )
        }
    }
}
