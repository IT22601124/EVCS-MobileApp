package com.example.evcs_mobileapp.screens.homescreen

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcs_mobileapp.AppConstants
import com.example.evcs_mobileapp.db.AppDatabase
import com.example.evcs_mobileapp.model.ScheduleSlotDto
import com.example.evcs_mobileapp.model.StationDto
import com.example.evcs_mobileapp.viewmodel.BookingViewModel
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.navigation.NavController
import androidx.room.Room
import androidx.compose.ui.graphics.asImageBitmap
import android.widget.Toast
import android.os.Build
import android.provider.MediaStore
import android.content.ContentValues

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
    var showFailureDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
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
                                    Log.d("NewBookingScreen", "Booking JSON: $json")
                                    val prefs = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
                                    val token = prefs.getString("token", "") ?: ""
                                    val request = Request.Builder()
                                        .url("${AppConstants.BASE_URL}bookings")
                                        .post(body)
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("Authorization", "Bearer $token")
                                        .build()
                                    try {
                                        Log.d("NewBookingScreen", "Sending booking request to: ${AppConstants.BASE_URL}bookings")
                                        val response = withContext(Dispatchers.IO) {
                                            client.newCall(request).execute()
                                        }
                                        Log.d("NewBookingScreen", "Response code: ${response.code}")
                                        if (response.isSuccessful) {
                                            bookingResult = "Booking successful!"
                                            navController.popBackStack()
                                          //  showSuccessDialog = true
                                            // Generate QR code with booking info
                                            val qrContent = "NIC: ${user?.nic}\nStation: ${station.name}\nDate: $date\nSlot: ${slot.start} - ${slot.end}"
                                            val encoder = BarcodeEncoder()
                                            qrBitmap = encoder.encodeBitmap(qrContent, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400)
                                        } else {
                                            bookingResult = "Booking failed: ${response.message}"
                                            showFailureDialog = true
                                        }
                                    } catch (e: Exception) {
                                        bookingResult = "Network error: ${e.localizedMessage}"
                                    }
                                    isProcessing = false
                                }
                            },
                            enabled = !isProcessing
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
                Row {
                    Button(
                        onClick = {
                            qrBitmap?.let { saveQrToGallery(context, it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                    ) {
                        Text("Download QR")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                    ) {
                        Text("Done")
                    }
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your charging slot has been successfully reserved.")
                    Spacer(Modifier.height(16.dp))
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Booking QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Scan this QR code at the station.", fontSize = 14.sp)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Failure Dialog
    if (showFailureDialog) {
        AlertDialog(
            onDismissRequest = { showFailureDialog = false },
            confirmButton = {
                Button(
                    onClick = { showFailureDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Close")
                }
            },
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Booking Failed", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("No slot available.")
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

fun saveQrToGallery(context: Context, bitmap: Bitmap) {
    val filename = "EVCS_Booking_QR_${System.currentTimeMillis()}.png"
    val fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/EVCS_QR")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { context.contentResolver.openOutputStream(it) }
    } else {
        val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES).toString()
        val file = java.io.File(imagesDir, filename)
        java.io.FileOutputStream(file)
    }
    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        Toast.makeText(context, "QR code saved to gallery", Toast.LENGTH_SHORT).show()
    } ?: Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_SHORT).show()
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