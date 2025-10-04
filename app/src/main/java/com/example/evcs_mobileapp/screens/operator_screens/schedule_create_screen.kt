package com.example.evcs_mobileapp.screens.operator_screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""

    // Dynamic slots
    var slots by remember { mutableStateOf(listOf<SlotInput>()) }

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
            OutlinedTextField(value = stationId, onValueChange = { stationId = it }, label = { Text("Station ID") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Text("Slots", style = MaterialTheme.typography.titleMedium)
            slots.forEachIndexed { idx, slot ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = slot.start, onValueChange = { slots = slots.toMutableList().apply { set(idx, slot.copy(start = it)) } }, label = { Text("Start") }, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(value = slot.end, onValueChange = { slots = slots.toMutableList().apply { set(idx, slot.copy(end = it)) } }, label = { Text("End") }, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(value = slot.capacity, onValueChange = { slots = slots.toMutableList().apply { set(idx, slot.copy(capacity = it)) } }, label = { Text("Capacity") }, modifier = Modifier.width(80.dp))
                    IconButton(onClick = { slots = slots.toMutableList().apply { removeAt(idx) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Slot")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = { slots = slots + SlotInput() }, modifier = Modifier.align(Alignment.End)) { Text("Add Slot") }
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                isLoading = true
                resultMessage = ""
                // Build JSON payload
                val json = JSONObject()
                json.put("stationId", stationId)
                json.put("date", date)
                val slotsArray = JSONArray()
                slots.forEach {
                    val slotObj = JSONObject()
                    slotObj.put("start", it.start)
                    slotObj.put("end", it.end)
                    slotObj.put("available", true)
                    slotObj.put("capacity", it.capacity.toIntOrNull() ?: 1)
                    slotsArray.put(slotObj)
                }
                json.put("slots", slotsArray)
                val client = OkHttpClient()
                val body = RequestBody.create("application/json".toMediaType(), json.toString())
                val request = Request.Builder()
                    .url("http://10.0.2.2:5132/api/schedules")
                    .put(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        resultMessage = "Schedule created successfully!"
                        // Optionally clear form
                        stationId = ""
                        date = ""
                        slots = listOf()
                    } else {
                        resultMessage = "Failed: ${response.code} ${response.message}"
                    }
                } catch (e: Exception) {
                    resultMessage = "Error: ${e.localizedMessage}"
                }
                isLoading = false
            }, enabled = !isLoading && stationId.isNotBlank() && date.isNotBlank() && slots.isNotEmpty() && slots.all { it.start.isNotBlank() && it.end.isNotBlank() && it.capacity.isNotBlank() }, modifier = Modifier.fillMaxWidth()) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Create Schedule")
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
    val start: String = "",
    val end: String = "",
    val capacity: String = ""
)
