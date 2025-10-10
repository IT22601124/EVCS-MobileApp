package com.example.evcs_mobileapp.screens.operator_screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.platform.LocalContext
import com.example.evcs_mobileapp.AppConstants
import com.example.evcs_mobileapp.model.BookingItem
import kotlinx.coroutines.CoroutineScope
import okhttp3.RequestBody.Companion.toRequestBody


@Composable
fun ActiveBookingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""
    val today = "2025-10-10" // Updated to current date
    var bookings by remember { mutableStateOf(listOf<BookingItem>()) }
    var resultMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val baseUrl: String = AppConstants.BASE_URL
    val scope = rememberCoroutineScope()

    LaunchedEffect(token, today) {
        isLoading = true

        scope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val userId = prefs.getString("assignedStationId", "") ?: ""
                Log.d("ActiveBookingsScreen", "User ID: $userId")

                if (userId.isNotBlank()) {
                    val url = "${baseUrl}bookings/by-station/$userId"
                    Log.d("ActiveBookingsScreen", "Fetching bookings from URL: $url")

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    Log.d("ActiveBookingsScreen", "Response code: ${response.code}, body: $responseBody")

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                            val arr = JSONArray(responseBody)
                            val items = mutableListOf<BookingItem>()

                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                if (obj.optString("status") == "Approved") {
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
                            }
                            bookings = items
                            Log.d("ActiveBookingsScreen", "Successfully parsed ${items.size} approved bookings")
                        } else {
                            resultMessage = "Failed to fetch bookings: ${response.code}"
                            Log.e("ActiveBookingsScreen", resultMessage)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        resultMessage = "No assigned station found for user"
                        Log.e("ActiveBookingsScreen", resultMessage)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resultMessage = "Network error: ${e.localizedMessage}"
                    Log.e("ActiveBookingsScreen", "Network error", e)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
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
                            scope.launch(Dispatchers.IO) {
                                withContext(Dispatchers.Main) {
                                    isLoading = true
                                    resultMessage = ""
                                }

                                val client = OkHttpClient()
                                val url = "${baseUrl}bookings/${booking.id}/finalize"
                                val request = Request.Builder()
                                    .url(url)
                                    .post("".toRequestBody("application/json".toMediaType()))
                                    .addHeader("Authorization", "Bearer $token")
                                    .addHeader("Content-Type", "application/json")
                                    .build()

                                try {
                                    val response = client.newCall(request).execute()
                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                            resultMessage = "Booking finalized!"
                                            bookings = bookings.filter { it.id != booking.id }
                                        } else {
                                            resultMessage = "Failed to finalize: ${response.code}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        resultMessage = "Error: ${e.localizedMessage}"
                                    }
                                } finally {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                    }
                                }
                            }
                        }, enabled = !isLoading && booking.status != "completed") {
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

// Make BookingItem public so it can be used elsewhere
data class BookingItem(
    val id: String,
    val nic: String,
    val stationId: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String
)
