package com.aayush.smartticket

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    authRepository: AuthRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestoreRepository = remember { FirestoreRepository() }
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    // PASSWORD RULES
    val ruleLength = password.length >= 6
    val ruleUpperLower =
        password.any { it.isUpperCase() } && password.any { it.isLowerCase() }
    val ruleNumberOrSymbol =
        password.any { it.isDigit() || !it.isLetterOrDigit() }

    val isPasswordValid = ruleLength && ruleUpperLower && ruleNumberOrSymbol

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D47A1),
                        Color(0xFF1976D2),
                        Color(0xFF42A5F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Create account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Sign up to continue",
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = {
                            if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                mobile = it
                            }
                        },
                        label = { Text("Mobile number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                isPasswordFocused = it.isFocused
                            }
                    )

                    // PASSWORD RULES (only show when focused or typing)
                    if (isPasswordFocused || password.isNotEmpty()) {
                        Column {
                            PasswordRuleItem("At least 6 characters", ruleLength)
                            PasswordRuleItem("Uppercase & lowercase letters", ruleUpperLower)
                            PasswordRuleItem("Number or symbol", ruleNumberOrSymbol)
                        }
                    }

                    Button(
                        enabled = isPasswordValid && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            if (
                                name.isBlank() ||
                                mobile.length != 10 ||
                                email.isBlank() ||
                                !isPasswordValid
                            ) {
                                Toast.makeText(
                                    context,
                                    "Please enter valid details",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isLoading = true

                            scope.launch {
                                try {
                                    // 1️⃣ SIGN UP FIRST (AUTH ONLY)
                                    //
                                    val result = FirebaseAuth.getInstance()
                                        .createUserWithEmailAndPassword(email.trim(), password)
                                        .await()

                                    val user = result.user ?: throw IllegalStateException("User not created")
                                    val uid = user.uid




                                    // 2️⃣ CHECK MOBILE (FIRESTORE)
                                    if (firestoreRepository.isMobileExists(mobile)) {

                                        // rollback auth user
                                        user.delete()

                                        Toast.makeText(
                                            context,
                                            "Mobile number already registered",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        isLoading = false
                                        return@launch
                                    }

// 3️⃣ SAVE USER PROFILE (ONLY ONCE)
                                    firestoreRepository.createUserProfile(
                                        uid = uid,
                                        name = name.trim(),
                                        email = email.trim(),
                                        mobile = mobile
                                    )

                                    // 4️⃣ SEND EMAIL VERIFICATION
                                    user.sendEmailVerification()

                                    Toast.makeText(
                                        context,
                                        "Verification email sent. Please verify & login.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // 5️⃣ LOGOUT & GO TO LOGIN
                                    FirebaseAuth.getInstance().signOut()

                                    navController.navigate("login") {
                                        popUpTo("signup") { inclusive = true }
                                    }

                                } catch (e: Exception) {
                                    Log.e("SIGNUP_ERROR", e.message ?: "unknown", e)

                                    val message = when (e) {
                                        is FirebaseAuthUserCollisionException ->
                                            "Email already registered"
                                        is FirebaseAuthWeakPasswordException ->
                                            "Password too weak"
                                        is FirebaseAuthInvalidCredentialsException ->
                                            "Invalid email"
                                        is FirebaseNetworkException ->
                                            "No internet connection"
                                        else ->
                                            e.message ?: "Signup failed"
                                    }

                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }

                                isLoading = false
                            }

                        }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Sign up")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have an account? ", color = Color.White)
                Text(
                    "Login",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}

/* ---------- PASSWORD RULE ITEM ---------- */
@Composable
fun PasswordRuleItem(
    text: String,
    satisfied: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (satisfied)
                Icons.Filled.CheckCircle
            else
                Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (satisfied) Color(0xFF2E7D32) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            color = if (satisfied) Color(0xFF2E7D32) else Color.Gray
        )
    }
}
