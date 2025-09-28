package com.example.evcs_mobileapp.login_screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.evcs_mobileapp.R
import com.example.evcs_mobileapp.network.AuthApi
import com.example.evcs_mobileapp.network.LoginPayload
import com.example.evcs_mobileapp.network.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun login_screen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Retrofit setup
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5132/api/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(AuthApi::class.java) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text("Login", fontSize = 24.sp, color = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", fontSize = 12.sp) },
            modifier = Modifier.width(300.dp).height(56.dp),
            shape = RoundedCornerShape(25.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", fontSize = 12.sp) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.width(300.dp).height(56.dp),
            shape = RoundedCornerShape(25.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (isLoading) {
            CircularProgressIndicator()
        }
        errorMessage?.let {
            Text(it, color = Color.Red, fontSize = 14.sp)
        }
        if (success) {
            Text("Login successful!", color = Color(0xFF4CAF50), fontSize = 14.sp)
        }
        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                success = false
                coroutineScope.launch {
                    val payload = LoginPayload(email, password)
                    try {
                        val response = withContext(Dispatchers.IO) {
                            api.login(payload)
                        }
                        isLoading = false
                        if (response.isSuccessful && response.body()?.token != null) {
                            success = true
                            // Store token in SharedPreferences
                            val loginResp = response.body()!!
                            val prefs = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("token", loginResp.token)
                                .putString("username", loginResp.username)
                                .putString("role", loginResp.role)
                                .putString("expiresAt", loginResp.expiresAt)
                                .apply()
                            navController.navigate("owner_home")
                        } else {
                            errorMessage = "Login failed: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Network error: ${e.localizedMessage}"
                    }
                }
            },
            modifier = Modifier
                .width(300.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Login", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Gray,
                thickness = 1.dp
            )
            Text(
                "Or",
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 14.sp,
                color = Color.Gray
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Gray,
                thickness = 1.dp
            )
        }
        Button(
            onClick = { /* Handle Google login */ },
            modifier = Modifier
                .width(300.dp)
                .height(48.dp)
                .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(25.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.gicon), // Ensure gicon.png exists in drawable
                contentDescription = "Google Icon",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login with Google", fontSize = 14.sp, color = Color.Black)
        }
    }
}
