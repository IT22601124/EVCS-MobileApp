package com.example.evcs_mobileapp.homescreen

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.Calendar

// ---------- API models (match your swagger) ----------
data class ScheduleDto(
    val id: String,
    val stationId: String,
    val date: String, // "YYYY-MM-DD"
    val slots: List<ScheduleSlotDto>
)

data class ScheduleSlotDto(
    val start: String,       // "HH:mm:ss" (or "HH:mm")
    val end: String,         // "HH:mm:ss" (or "HH:mm")
    val available: Boolean,
    val capacity: Int
)

// Request body for /api/Bookings
data class CreateBookingBody(
    val nic: String,
    val stationId: String,
    val date: String,   // "YYYY-MM-DD"
    val start: String,  // "HH:mm:ss"
    val end: String     // "HH:mm:ss"
)

// ---------- Screen ----------
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSchedulesThenCreateScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)

    var nic by remember { mutableStateOf("") }
    var stationId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") } // user picks

    var schedules by remember { mutableStateOf<List<ScheduleDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // flattened slots (from the first schedule that matches the stationId/date)
    var slots by remember { mutableStateOf<List<ScheduleSlotDto>>(emptyList()) }
    var selectedSlot by remember { mutableStateOf<ScheduleSlotDto?>(null) }

    val scope = rememberCoroutineScope()
    val http = remember { OkHttpClient() }
    val gson = remember { Gson() }

    fun openDatePicker(onPick: (String) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                onPick("$y-$mm-$dd")
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    suspend fun fetchSchedules(stationId: String, date: String): Result<List<ScheduleDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "http://10.0.2.2:5132/api/Schedules?stationId=$stationId&date=$date"
                val reqBuilder = Request.Builder().url(url)
                if (!token.isNullOrBlank()) reqBuilder.addHeader("Authorization", "Bearer $token")
                val res = http.newCall(reqBuilder.build()).execute()
                if (!res.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to fetch schedules: ${res.code} ${res.message}"))
                }
                val body = res.body?.string().orEmpty()
                val type = object : TypeToken<List<ScheduleDto>>() {}.type
                val parsed: List<ScheduleDto> = gson.fromJson(body, type) ?: emptyList()
                Result.success(parsed)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createBooking(
        nic: String,
        stationId: String,
        date: String,
        start: String,
        end: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "http://10.0.2.2:5132/api/Bookings" // POST
                val payload = gson.toJson(CreateBookingBody(nic, stationId, date, start.ensureSeconds(), end.ensureSeconds()))
                val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), payload)
                val reqBuilder = Request.Builder().url(url).post(body)
                if (!token.isNullOrBlank()) reqBuilder.addHeader("Authorization", "Bearer $token")
                val res = http.newCall(reqBuilder.build()).execute()
                if (res.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Booking failed: ${res.code} ${res.message}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Fetch schedules when all three inputs are present
    LaunchedEffect(nic, stationId, date) {
        if (nic.isNotBlank() && stationId.isNotBlank() && date.isNotBlank()) {
            loading = true
            error = null
            selectedSlot = null
            slots = emptyList()
            val result = fetchSchedules(stationId, date)
            loading = false
            result.onSuccess { list ->
                schedules = list
                // Pick the first schedule item (API returns an array)
                val first = list.firstOrNull()
                slots = first?.slots.orEmpty()
                if (slots.isEmpty()) error = "No slots found for $date."
            }.onFailure {
                error = it.message ?: "Unknown error"
            }
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
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("New Booking", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF4CAF50))

                // NIC
                OutlinedTextField(
                    value = nic,
                    onValueChange = { nic = it },
                    label = { Text("NIC", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Station ID
                OutlinedTextField(
                    value = stationId,
                    onValueChange = { stationId = it },
                    label = { Text("Station ID", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Date (picker)
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Date (YYYY-MM-DD)", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { openDatePicker { date = it } }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                        }
                    }
                )

                if (loading) {
                    CircularProgressIndicator()
                } else if (!error.isNullOrBlank()) {
                    Text(error!!, color = Color.Red)
                } else if (slots.isNotEmpty()) {
                    Text("Available Slots", style = MaterialTheme.typography.titleMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        slots.forEach { slot ->
                            val text =
                                "${slot.start.ensureSeconds()} - ${slot.end.ensureSeconds()}" +
                                        (if (slot.available) "" else " (Full)") +
                                        "  | cap: ${slot.capacity}"
                            Button(
                                onClick = { if (slot.available) selectedSlot = slot },
                                enabled = slot.available,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (slot.available) Color(0xFF4CAF50) else Color.LightGray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text)
                            }
                        }
                    }
                }

                if (selectedSlot != null) {
                    val s = selectedSlot!!
                    Button(
                        onClick = {
                            // guard
                            if (nic.isBlank() || stationId.isBlank() || date.isBlank()) return@Button
                            scope.launch {
                                loading = true
                                error = null
                                val res = createBooking(
                                    nic = nic,
                                    stationId = stationId,
                                    date = date,
                                    start = s.start,
                                    end = s.end
                                )
                                loading = false
                                res.onSuccess {
                                    selectedSlot = null
                                    // Re-fetch to reflect capacity/availability after booking (optional)
                                    val r2 = fetchSchedules(stationId, date)
                                    r2.onSuccess { list ->
                                        schedules = list
                                        slots = list.firstOrNull()?.slots.orEmpty()
                                    }
                                    // small success toast substitute:
                                    error = "Booking successful."
                                    // Navigate to MyBookingsScreen after success
                                    val navController = null
                                    navController?.navigate("my_bookings")
                                }.onFailure {
                                    error = it.message ?: "Booking failed."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Confirm Booking (${s.start.ensureSeconds()}–${s.end.ensureSeconds()})", fontSize = 16.sp)
                    }
                }

                Text(
                    "Can modify/cancel ≥12h before start.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                if (token.isNullOrBlank()) {
                    Text(
                        "⚠️ No auth token found. Login first to add Authorization header.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

private fun Nothing?.navigate(string: String) {}

// ---------- Helpers ----------
private fun String.ensureSeconds(): String {
    // turns "HH:mm" -> "HH:mm:00", leaves "HH:mm:ss" untouched
    return if (this.count { it == ':' } == 1) "$this:00" else this
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewBookingSchedulesThenCreateScreen() {
    BookingSchedulesThenCreateScreen()
}
