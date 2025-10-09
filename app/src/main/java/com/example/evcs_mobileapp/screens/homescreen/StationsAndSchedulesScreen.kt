package com.example.evcs_mobileapp.screens.homescreen

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcs_mobileapp.model.StationWithSchedulesDto
import com.example.evcs_mobileapp.viewmodel.BookingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.util.Calendar
import androidx.navigation.NavController
import android.util.Log
import com.example.evcs_mobileapp.AppConstants


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsAndSchedulesScreen(navController: NavController, bookingViewModel: BookingViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)
    var stationsWithSchedules by remember { mutableStateOf<List<StationWithSchedulesDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val http = remember { OkHttpClient() }
    val gson = remember { Gson() }
    val scope = rememberCoroutineScope()
    val baseUrl: String = AppConstants.BASE_URL

    // Theme colors
    val primaryGreen = Color(0xFF4CAF50)
    val darkGreen = Color(0xFF388E3C)
    val lightGreen = Color(0xFFE8F5E9)
    val textSecondary = Color(0xFF666666)
    val errorRed = Color(0xFFD32F2F)

    fun fetchSchedules() {
        scope.launch {
            loading = true
            error = ""

            // Check if token exists and is not expired
            if (token.isNullOrBlank()) {
                error = "No authentication token available. Please login first."
                loading = false
                return@launch
            }

            val url = "${baseUrl}stations/with-weekly-schedules"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                val result = withContext(Dispatchers.IO) {
                    val response = http.newCall(request).execute()
                    val body = response.body?.string().orEmpty()
                    response to body
                }
                Log.d("SchedulesAPI", "HTTP Response: ${result.first}")
                val response = result.first
                val body = result.second

                when (response.code) {
                    200 -> {
                        Log.d("SchedulesAPI", "Response body: $body")
                        val type = object : TypeToken<List<StationWithSchedulesDto>>() {}.type
                        stationsWithSchedules = gson.fromJson(body, type)
                        error = ""
                    }
                    401 -> {
                        error = "Authentication failed. Please login again."
                    }
                    403 -> {
                        error = "Access denied. Insufficient permissions."
                    }
                    404 -> {
                        error = "Schedules endpoint not found."
                    }
                    else -> {
                        error = "Failed to fetch schedules: ${response.message} (${response.code})"
                    }
                }
            } catch (e: Exception) {
                error = "Network error: ${e.localizedMessage}"
                Log.e("SchedulesAPI", "Error fetching schedules", e)
            } finally {
                loading = false
            }
        }
    }

    // Show error if token is missing
    if (token.isNullOrBlank()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = primaryGreen
                    )
                    Text(
                        "Authentication Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        "Please log in to view station schedules",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }
            }
        }
        return
    }

    // Fetch schedules with stations on first load
    LaunchedEffect(Unit) {
        fetchSchedules()
    }

    val today = LocalDate.now().toString() // Use current date
    // Find the next available schedule for each station (date >= today)
    val stationsWithNextSchedules = stationsWithSchedules.mapNotNull { station ->
        val nextSchedule = station.schedules
            .filter { it.date != null && it.date >= today }
            .minByOrNull { it.date ?: "9999-99-99" }
        if (nextSchedule != null) {
            station to nextSchedule
        } else {
            null
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = primaryGreen,
                shadowElevation = 4.dp
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        "Stations & Schedules",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Today: $today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Content
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when {
                    loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = primaryGreen,
                                strokeWidth = 5.dp
                            )
                            Spacer(Modifier.height(20.dp))
                            Text(
                                "Loading schedules...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textSecondary
                            )
                        }
                    }
                    error.isNotBlank() -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    error,
                                    color = errorRed,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = { scope.launch { fetchSchedules() } },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryGreen
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    stationsWithNextSchedules.isEmpty() -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                Modifier.padding(32.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = textSecondary
                                )
                                Text(
                                    "No schedules available",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121)
                                )
                                Text(
                                    "There are no charging schedules for today or future dates.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondary
                                )
                                Button(
                                    onClick = { scope.launch { fetchSchedules() } },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryGreen
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Refresh")
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(stationsWithNextSchedules) { (station, schedule) ->
                                val stationName = station.name ?: "Unknown Station"
                                val stationAddress = station.address ?: "Address not available"
                                val slots = schedule.slots

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(Modifier.padding(20.dp)) {
                                        // Station Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(lightGreen, RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = darkGreen,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    stationName,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF212121)
                                                )
                                                Text(
                                                    stationAddress,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = textSecondary,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }

                                        Divider(
                                            modifier = Modifier.padding(vertical = 16.dp),
                                            color = Color(0xFFE0E0E0)
                                        )

                                        // Schedules for next available date
                                        Text(
                                            "Available Time Slots for ${schedule.date}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF212121),
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        if (slots.isEmpty()) {
                                            Text(
                                                "No time slots available",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textSecondary,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        } else {
                                            slots.forEach { slot ->
                                                val slotText = "${slot.start} - ${slot.end}"
                                                val statusText = if (slot.available) "Available" else "Full"
                                                val capacityText = "Capacity: ${slot.capacity}"

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp)
                                                        .clickable(enabled = slot.available) {
                                                            bookingViewModel.setBooking(
                                                                com.example.evcs_mobileapp.model.StationDto(
                                                                    id = station.id,
                                                                    name = station.name,
                                                                    address = station.address,
                                                                    latitude = station.latitude,
                                                                    longitude = station.longitude,
                                                                    type = station.type,
                                                                    slots = station.slots,
                                                                    isActive = station.isActive
                                                                ),
                                                                schedule.date,
                                                                slot
                                                            )
                                                            navController.navigate("new_booking")
                                                        },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (slot.available)
                                                            lightGreen else Color(0xFFF5F5F5)
                                                    ),
                                                    elevation = CardDefaults.cardElevation(0.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                slotText,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = if (slot.available) darkGreen else Color(0xFF757575)
                                                            )
                                                            Text(
                                                                capacityText,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = if (slot.available) darkGreen else Color(0xFF9E9E9E),
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                        }

                                                        Surface(
                                                            shape = RoundedCornerShape(16.dp),
                                                            color = if (slot.available) primaryGreen else Color(0xFFBDBDBD)
                                                        ) {
                                                            Text(
                                                                statusText,
                                                                modifier = Modifier.padding(
                                                                    horizontal = 12.dp,
                                                                    vertical = 6.dp
                                                                ),
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                fontSize = 12.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Bottom spacing
                            item {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}