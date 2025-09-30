package com.example.evcs_mobileapp.screens.login_screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.example.evcs_mobileapp.R
import com.example.evcs_mobileapp.network.RegistrationApi
import com.example.evcs_mobileapp.network.RegistrationPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

enum class Strength(val label: String) { WEAK("Weak"), MED("Medium"), STRONG("Strong") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavHostController,
    baseUrl: String = "http://10.0.2.2:5132/"
) {
    // ---- State ----
    var nic by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var showPwd by rememberSaveable { mutableStateOf(false) }
    var showPwd2 by rememberSaveable { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ---- Validation ----
    fun isValidNIC(s: String): Boolean {
        // Sri Lanka NIC: old 9 digits + V/X (case-insensitive), or 12 digits
        val re = Regex("""^(\d{9}[vVxX]|\d{12})$""")
        return re.matches(s.trim())
    }
    fun isValidEmail(s: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(s.trim()).matches()

    fun isValidPhone(s: String): Boolean {
        // Local mobile format: 0 + 9 digits (total 10). Adjust to your needs.
        return Regex("""^0\d{9}$""").matches(s.trim())
    }

    val nicError = nic.isNotBlank() && !isValidNIC(nic)
    val nameError = fullName.isNotBlank() && fullName.trim().length < 3
    val emailError = email.isNotBlank() && !isValidEmail(email)
    val phoneError = phone.isNotBlank() && !isValidPhone(phone)

    // simple password strength
    fun passwordStrength(p: String): Strength {
        val hasDigit = p.any { it.isDigit() }
        val hasLetter = p.any { it.isLetter() }
        val hasSpecial = p.any { !it.isLetterOrDigit() }
        return when {
            p.length >= 10 && hasDigit && hasLetter && hasSpecial -> Strength.STRONG
            p.length >= 8 && ((hasDigit && hasLetter) || (hasLetter && hasSpecial) || (hasDigit && hasSpecial)) -> Strength.MED
            else -> Strength.WEAK
        }
    }
    val pwdError = password.isNotBlank() && password.length < 6
    val pwdStrength = passwordStrength(password)
    val confirmError = confirmPassword.isNotBlank() && confirmPassword != password

    val formValid = nic.isNotBlank() && !nicError &&
            fullName.isNotBlank() && !nameError &&
            email.isNotBlank() && !emailError &&
            phone.isNotBlank() && !phoneError &&
            password.isNotBlank() && !pwdError &&
            confirmPassword.isNotBlank() && !confirmError &&
            acceptTerms

    // ---- Networking (keep simple; ideally move to DI/ViewModel) ----
    val retrofit = remember(baseUrl) {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(RegistrationApi::class.java) }

    // ---- UI ----
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .imePadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo circle
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
                        text = "Create your account",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Join EVCS to manage bookings with ease",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // NIC
                        OutlinedTextField(
                            value = nic,
                            onValueChange = { nic = it; errorMessage = null },
                            label = { Text("NIC") },
                            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                            singleLine = true,
                            isError = nicError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nicError) Text("Enter a valid NIC (e.g., 200012345678 or 123456789V)", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it; errorMessage = null },
                            label = { Text("Full name") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            isError = nameError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nameError) Text("Please enter your full name", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                            singleLine = true,
                            isError = emailError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (emailError) Text("Please enter a valid email", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                        // Phone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; errorMessage = null },
                            label = { Text("Phone number") },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            singleLine = true,
                            isError = phoneError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (phoneError) Text("Enter a valid number (e.g., 07XXXXXXXX or 0XXXXXXXXX)", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            singleLine = true,
                            isError = pwdError,
                            visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPwd = !showPwd }) {
                                    Icon(
                                        imageVector = if (showPwd) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showPwd) "Hide password" else "Show password"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // strength indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (bar1, bar2, bar3) = when (pwdStrength) {
                                Strength.WEAK -> Triple(Color.Red, Color.LightGray, Color.LightGray)
                                Strength.MED -> Triple(Color.Yellow, Color.Yellow, Color.LightGray)
                                Strength.STRONG -> Triple(Color(0xFF4CAF50), Color(0xFF4CAF50), Color(0xFF4CAF50))
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(bar1)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(bar2)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(bar3)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(pwdStrength.label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (pwdError) Text("Password must be at least 6 characters", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            label = { Text("Confirm password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            singleLine = true,
                            isError = confirmError,
                            visualTransformation = if (showPwd2) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPwd2 = !showPwd2 }) {
                                    Icon(
                                        imageVector = if (showPwd2) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showPwd2) "Hide password" else "Show password"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (confirmError) Text("Passwords do not match", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                        // Terms
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
                            Text(
                                "I agree to the Terms & Privacy Policy",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // Submit
                        Button(
                            onClick = {
                                errorMessage = null
                                if (!formValid) return@Button
                                isLoading = true
                                scope.launch {
                                    try {
                                        val payload = RegistrationPayload(
                                            nic = nic.trim(),
                                            fullName = fullName.trim(),
                                            email = email.trim(),
                                            phone = phone.trim(),
                                            password = password
                                        )
                                        val resp = withContext(Dispatchers.IO) { api.registerOwner(payload) }
                                        isLoading = false
                                        if (resp.isSuccessful) {
                                            success = true
                                            navController.navigate("login")
                                        } else {
                                            errorMessage = "Registration failed: ${resp.code()}"
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "Couldn’t reach the server. Check your connection."
                                    }
                                }
                            },
                            enabled = formValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Creating account…")
                            } else {
                                Text("Sign up")
                            }
                        }

                        // Subtle feedback
                        if (!errorMessage.isNullOrBlank()) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                        if (success) {
                            Text("Registration successful!", color = Color(0xFF4CAF50), fontSize = 13.sp)
                        }

                        // Divider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp)
                        ) {
                            Divider(Modifier.weight(1f))
                            Text("  or  ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Divider(Modifier.weight(1f))
                        }

                        // Google
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

                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { navController.navigate("login") }) {
                    Text("Already have an account? Log in")
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "By signing up, you agree to our Terms & Privacy Policy.",
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
fun SignupScreenPreview() {
    MaterialTheme { SignupScreen(navController = rememberNavController()) }
}
