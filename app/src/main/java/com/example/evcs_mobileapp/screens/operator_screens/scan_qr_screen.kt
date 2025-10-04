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
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanQrScreen(navController: NavHostController) {
    val context = LocalContext.current
    var scannedData by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

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
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Start QR Scan")
            }
            Spacer(Modifier.height(16.dp))
            if (scannedData.isNotBlank()) {
                Text("Scanned Data: $scannedData")
                Button(onClick = {
                    // TODO: Call API to verify booking
                    resultMessage = "Booking verified (mock)"
                }) {
                    Text("Verify Booking")
                }
            }
            if (resultMessage.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(resultMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

