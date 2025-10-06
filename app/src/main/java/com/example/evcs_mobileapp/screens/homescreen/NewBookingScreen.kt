package com.example.evcs_mobileapp.screens.homescreen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcs_mobileapp.viewmodel.BookingViewModel
import androidx.navigation.NavController
import com.example.evcs_mobileapp.model.StationDto
import com.example.evcs_mobileapp.model.ScheduleSlotDto
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.example.evcs_mobileapp.db.AppDatabase
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun NewBookingScreen(navController: NavController, bookingViewModel: BookingViewModel) {
    val station = bookingViewModel.selectedStation
    val date = bookingViewModel.selectedDate
    val slot = bookingViewModel.selectedSlot
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
    val nic = prefs.getString("nic", "") ?: ""
    var bookingResult by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Theme colors
    val primaryGreen = Color(0xFF4CAF50)
    val darkGreen = Color(0xFF388E3C)
    val lightGreen = Color(0xFFE8F5E9)
    val accentBlue = Color(0xFF2196F3)
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF666666)

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
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (station == null || date == null || slot == null) {
            // Error State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Color(0xFFFFEBEE),
                                    RoundedCornerShape(40.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Text(
                            "Booking Data Missing",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Text(
                            "Please select a time slot from the schedules screen to proceed with your booking.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Back to Schedules",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        } else {
            // Main Booking Screen
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = primaryGreen,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Column {
                            Text(
                                "Confirm Booking",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Review your reservation details",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Station Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(lightGreen, RoundedCornerShape(12.dp)),
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

                                Column {
                                    Text(
                                        "Charging Station",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        station.name ?: "Unknown Station",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                }
                            }

                            if (!station.address.isNullOrBlank() && station.address != "null") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFFF5F5F5),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        station.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Booking Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Booking Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )

                            // Date
                            BookingDetailRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Date",
                                value = date,
                                iconColor = accentBlue
                            )

                            Divider(color = Color(0xFFEEEEEE))

                            // Time Slot
                            BookingDetailRow(
                                icon = Icons.Default.Schedule,
                                label = "Time Slot",
                                value = "${slot.start} - ${slot.end}",
                                iconColor = Color(0xFFFF9800)
                            )

                            Divider(color = Color(0xFFEEEEEE))

                            // Capacity
                            BookingDetailRow(
                                icon = Icons.Default.Battery5Bar,
                                label = "Slot Capacity",
                                value = "${slot.capacity} vehicles",
                                iconColor = Color(0xFF9C27B0)
                            )

                            // Status Badge
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = lightGreen
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = darkGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "This slot is available for booking",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = darkGreen
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Action Buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                isProcessing = true
                                scope.launch {
                                    val client = OkHttpClient()
                                    val json = JSONObject().apply {
                                        put("nic", user?.nic)
                                        put("stationId", station.id)
                                        put("date", date)
                                        put("start", slot.start)
                                        put("end", slot.end)
                                    }
                                    val body = json.toString().toRequestBody("application/json".toMediaType())
                                    val request = Request.Builder()
                                        .url("http://10.0.2.2:5132/api/Bookings")
                                        .post(body)
                                        .addHeader("Content-Type", "application/json")
                                        .build()
                                    try {
                                        val response = client.newCall(request).execute()
                                        if (response.isSuccessful) {
                                            bookingResult = "Booking successful!"
                                            showSuccessDialog = true
                                        } else {
                                            bookingResult = "Booking failed: ${response.message}"
                                        }
                                    } catch (e: Exception) {
                                        bookingResult = "Network error: ${e.localizedMessage}"
                                    }
                                    isProcessing = false
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Confirm Booking",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        bookingResult?.let {
                            Text(
                                it,
                                color = if (it.contains("successful")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen
                    )
                ) {
                    Text("Done")
                }
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Booking Confirmed!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Your charging slot has been successfully reserved.")
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun BookingDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    iconColor.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF999999),
                fontSize = 12.sp
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )
        }
    }
}