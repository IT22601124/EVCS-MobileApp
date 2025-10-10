package com.example.evcs_mobileapp.screens.operator_screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.evcs_mobileapp.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import com.example.evcs_mobileapp.model.BookingItem

data class BookingDto(
    val id: String,
    val nic: String?,
    val ownerName: String?,
    val ownerEmail: String?,
    val ownerPhone: String?,
    val stationId: String?,
    val stationName: String?,
    val stationAddress: String?,
    val stationType: String?,
    val date: String?,
    val start: String?,
    val end: String?,
    val status: String?,
    val qrToken: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PendingBookingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""
    val assignedStationId = prefs.getString("assignedStationId", "") ?: ""
    val today = "2025-10-10"
    var bookings by remember { mutableStateOf(listOf<BookingItem>()) }
    var resultMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val baseUrl: String = AppConstants.BASE_URL
    val scope = rememberCoroutineScope()

    LaunchedEffect(assignedStationId, today) {
        if (token.isNotBlank() && assignedStationId.isNotBlank()) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val client = OkHttpClient()
                val url = "${baseUrl}bookings/by-station/$assignedStationId"
                Log.d("PendingBookingsScreen", "Fetching bookings: $url")

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        Log.d("PendingBookingsScreen", "Response: $body")
                        val arr = JSONArray(body ?: "[]")
                        val items = mutableListOf<BookingItem>()

                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val status = obj.optString("status")
                            // Only add pending bookings
                            if (status == "Pending") {
                                items.add(
                                    BookingItem(
                                        id = obj.optString("id"),
                                        nic = obj.optString("nic"),
                                        ownerName = obj.optString("ownerName"),
                                        ownerPhone = obj.optString("ownerPhone"),
                                        stationId = obj.optString("stationId"),
                                        date = obj.optString("date"),
                                        start = obj.optString("start"),
                                        end = obj.optString("end"),
                                        status = status
                                    )
                                )
                            }
                        }
                        bookings = items
                        Log.d("PendingBookingsScreen", "Loaded ${items.size} pending bookings")
                    } else {
                        resultMessage = "Failed to fetch bookings: ${response.code}"
                        Log.e("PendingBookingsScreen", resultMessage)
                    }
                } catch (e: Exception) {
                    resultMessage = "Error: ${e.localizedMessage}"
                    Log.e("PendingBookingsScreen", "Error fetching bookings", e)
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pending Bookings") }) }
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
            } else if (bookings.isEmpty()) {
                Text("No pending bookings found")
            } else {
                bookings.forEach { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Customer: ${booking.ownerName}", style = MaterialTheme.typography.titleMedium)
                            Text("NIC: ${booking.nic}", style = MaterialTheme.typography.bodyMedium)
                            Text("Phone: ${booking.ownerPhone}", style = MaterialTheme.typography.bodyMedium)
                            Text("Date: ${booking.date}", style = MaterialTheme.typography.bodyMedium)
                            Text("Time: ${booking.start} - ${booking.end}", style = MaterialTheme.typography.bodyMedium)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            withContext(Dispatchers.Main) {
                                                isLoading = true
                                                resultMessage = ""
                                            }

                                            val client = OkHttpClient()
                                            val approveUrl = "${baseUrl}bookings/${booking.id}/approve"

                                            val approveRequest = Request.Builder()
                                                .url(approveUrl)
                                                .post(RequestBody.create("application/json".toMediaType(), "{}"))
                                                .addHeader("Authorization", "Bearer $token")
                                                .addHeader("Content-Type", "application/json")
                                                .build()
                                            try {
                                                Log.d("PendingBookingsScreen", "Sending approve request to: $approveUrl")
                                                val response = client.newCall(approveRequest).execute()
                                                val responseBody = response.body?.string()
                                                Log.d("PendingBookingsScreen", "Approve response: ${response.code}, body: $responseBody")

                                                withContext(Dispatchers.Main) {
                                                    if (response.isSuccessful) {
                                                        resultMessage = "Booking approved successfully!"
                                                        // Remove the approved booking from the list
                                                        bookings = bookings.filter { it.id != booking.id }
                                                    } else {
                                                        resultMessage = "Failed to approve booking: ${response.code}"
                                                        Log.e("PendingBookingsScreen", "Approve failed: ${response.code}, $responseBody")
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("PendingBookingsScreen", "Approve error", e)
                                                withContext(Dispatchers.Main) {
                                                    resultMessage = "Error approving booking: ${e.localizedMessage}"
                                                }
                                            } finally {
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Approve")
                                }

                                Spacer(Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            isLoading = true
                                            val client = OkHttpClient()
                                            val cancelUrl = "${baseUrl}bookings/${booking.id}/cancel"
                                            val cancelRequest = Request.Builder()
                                                .url(cancelUrl)
                                                .put(RequestBody.create("application/json".toMediaType(), "{}"))
                                                .addHeader("Authorization", "Bearer $token")
                                                .build()

                                            try {
                                                val response = client.newCall(cancelRequest).execute()
                                                withContext(Dispatchers.Main) {
                                                    if (response.isSuccessful) {
                                                        resultMessage = "Booking cancelled!"
                                                        bookings = bookings.filter { it.id != booking.id }
                                                    } else {
                                                        resultMessage = "Failed to cancel: ${response.code}"
                                                    }
                                                    isLoading = false
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    resultMessage = "Error: ${e.localizedMessage}"
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
            if (resultMessage.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(resultMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
