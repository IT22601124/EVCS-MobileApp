package com.example.evcs_mobileapp.screens.operator_screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// Data class for bookings (reuse from your model)
data class OperatorBookingItem(
    val id: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val stationName: String,
    val stationAddress: String,
    val stationType: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PendingBookingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)
    var bookings by remember { mutableStateOf<List<OperatorBookingItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            loading = true
            error = ""
            val client = OkHttpClient()
            val url = "http://10.0.2.2:5132/api/bookings/pending" // Adjust endpoint as needed
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            try {
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    val type = object : com.google.gson.reflect.TypeToken<List<OperatorBookingItem>>() {}.type
                    bookings = Gson().fromJson(body, type)
                } else {
                    error = "Failed to fetch bookings: ${response.message}"
                }
            } catch (e: Exception) {
                error = "Error: ${e.localizedMessage}"
            }
            loading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pending Bookings") }) }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error.isNotBlank()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        } else if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No pending bookings")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(bookings) { item ->
                    PendingBookingCard(item, navController)
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
fun PendingBookingCard(item: OperatorBookingItem, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.stationName, style = MaterialTheme.typography.titleMedium)
            Text("${item.date} ${item.start} - ${item.end}", style = MaterialTheme.typography.bodyMedium)
            Text("Owner: ${item.ownerName}", style = MaterialTheme.typography.bodySmall)
            Text("Status: ${item.status}", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = { /* Approve logic */ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Approve")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { /* Cancel logic */ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Cancel")
                }
            }
        }
    }
}

