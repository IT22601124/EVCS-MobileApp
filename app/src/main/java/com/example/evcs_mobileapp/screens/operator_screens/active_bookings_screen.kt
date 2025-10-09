package com.example.evcs_mobileapp.screens.operator_screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.platform.LocalContext
import com.example.evcs_mobileapp.AppConstants

@Composable
fun ActiveBookingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""
    val stationId = prefs.getString("stationId", "") ?: ""
    val today = "2025-10-04"
    var bookings by remember { mutableStateOf(listOf<BookingItem>()) }
    var resultMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val baseUrl: String = AppConstants.BASE_URL

    // Fetch bookings from API
    LaunchedEffect(stationId, today) {
        isLoading = true
        val client = OkHttpClient()
        val url = "${baseUrl}bookings?stationId=$stationId&date=$today"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                val arr = JSONArray(body ?: "[]")
                val items = mutableListOf<BookingItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    items.add(
                        BookingItem(
                            id = obj.optString("id"),
                            nic = obj.optString("nic"),
                            stationId = obj.optString("stationId"),
                            date = obj.optString("date"),
                            start = obj.optString("start"),
                            end = obj.optString("end"),
                            status = obj.optString("status")
                        )
                    )
                }
                bookings = items
            } else {
                resultMessage = "Failed to fetch bookings: ${response.code}"
            }
        } catch (e: Exception) {
            resultMessage = "Error: ${e.localizedMessage}"
        }
        isLoading = false
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Active Bookings", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else if (bookings.isEmpty()) {
                Text("No active bookings found.")
            } else {
                bookings.forEach { booking ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NIC: ${booking.nic}", style = MaterialTheme.typography.bodyMedium)
                            Text("Time: ${booking.start} - ${booking.end}", style = MaterialTheme.typography.bodySmall)
                            Text("Status: ${booking.status}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = {
                            isLoading = true
                            resultMessage = ""
                            val client = OkHttpClient()
                            val url = "${baseUrl}bookings/${booking.id}/complete"
                            val request = Request.Builder()
                                .url(url)
                                .put(RequestBody.create("application/json".toMediaType(), "{}"))
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                            try {
                                val response = client.newCall(request).execute()
                                if (response.isSuccessful) {
                                    resultMessage = "Booking finalized!"
                                    bookings = bookings.filter { it.id != booking.id }
                                } else {
                                    resultMessage = "Failed to finalize: ${response.code}"
                                }
                            } catch (e: Exception) {
                                resultMessage = "Error: ${e.localizedMessage}"
                            }
                            isLoading = false
                        }, enabled = booking.status != "completed") {
                            Text("Finalize")
                        }
                    }
                }
            }
            if (resultMessage.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(resultMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Data class for booking item
private data class BookingItem(
    val id: String,
    val nic: String,
    val stationId: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String
)
