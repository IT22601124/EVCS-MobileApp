package com.example.evcs_mobileapp.screens.homescreen

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcs_mobileapp.screens.homescreen.ScheduleSlotDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

// Data classes for stations and schedules
 data class StationDto(
    val id: String,
    val name: String,
    val location: String
 )

 data class StationScheduleDto(
    val stationId: String,
    val date: String,
    val slots: List<ScheduleSlotDto>
 )

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsAndSchedulesScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)
    var stations by remember { mutableStateOf<List<StationDto>>(emptyList()) }
    var selectedStation by remember { mutableStateOf<StationDto?>(null) }
    var date by remember { mutableStateOf("") }
    var schedules by remember { mutableStateOf<List<ScheduleSlotDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val http = remember { OkHttpClient() }
    val gson = remember { Gson() }

    // Show error if token is missing
    if (token.isNullOrBlank()) {
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
                    Text("Stations & Schedules", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF4CAF50))
                    Text("You are not logged in. Please login to view stations${token}", color = Color.Red)
                }
            }
        }
        return
    }

    // Fetch stations on first load
    LaunchedEffect(Unit) {
        loading = true
        error = ""
        val url = "http://10.0.2.2:5132/api/stations"
        val reqBuilder = Request.Builder().url(url)
        if (!token.isNullOrBlank()) reqBuilder.addHeader("Authorization", "Bearer $token")
        try {
            val res = withContext(Dispatchers.IO) { http.newCall(reqBuilder.build()).execute() }
            if (res.isSuccessful) {
                val body = res.body?.string().orEmpty()
                val type = object : com.google.gson.reflect.TypeToken<List<StationDto>>() {}.type
                stations = gson.fromJson(body, type)
            } else {
                error = "Failed to fetch stations: ${res.message}"
            }
        } catch (e: Exception) {
            error = "Error: ${e.localizedMessage}"
        }
        loading = false
    }

    // Fetch schedules when station and date are selected
    LaunchedEffect(selectedStation, date) {
        if (selectedStation != null && date.isNotBlank()) {
            loading = true
            error = ""
            val url = "http://10.0.2.2:5132/api/schedules?stationId=${selectedStation!!.id}&date=$date"
            val reqBuilder = Request.Builder().url(url)
            if (!token.isNullOrBlank()) reqBuilder.addHeader("Authorization", "Bearer $token")
            try {
                val res = withContext(Dispatchers.IO) { http.newCall(reqBuilder.build()).execute() }
                if (res.isSuccessful) {
                    val body = res.body?.string().orEmpty()
                    val type = object : com.google.gson.reflect.TypeToken<StationScheduleDto>() {}.type
                    val schedule: StationScheduleDto? = gson.fromJson(body, type)
                    schedules = schedule?.slots.orEmpty()
                } else {
                    error = "Failed to fetch schedules: ${res.message}"
                }
            } catch (e: Exception) {
                error = "Error: ${e.localizedMessage}"
            }
            loading = false
        }
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
                Text("Stations & Schedules", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF4CAF50))
                if (loading) {
                    CircularProgressIndicator()
                } else if (error.isNotBlank()) {
                    Text(error, color = Color.Red)
                } else {
                    // Station list
                    Text("Select Station:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(Modifier.height(120.dp)) {
                        items(stations) { station ->
                            Button(
                                onClick = { selectedStation = station },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedStation?.id == station.id) Color(0xFF4CAF50) else Color.LightGray
                                ),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Text("${station.name} (${station.location})")
                            }
                        }
                    }
                    // Date picker
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Date (YYYY-MM-DD)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                val now = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val mm = (m + 1).toString().padStart(2, '0')
                                        val dd = d.toString().padStart(2, '0')
                                        date = "$y-$mm-$dd"
                                    },
                                    now.get(Calendar.YEAR),
                                    now.get(Calendar.MONTH),
                                    now.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) { Icon(Icons.Default.DateRange, contentDescription = "Pick date") }
                        }
                    )
                    // Schedules for selected station/date
                    if (schedules.isNotEmpty()) {
                        Text("Available Slots:", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(Modifier.height(160.dp)) {
                            items(schedules) { slot ->
                                val text = "${slot.start} - ${slot.end} " +
                                    (if (slot.available) "(Available)" else "(Full)") +
                                    " | cap: ${slot.capacity}"
                                Button(
                                    onClick = {},
                                    enabled = slot.available,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (slot.available) Color(0xFF4CAF50) else Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Text(text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
