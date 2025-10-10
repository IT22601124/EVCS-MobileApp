package com.example.evcs_mobileapp.screens.operator_screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.util.Log
import com.example.evcs_mobileapp.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class ScanResult(
    val id: String,
    val nic: String,
    val ownerName: String,
    val stationId: String,
    val stationName: String,
    val stationAddress: String, // Changed from address to stationAddress
    val date: String,
    val start: String,
    val end: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanQrScreen(navController: NavHostController) {
    val context = LocalContext.current
    var scannedData by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var manualQrToken by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var isFinalizingBooking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""
    val baseUrl = AppConstants.BASE_URL

    fun finalizeBooking(bookingId: String) {
        scope.launch {
            try {
                isFinalizingBooking = true
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("${baseUrl}bookings/$bookingId/finalize")
                        .post("".toRequestBody("application/json".toMediaType()))
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    val response = client.newCall(request).execute()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            resultMessage = "Booking finalized successfully"
                            showDialog = false
                            scanResult = null
                        } else {
                            resultMessage = "Failed to finalize booking: ${response.code}"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ScanQrScreen", "Error finalizing booking", e)
                withContext(Dispatchers.Main) {
                    resultMessage = "Error: ${e.localizedMessage}"
                }
            } finally {
                isFinalizingBooking = false
            }
        }
    }

    fun fetchBookingDetails(qrToken: String) {
        if (qrToken.isBlank()) {
            resultMessage = "Please enter a QR token"
            return
        }

        scope.launch {
            withContext(Dispatchers.Main) {
                isLoading = true
                resultMessage = ""
            }

            try {
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val jsonBody = JSONObject().apply {
                        put("qrToken", qrToken)
                    }.toString()

                    val request = Request.Builder()
                        .url("${baseUrl}bookings/scan")
                        .post(jsonBody.toRequestBody("application/json".toMediaType()))
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    Log.d("ScanQrScreen", "Making request with token: $qrToken")

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                            val json = JSONObject(responseBody)
                            scanResult = ScanResult(
                                id = json.getString("bookingId"),
                                nic = json.getString("nic"),
                                ownerName = json.getString("ownerName"),
                                stationId = json.getString("stationId"),
                                stationName = json.getString("stationName"),
                                stationAddress = json.getString("stationAddress"),
                                date = json.getString("date"),
                                start = json.getString("start"),
                                end = json.getString("end"),
                                status = json.getString("status")
                            )
                            resultMessage = "Booking verified successfully"
                            manualQrToken = ""
                            showDialog = true // Show dialog when booking is fetched
                        } else {
                            resultMessage = "Failed to verify booking: ${response.code}"
                            Log.e("ScanQrScreen", "API Error: ${response.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ScanQrScreen", "Error verifying booking", e)
                withContext(Dispatchers.Main) {
                    resultMessage = "Error: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { qrContent ->
            scannedData = qrContent
            fetchBookingDetails(qrContent)
        }
    }

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    if (showDialog && scanResult != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Booking Details") },
            text = {
                Column {
                    Text("Customer: ${scanResult?.ownerName}")
                    Text("NIC: ${scanResult?.nic}")
                    Text("Station: ${scanResult?.stationName}")
                    Text("Address: ${scanResult?.stationAddress}")
                    Text("Date: ${scanResult?.date}")
                    Text("Time: ${scanResult?.start} - ${scanResult?.end}")
                    Text("Status: ${scanResult?.status}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scanResult?.id?.let { finalizeBooking(it) }
                    },
                    enabled = !isFinalizingBooking
                ) {
                    if (isFinalizingBooking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Finalize Booking")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Scan Booking QR Code", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            // Add manual QR token input
            OutlinedTextField(
                value = manualQrToken,
                onValueChange = { manualQrToken = it },
                label = { Text("Enter QR Token") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            Button(
                onClick = { fetchBookingDetails(manualQrToken) },
                enabled = !isLoading && manualQrToken.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Details")
            }

            Spacer(Modifier.height(16.dp))
            Text("- OR -", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                when {
                    cameraPermissionState.status.isGranted -> {
                        Button(
                            onClick = {
                                barcodeLauncher.launch(ScanOptions().apply {
                                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    setPrompt("Scan QR Code")
                                    setBeepEnabled(true)
                                })
                            }
                        ) {
                            Text("Start QR Scan")
                        }
                    }
                    cameraPermissionState.status.shouldShowRationale -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Camera permission is needed to scan QR codes")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                Text("Request Permission")
                            }
                        }
                    }
                    else -> {
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Request Camera Permission")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (resultMessage.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = resultMessage,
                    color = if (resultMessage.startsWith("Error") || resultMessage.startsWith("Failed"))
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
