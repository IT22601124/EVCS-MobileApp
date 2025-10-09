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
import com.example.evcs_mobileapp.AppConstants
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// Data classes matching API response
// Station with schedules
data class StationWithSchedules(
    val id: String,
    val name: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val type: String?,
    val slots: Int?,
    val isActive: Boolean?,
    val schedules: List<ScheduleDto> = emptyList()
)

data class ScheduleDto(
    val id: String,
    val stationId: String?,
    val date: String?,
    val slots: List<SlotDto> = emptyList()
)

data class SlotDto(
    val start: String?,
    val end: String?,
    val available: Boolean?,
    val capacity: Int?
)

// OperatorBookingItem for UI
data class OperatorBookingItem(
    val stationName: String?,
    val stationAddress: String?,
    val date: String?,
    val start: String?,
    val end: String?,
    val available: Boolean?,
    val capacity: Int?
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
    val baseUrl: String = AppConstants.BASE_URL

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            loading = true
            error = ""
            val client = OkHttpClient()
            val url = "${baseUrl}stations/with-weekly-schedules" // Use correct endpoint
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            try {
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    val type = object : com.google.gson.reflect.TypeToken<List<StationWithSchedules>>() {}.type
                    val stations: List<StationWithSchedules> = Gson().fromJson(body, type)
                    // Extract pending slots (available == true) as pending bookings
                    bookings = stations.flatMap { station ->
                        station.schedules.flatMap { schedule ->
                            schedule.slots.filter { it.available == true }.map { slot ->
                                OperatorBookingItem(
                                    stationName = station.name,
                                    stationAddress = station.address,
                                    date = schedule.date,
                                    start = slot.start,
                                    end = slot.end,
                                    available = slot.available,
                                    capacity = slot.capacity
                                )
                            }
                        }
                    }
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
            Text(item.stationName ?: "Unknown Station", style = MaterialTheme.typography.titleMedium)
            Text("${item.date ?: ""} ${item.start ?: ""} - ${item.end ?: ""}", style = MaterialTheme.typography.bodyMedium)
            Text("Capacity: ${item.capacity ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text("Available: ${if (item.available == true) "Yes" else "No"}", style = MaterialTheme.typography.bodySmall)
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
