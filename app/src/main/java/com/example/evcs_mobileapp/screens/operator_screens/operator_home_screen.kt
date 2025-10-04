package com.example.evcs_mobileapp.screens.operator_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorHomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val fullName = prefs.getString("fullName", "Operator") ?: "Operator"
    val email = prefs.getString("email", "operator@evcs.com") ?: "operator@evcs.com"
    val role = prefs.getString("role", "Operator") ?: "Operator"

    val showMenu = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Dashboard", style = MaterialTheme.typography.headlineMedium , fontSize = 18.sp)
                },
                navigationIcon = {
                    Icon(
                        Icons.Filled.EvStation,
                        contentDescription = "EV Station",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showMenu.value = !showMenu.value }
                    )
                },
                actions = {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.navigate("operator_profile") },
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 🔹 Full-height NavigationRail
            if (showMenu.value) {
                var selectedMenuIndex by remember { mutableStateOf(0) }
                val menuItems = listOf(
                    Triple("Scan QR", Icons.Filled.QrCodeScanner, "scan_qr"),
                    Triple("Active Bookings", Icons.Filled.List, "active_bookings"),
                    Triple("Logout", Icons.Filled.Logout, "operator_logout")
                )

                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                ) {
                    menuItems.forEachIndexed { index, item ->
                        NavigationRailItem(
                            selected = selectedMenuIndex == index,
                            onClick = {
                                selectedMenuIndex = index
                                navController.navigate(item.third)
                                showMenu.value = false // Hide menu after navigation
                            },
                            icon = { Icon(item.second, contentDescription = item.first) },
                            label = { Text(item.first) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = if (item.first == "Logout") Color.White else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (item.first == "Logout") Color.White else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (item.first == "Logout") Color(0xFFD32F2F) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // 🔹 Main Dashboard Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Welcome, $fullName!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Role: $role",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Button to navigate to ScheduleCreateScreen
                Button(
                    onClick = { navController.navigate("schedule_create") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Create Schedule", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
