package com.aayush.smartticket

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.auth.FirebaseAuth

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(navController: NavController) {

    val repo = remember { AdminRepository() }
    val scope = rememberCoroutineScope()   // ✅ ADD THIS

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        users = repo.getAllUsers()
        loading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Users Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                AdminUserRow(
                    user = user,
                    repo = repo,
                    scope = scope,
                    onUserDeleted = { deletedId ->        // ✅ FIXED
                        users = users.filterNot { it.id == deletedId }
                    }
                )
            }
        }
    }
}
@Composable
fun AdminUserRow(
    user: User,
    repo: AdminRepository,
    scope: CoroutineScope,
    onUserDeleted: (String) -> Unit
) {
    var isBlocked by remember { mutableStateOf(user.blocked) }
    val context = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.SemiBold)
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                AssistChip(
                    onClick = {},
                    label = { Text(if (isBlocked) "Blocked" else "Active") } ,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (user.blocked)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            Row {

                if (isBlocked) {
                    Button(onClick = {
                        scope.launch {
                            repo.unblockUser(user.id)
                            isBlocked = false
                        }
                    }) {
                        Text("Unblock")
                    }
                } else {
                    OutlinedButton(onClick = {
                        scope.launch {
                            repo.blockUser(user.id)
                            isBlocked = true
                        }
                    }) {
                        Text("Block")
                    }
                }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(onClick = {
                    scope.launch {

                        if (user.id == currentUid) {
                            Toast.makeText(context, "You cannot delete yourself", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        try {
                            repo.deleteUserPermanently(user.id)
                            onUserDeleted(user.id)

                            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {

                            if (e.message?.contains("NOT_FOUND") == true) {

                                // User already deleted in Firebase Auth → treat as success
                                onUserDeleted(user.id)

                                Toast.makeText(
                                    context,
                                    "User deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                Toast.makeText(
                                    context,
                                    "Delete failed: ${e.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                    }
                }) {
                    Text("Delete")
                }
            }
        }
    }
}
