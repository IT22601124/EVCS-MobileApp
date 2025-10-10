package com.example.evcs_mobileapp.screens.homescreen

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.evcs_mobileapp.AppConstants
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

// Reuse BookingStatus and BookingItem from your existing code

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovedBookingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)

    val db = remember(context) {
        androidx.room.Room.databaseBuilder(
            context,
            com.example.evcs_mobileapp.db.AppDatabase::class.java,
            "evcs_db"
        ).build()
    }
    val dao = db.authResponseDao()
    var user by remember { mutableStateOf<com.example.evcs_mobileapp.db.AuthResponseEntity?>(null) }
    LaunchedEffect(Unit) {
        user = dao.getUser()
    }
    val nic = user?.nic ?: ""

    var bookings by remember { mutableStateOf<List<BookingItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(nic) {
        if (nic.isNotBlank() && !token.isNullOrBlank()) {
            loading = true
            error = ""
            scope.launch {
                try {
                    val client = okhttp3.OkHttpClient()
                    val url = "${AppConstants.BASE_URL}bookings/by-owner/$nic"
                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                    val responseBody = withContext(Dispatchers.IO) { response.body?.string() ?: "[]" }
                    if (response.isSuccessful) {
                        val type = object : com.google.gson.reflect.TypeToken<List<BookingItem>>() {}.type
                        val fetchedBookings: List<BookingItem> = com.google.gson.Gson().fromJson(responseBody, type) ?: emptyList()
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
                        // Reload from SQL after saving
                        val localBookings = withContext(Dispatchers.IO) {
                            db.bookingDao().getBookingsByNic(nic)
                        }
                        bookings = localBookings.map { entity ->
                            BookingItem(
                                id = entity.id,
                                nic = entity.nic,
                                ownerName = entity.ownerName,
                                ownerEmail = entity.ownerEmail,
                                ownerPhone = entity.ownerPhone,
                                stationId = entity.stationId,
                                stationName = entity.stationName,
                                stationAddress = entity.stationAddress,
                                stationType = entity.stationType,
                                date = entity.date,
                                start = entity.start,
                                end = entity.end,
                                status = entity.status,
                                qrToken = entity.qrToken,
                                createdAt = entity.createdAt,
                                updatedAt = entity.updatedAt
                            )
                        }
                    } else {
                        error = "Failed to fetch bookings: ${response.code} - $responseBody"
                    }
                } catch (e: Exception) {
                    error = "Error: ${e.localizedMessage}"
                }
                loading = false
            }
        } else {
            error = when {
                token.isNullOrBlank() -> "Not logged in. Please login first."
                nic.isBlank() -> "User NIC not found. Please login again."
                else -> "Unknown error"
            }
        }
    }

    val approvedBookings = remember(bookings) {
        bookings.filter { it.status == BookingStatus.Approved.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approved Bookings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.EventAvailable, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error.isNotBlank() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
            approvedBookings.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No approved bookings")
                }
            }
            else -> {
                LazyColumn(Modifier.padding(padding)) {
                    items(approvedBookings) { item ->
                        ApprovedBookingItemRow(item)
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovedBookingItemRow(item: BookingItem) {
    val context = LocalContext.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val qrToken = item.qrToken

    // Generate QR bitmap when qrToken changes
    LaunchedEffect(qrToken) {
        qrToken?.let {
            val size = 512 // px
            val bits = QRCodeWriter().encode(it, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bits.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            qrBitmap = bmp
        }
    }

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
                    label = { Text("Approved") }
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
            if (qrToken != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "QR Token: $qrToken",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                qrBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val filename = "qr_${item.id}.png"
                        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        val file = File(picturesDir, filename)
                        try {
                            val out: OutputStream = FileOutputStream(file)
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                            out.close()
                            Toast.makeText(context, "QR code saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_LONG).show()
                        }
                    }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Download QR Code")
                    }
                }
            }
        }
    }
}
