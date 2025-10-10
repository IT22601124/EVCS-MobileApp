package com.example.evcs_mobileapp.screens.operator_screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.example.evcs_mobileapp.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCreateScreen(navController: NavHostController) {
    var stationId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var showEndTimeErrorDialog by remember { mutableStateOf(false) }
    var errorDialogSlotIdx by remember { mutableStateOf(-1) }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""

    // Dynamic slots
    var slots by remember { mutableStateOf(listOf<SlotInput>()) }
    // Track which picker is open for each slot: null, "start", or "end"
    var openPickerForSlot by remember { mutableStateOf<Map<Int, String?>>(emptyMap()) }

    // State for stations and selected station
    var stations by remember { mutableStateOf(listOf<Pair<String, String>>()) } // Pair<id, name>
    var selectedStation by remember { mutableStateOf<Pair<String, String>?>(null) }
    var stationDropdownExpanded by remember { mutableStateOf(false) }
    var isStationsLoading by remember { mutableStateOf(true) }
    val baseUrl = AppConstants.BASE_URL

    // Fetch stations on first composition
    LaunchedEffect(token) {
        try {
            val stationList = withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${baseUrl}stations")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "[]"
                if (response.isSuccessful) {
                    Log.d("ScheduleCreateScreen", "Stations fetched: $responseBody")
                    val jsonArray = JSONArray(responseBody)
                    val tempList = mutableListOf<Pair<String, String>>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val name = obj.optString("name", null)
                        val id = obj.optString("id", null)
                        if (!name.isNullOrBlank() && !id.isNullOrBlank()) {
                            tempList.add(id to name)
                        }
                    }
                    tempList
                } else {
                    Log.e("ScheduleCreateScreen", "Request failed: ${response.code} ${response.message}")
                    emptyList()
                }
            }
            stations = stationList
        } catch (e: Exception) {
            Log.e("ScheduleCreateScreen", "Exception: ${Log.getStackTraceString(e)}")
        }
        isStationsLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Schedule") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                OutlinedTextField(
                    value = selectedStation?.second ?: "",
                    onValueChange = {},
                    label = { Text("Station") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.clickable {
                            stationDropdownExpanded = true
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            stationDropdownExpanded = true
                        }
                )
                DropdownMenu(
                    expanded = stationDropdownExpanded,
                    onDismissRequest = { stationDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary))
                ) {
                    if (isStationsLoading) {
                        DropdownMenuItem(onClick = {}, enabled = false, text = { Text("Loading stations...") })
                    } else if (stations.isEmpty()) {
                        DropdownMenuItem(onClick = {}, enabled = false, text = { Text("No stations available") })
                    } else {
                        stations.forEach { station ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedStation = station
                                    stationDropdownExpanded = false
                                },
                                text = { Text(station.second) }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // Calendar date picker
            val datePickerDialog = remember {
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val m = (month + 1).toString().padStart(2, '0')
                        val d = dayOfMonth.toString().padStart(2, '0')
                        date = "$year-$m-$d"
                    },
                    2025, 9, 7 // Default to today (October 7, 2025)
                )
            }
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Date (YYYY-MM-DD)") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        datePickerDialog.show()
                    }
            )
            Spacer(Modifier.height(12.dp))
            Text("Slots", style = MaterialTheme.typography.titleMedium)
            slots.forEachIndexed { idx, slot ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = slot.startTime,
                        onValueChange = {},
                        label = { Text("Start Time") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable {
                            openPickerForSlot = openPickerForSlot.toMutableMap().apply { put(idx, "start") }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = slot.endTime,
                        onValueChange = {},
                        label = { Text("End Time") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable {
                            openPickerForSlot = openPickerForSlot.toMutableMap().apply { put(idx, "end") }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    // Switch for isAvailable
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Available")
                        Switch(
                            checked = slot.isAvailable,
                            onCheckedChange = { checked ->
                                slots = slots.toMutableList().apply { set(idx, slot.copy(isAvailable = checked)) }
                            }
                        )
                    }
                    IconButton(onClick = {
                        slots = slots.toMutableList().apply { removeAt(idx) }
                        openPickerForSlot = openPickerForSlot.toMutableMap().apply { remove(idx) }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Slot")
                    }
                }
                // Show only one picker per slot
                if (openPickerForSlot[idx] == "start") {
                    val slotStartPickerDialog = android.app.TimePickerDialog(
                        LocalContext.current,
                        { _, hour, minute ->
                            val h = hour.toString().padStart(2, '0')
                            val m = minute.toString().padStart(2, '0')
                            slots = slots.toMutableList().apply { set(idx, slot.copy(startTime = "$h:$m")) }
                            openPickerForSlot = openPickerForSlot.toMutableMap().apply { put(idx, null) }
                        },
                        8, 0, true
                    )
                    LaunchedEffect(openPickerForSlot[idx]) {
                        if (openPickerForSlot[idx] == "start") slotStartPickerDialog.show()
                    }
                } else if (openPickerForSlot[idx] == "end") {
                    val slotEndPickerDialog = android.app.TimePickerDialog(
                        LocalContext.current,
                        { _, hour, minute ->
                            val h = hour.toString().padStart(2, '0')
                            val m = minute.toString().padStart(2, '0')
                            val newEndTime = "$h:$m"
                            if (!isSlotTimeValid(slot.startTime, newEndTime)) {
                                showEndTimeErrorDialog = true
                                errorDialogSlotIdx = idx
                            } else {
                                slots = slots.toMutableList().apply { set(idx, slot.copy(endTime = newEndTime)) }
                                openPickerForSlot = openPickerForSlot.toMutableMap().apply { put(idx, null) }
                            }
                        },
                        9, 0, true
                    )
                    LaunchedEffect(openPickerForSlot[idx]) {
                        if (openPickerForSlot[idx] == "end") slotEndPickerDialog.show()
                    }
                }
                // Show Compose error dialog if needed
                if (showEndTimeErrorDialog && errorDialogSlotIdx == idx) {
                    AlertDialog(
                        onDismissRequest = { showEndTimeErrorDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showEndTimeErrorDialog = false }) { Text("OK") }
                        },
                        title = { Text("Invalid End Time") },
                        text = { Text("End time must be after start time.") }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = { slots = slots + SlotInput() }, modifier = Modifier.align(Alignment.End)) { Text("Add Slot") }
            Spacer(Modifier.height(24.dp))
            var showError by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            Button(onClick = {
                coroutineScope.launch {
                    // Validate slot times before API call
                    val invalidSlotIdx = slots.indexOfFirst { !isSlotTimeValid(it.startTime, it.endTime) }
                    if (invalidSlotIdx != -1) {
                        resultMessage = "Error: Slot ${invalidSlotIdx + 1} end time must be after start time."
                        return@launch
                    }
                    isLoading = true
                    resultMessage = ""
                    showError = false
                    // Build JSON payload
                    val json = JSONObject()
                    json.put("stationId", selectedStation?.first ?: "")
                    json.put("date", date)
                    val slotsArray = JSONArray()
                    slots.forEach {
                        val slotObj = JSONObject()
                        slotObj.put("start", "${it.startTime}:00")     // Changed from "startTime"
                        slotObj.put("end", "${it.endTime}:00")         // Changed from "endTime"
                        slotObj.put("available", it.isAvailable)        // Changed from "isAvailable"
                        slotObj.put("capacity", 1)                     // Added required capacity field
                        slotsArray.put(slotObj)
                    }
                    json.put("slots", slotsArray)
                    val client = OkHttpClient()
                    val body = RequestBody.create("application/json".toMediaType(), json.toString())
                    val request = Request.Builder()
                        .url("${baseUrl}schedules")
                        .put(body) // Use POST for creation
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    try {
                        var responseBody = ""
                        val response = withContext(Dispatchers.IO) {
                            val resp = client.newCall(request).execute()
                            responseBody = resp.body?.string() ?: ""
                            resp
                        }
                        if (response.isSuccessful) {
                            resultMessage = "Schedule created successfully!"
                            // Optionally clear form
                            stationId = ""
                            date = ""
                            slots = listOf()
                        } else {
                            resultMessage = "Failed: ${response.code} ${response.message}\n${responseBody}"
                            Log.e("ScheduleCreateScreen", "API error: ${response.code} ${response.message} Body: $responseBody")
                        }
                    } catch (e: Exception) {
                        resultMessage = "Error: ${e.localizedMessage}"
                        Log.e("ScheduleCreateScreen", "Exception: ${Log.getStackTraceString(e)}")
                    }
                    isLoading = false
                }
            }, enabled = !isLoading && selectedStation != null && date.isNotBlank() && slots.isNotEmpty() && slots.all { it.startTime.isNotBlank() && it.endTime.isNotBlank() }, modifier = Modifier.fillMaxWidth()) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Create Schedule")
            }

            // Show error if button is disabled and user tries to click
            LaunchedEffect(!isLoading && !(!isLoading && selectedStation != null && date.isNotBlank() && slots.isNotEmpty() && slots.all { it.startTime.isNotBlank() && it.endTime.isNotBlank() })) {
                showError = true
            }
            if (resultMessage.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(resultMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Helper data class for slot input
data class SlotInput(
    val startTime: String = "",
    val endTime: String = "",
    val isAvailable: Boolean = true
)

fun isSlotTimeValid(start: String, end: String): Boolean {
    // Assumes format "HH:mm"
    val startParts = start.split(":")
    val endParts = end.split(":")
    if (startParts.size != 2 || endParts.size != 2) return false
    val startMinutes = startParts[0].toIntOrNull()?.times(60)?.plus(startParts[1].toIntOrNull() ?: 0) ?: return false
    val endMinutes = endParts[0].toIntOrNull()?.times(60)?.plus(endParts[1].toIntOrNull() ?: 0) ?: return false
    return endMinutes > startMinutes
}