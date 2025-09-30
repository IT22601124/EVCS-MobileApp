package com.example.evcs_mobileapp.screens.homescreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.ZonedDateTime

enum class BookingStatus { Pending, Approved, Cancelled, Completed, Canceled }
data class BookingItem(
    val id: String,
    val station: String,
    val startIso: String,
    val durationMin: Int,
    val status: BookingStatus
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)
    val nic = prefs.getString("nic", "") ?: ""
    var bookings by remember { mutableStateOf<List<BookingItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(nic) {
        if (nic.isNotBlank() && !token.isNullOrBlank()) {
            loading = true
            error = ""
            val client = OkHttpClient()
            val url = "http://10.0.2.2:5132/api/bookings/by-owner/$nic"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()
            try {
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    val type = object : com.google.gson.reflect.TypeToken<List<BookingItem>>() {}.type
                    bookings = Gson().fromJson(body, type)
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
        topBar = { TopAppBar(title = { Text("My Bookings") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("new_booking") }) {
                Icon(Icons.Default.Add, contentDescription = "Create New Booking")
            }
        }
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
                Text("No upcoming bookings")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(bookings) { item ->
                    if (item.status == BookingStatus.Pending || item.status == BookingStatus.Approved) {
                        BookingItemRow(item, navController)
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingItemRow(item: BookingItem, navController: NavController) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.station, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(item.status.name) })
            }
            Spacer(Modifier.height(6.dp))
            val whenText = runCatching { ZonedDateTime.parse(item.startIso).toLocalDateTime().toString().replace('T', ' ') }
                .getOrElse { item.startIso }
            Text("${item.id}  ${item.durationMin} min")
            Text(whenText, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (item.status == BookingStatus.Pending || item.status == BookingStatus.Approved) {
                    Button(onClick = { /* Edit logic if >=12h before */ }) {
                        Text("Edit")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { /* Cancel logic if >=12h before */ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
