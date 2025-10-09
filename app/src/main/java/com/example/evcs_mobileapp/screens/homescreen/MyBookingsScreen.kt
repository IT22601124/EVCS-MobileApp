package com.example.evcs_mobileapp.screens.homescreen

import android.os.Build
import android.util.Log
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
import com.example.evcs_mobileapp.AppConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.ZonedDateTime
import androidx.room.Room
import com.example.evcs_mobileapp.db.AppDatabase

enum class BookingStatus { Pending, Approved, Cancelled, Completed, Canceled }
data class BookingItem(
    val id: String,
    val nic: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val stationId: String,
    val stationName: String,
    val stationAddress: String,
    val stationType: String,
    val date: String,
    val start: String,
    val end: String,
    val status: String,
    val qrToken: String?,
    val createdAt: String,
    val updatedAt: String?
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

    val db = remember(context) {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "evcs_db"
        ).build()
    }
    val dao = db.authResponseDao()
    var user by remember { mutableStateOf<com.example.evcs_mobileapp.db.AuthResponseEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        user = dao.getUser()
    }
    val nic = user?.nic ?: ""

    var bookings by remember { mutableStateOf<List<BookingItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("MyBookingsScreen", "Token: ${token?.take(20)}...")
        Log.d("MyBookingsScreen", "NIC: $nic")
        Log.d("MyBookingsScreen", "Base URL: ${AppConstants.BASE_URL}")
    }

    LaunchedEffect(nic) {
        if (nic.isNotBlank() && !token.isNullOrBlank()) {
            loading = true
            error = ""
            val client = OkHttpClient()
            val url = "${AppConstants.BASE_URL}bookings/by-owner/$nic"
            Log.d("MyBookingsScreen", "Fetching bookings from: $url")

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                val (response, responseBody) = withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "[]"
                    response to responseBody
                }
                Log.d("MyBookingsScreen", "Response code: ${response.code}")
                Log.d("MyBookingsScreen", "Response body: $responseBody")

                if (response.isSuccessful) {
                    val type = object : com.google.gson.reflect.TypeToken<List<BookingItem>>() {}.type
                    val fetchedBookings: List<BookingItem> = Gson().fromJson(responseBody, type) ?: emptyList()
                    bookings = fetchedBookings
                    Log.d("MyBookingsScreen", "Parsed ${bookings.size} bookings")
                    // Save to SQL
                    withContext(Dispatchers.IO) {
                        val entities = fetchedBookings.map { item ->
                            com.example.evcs_mobileapp.db.BookingEntity(
                                id = item.id,
                                nic = item.nic,
                                ownerName = item.ownerName,
                                ownerEmail = item.ownerEmail,
                                ownerPhone = item.ownerPhone,
                                stationId = item.stationId,
                                stationName = item.stationName,
                                stationAddress = item.stationAddress,
                                stationType = item.stationType,
                                date = item.date,
                                start = item.start,
                                end = item.end,
                                status = item.status,
                                qrToken = item.qrToken,
                                createdAt = item.createdAt,
                                updatedAt = item.updatedAt
                            )
                        }
                        db.bookingDao().insertBookings(entities)
                    }
                } else {
                    error = "Failed to fetch bookings: ${response.code} - $responseBody"
                    Log.e("MyBookingsScreen", error)
                }
            } catch (e: Exception) {
                error = "Error: ${e.localizedMessage}"
                Log.e("MyBookingsScreen", "Exception fetching bookings", e)
            }
            loading = false
        } else {
            error = when {
                token.isNullOrBlank() -> "Not logged in. Please login first."
                nic.isBlank() -> "User NIC not found. Please login again."
                else -> "Unknown error"
            }
            Log.e("MyBookingsScreen", error)
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
        Column(modifier = Modifier.padding(padding)) {


            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error.isNotBlank() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { navController.navigate("login") }) {
                                Text("Go to Login")
                            }
                        }
                    }
                }
                bookings.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No bookings found")
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { navController.navigate("new_booking") }) {
                                Text("Create Your First Booking")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn {
                        items(bookings) { item ->
                            if (item.status == BookingStatus.Pending.name ||
                                item.status == BookingStatus.Approved.name) {
                                BookingItemRow(item, navController)
                            }
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.stationName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(item.status) }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.stationAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            val whenText = "${item.date} ${item.start} - ${item.end}"
            Text(whenText, style = MaterialTheme.typography.bodyMedium)

            if (item.qrToken != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "QR Token: ${item.qrToken}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (item.status == BookingStatus.Pending.name ||
                    item.status == BookingStatus.Approved.name) {
                    Button(onClick = { /* Edit logic if >=12h before */ }) {
                        Text("Edit")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { /* Cancel logic if >=12h before */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}