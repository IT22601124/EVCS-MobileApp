package com.example.evcs_mobileapp.screens.operator_screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorCreateScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var stationId by remember { mutableStateOf("") } // Replace with dropdown for real station list
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Operator") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = stationId, onValueChange = { stationId = it }, label = { Text("Station ID") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                isLoading = true
                // TODO: Call API to create operator
                // On success: resultMessage = "Operator created successfully!"
                // On failure: resultMessage = "Failed to create operator."
                isLoading = false
            }, enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && username.isNotBlank() && password.isNotBlank() && stationId.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Create Operator")
            }
            if (resultMessage.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(resultMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

