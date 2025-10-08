package com.example.evcs_mobileapp.screens.login_screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.evcs_mobileapp.AppConstants
import com.example.evcs_mobileapp.R
import com.example.evcs_mobileapp.db.AppDatabase
import com.example.evcs_mobileapp.db.AuthResponseEntity
import com.example.evcs_mobileapp.network.AuthApi
import com.example.evcs_mobileapp.network.LoginPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    baseUrl: String = AppConstants.BASE_URL
) {
    // --- State ---
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Simple validation
    val usernameError = username.isNotBlank() && username.length < 3
    val pwdError = password.isNotBlank() && password.length < 6
    val isFormValid = username.isNotBlank() && !usernameError && password.isNotBlank() && !pwdError

    // Networking (keep for now; ideally move to DI/ViewModel)
    val retrofit = remember(baseUrl) {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(AuthApi::class.java) }

    val db = remember(context) {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "evcs_db"
        ).build()
    }
    val dao = db.authResponseDao()

    // --- UI ---
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Header with soft gradient + logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0EB804), // Dar9 green
                                Color(0xFF0EB804).copy(alpha = 0.9f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // App mark / logo circle (replace image if you have one)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logog), // Add walkthrough_2.png to drawable
                            contentDescription = "Reservation walkthrough",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Welcome back",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Sign in to continue",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card container for inputs
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                if (errorMessage != null) errorMessage = null
                            },
                            label = { Text("Username") },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null)
                            },
                            singleLine = true,
                            isError = usernameError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        if (usernameError) {
                            Text(
                                "Please enter a valid username (min 3 characters)",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (errorMessage != null) errorMessage = null
                            },
                            label = { Text("Password") },
                            singleLine = true,
                            isError = pwdError,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        if (pwdError) {
                            Text(
                                "Password must be at least 6 characters",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Forgot password?",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { /* TODO: navigate to reset */ }
                            )
                            Text(
                                "Create account",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { navController.navigate("signup") }
                            )
                        }

                        Button(
                            onClick = {
                                errorMessage = null
                                if (!isFormValid) return@Button
                                isLoading = true
                                scope.launch {
                                    try {
                                        val resp = withContext(Dispatchers.IO) {
                                            api.login(LoginPayload(username, password))
                                        }
                                        isLoading = false
                                        if (resp.isSuccessful && resp.body()?.token != null) {
                                            val body = resp.body()!!
                                            withContext(Dispatchers.IO) {
                                                dao.clear()
                                                dao.insert(AuthResponseEntity(
                                                    nic = body.nic ?: "",
                                                    fullName = body.fullName,
                                                    email = body.email,
                                                    phone = body.phone,
                                                    isActive = body.isActive,
                                                    role = body.role,
                                                    token = body.token,
                                                    expiresAt = body.expiresAt,
                                                    username = body.username,
                                                    isOwner = body.isOwner,
                                                    ownerNic = body.ownerNic
                                                ))
                                            }
                                            // Save token and role to SharedPreferences for other screens
                                            val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
                                            prefs.edit().putString("token", body.token).putString("role", body.role).apply()
                                            // Navigate based on role
                                            if (body.role == "Owner") {
                                                navController.navigate("owner_home")
                                            } else if (body.role == "Backoffice" || body.role == "admin" || body.role == "operator") {
                                                navController.navigate("operator_home")
                                            } else {
                                                errorMessage = "Unknown user role. Please contact support."
                                            }
                                        } else {
                                            errorMessage = "Invalid credentials. Please try again."
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "Couldn’t reach the server. Check your connection."
                                    }
                                }
                            },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Signing in…")
                            } else {
                                Text("Sign in")
                            }
                        }

                        // Error text (subtle)
                        if (!errorMessage.isNullOrBlank()) {
                            Text(
                                errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }

                        // Divider with "or"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp)
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                "  or  ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }

                        // Google button
                        OutlinedButton(
                            onClick = { /* TODO: Google Sign-In */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.gicon),
                                contentDescription = "Google",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Continue with Google")
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Footer hint
                Text(
                    text = "By continuing, you agree to our Terms & Privacy Policy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(navController = rememberNavController())
    }
}
