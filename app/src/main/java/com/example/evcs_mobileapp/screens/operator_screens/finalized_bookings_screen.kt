package com.example.evcs_mobileapp.screens.operator_screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import com.example.evcs_mobileapp.AppConstants
import java.text.SimpleDateFormat
import java.util.*

data class CompletedBooking(
    val id: String,
    val nic: String,
    val stationId: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String,
    val ownerName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizedBookingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""
    var bookings by remember { mutableStateOf(listOf<CompletedBooking>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val baseUrl = AppConstants.BASE_URL

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch(Dispatchers.IO) {
            try {
                val assignedStationId = prefs.getString("assignedStationId", "") ?: ""
                if (assignedStationId.isBlank()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "No assigned station found"
                        isLoading = false
                    }
                    return@launch
                }

                val client = OkHttpClient()
                val url = "${baseUrl}bookings/by-station/$assignedStationId"
                Log.d("FinalizedBookingsScreen", "Fetching bookings: $url")

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                        val jsonArray = JSONArray(responseBody)
                        val completedBookings = mutableListOf<CompletedBooking>()

                        for (i in 0 until jsonArray.length()) {
                            val booking = jsonArray.getJSONObject(i)
                            if (booking.getString("status").equals("completed", ignoreCase = true)) {
                                completedBookings.add(
                                    CompletedBooking(
                                        id = booking.getString("id"),
                                        nic = booking.getString("nic"),
                                        stationId = booking.getString("stationId"),
                                        date = booking.getString("date"),
                                        start = booking.getString("start"),
                                        end = booking.getString("end"),
                                        status = booking.getString("status"),
                                        ownerName = booking.getString("ownerName")
                                    )
                                )
                            }
                        }
                        bookings = completedBookings.sortedByDescending { it.date }
                    } else {
                        errorMessage = "Failed to fetch bookings: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                Log.e("FinalizedBookingsScreen", "Error fetching bookings", e)
                withContext(Dispatchers.Main) {
                    errorMessage = "Error: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalized Bookings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (bookings.isEmpty()) {
                Text(
                    text = "No completed bookings found",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                bookings.forEach { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Customer: ${booking.ownerName}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("NIC: ${booking.nic}")
                            Text("Date: ${booking.date}")
                            Text("Time: ${booking.start} - ${booking.end}")
                            Text(
                                text = "Status: ${booking.status}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
