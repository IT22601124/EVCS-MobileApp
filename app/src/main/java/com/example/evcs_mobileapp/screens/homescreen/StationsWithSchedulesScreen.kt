package com.example.evcs_mobileapp.screens.homescreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// Data classes for stations with schedules
 data class StationWithSchedulesDto(
    val id: String,
    val name: String,
    val location: String,
    val schedules: List<ScheduleSlotDto>
 )

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsWithSchedulesScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)
    var stations by remember { mutableStateOf<List<StationWithSchedulesDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val http = remember { OkHttpClient() }
    val gson = remember { Gson() }

    // Fetch all stations with schedules on first load
    LaunchedEffect(Unit) {
        loading = true
        error = ""
        val url = "http://10.0.2.2:5132/api/stations/with-schedules"
        val reqBuilder = Request.Builder().url(url)
        if (!token.isNullOrBlank()) reqBuilder.addHeader("Authorization", "Bearer $token")
        try {
            val res = withContext(Dispatchers.IO) { http.newCall(reqBuilder.build()).execute() }
            if (res.isSuccessful) {
                val body = res.body?.string().orEmpty()
                val type = object : com.google.gson.reflect.TypeToken<List<StationWithSchedulesDto>>() {}.type
                stations = gson.fromJson(body, type)
            } else {
                error = "Failed to fetch stations: ${res.message}"
            }
        } catch (e: Exception) {
            error = "Error: ${e.localizedMessage}"
        }
        loading = false
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.98f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Stations With Schedules", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF4CAF50))
                if (loading) {
                    CircularProgressIndicator()
                } else if (error.isNotBlank()) {
                    Text(error, color = Color.Red)
                } else {
                    LazyColumn(Modifier.fillMaxWidth()) {
                        items(stations) { station ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("${station.name} (${station.location})", style = MaterialTheme.typography.titleMedium)
                                    if (station.schedules.isNotEmpty()) {
                                        Text("Schedules:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        station.schedules.forEach { slot ->
                                            val text = "${slot.start} - ${slot.end} " +
                                                (if (slot.available) "(Available)" else "(Full)") +
                                                " | cap: ${slot.capacity}"
                                            Text(text, fontSize = 14.sp, color = if (slot.available) Color(0xFF388E3C) else Color.Red)
                                        }
                                    } else {
                                        Text("No schedules available.", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
