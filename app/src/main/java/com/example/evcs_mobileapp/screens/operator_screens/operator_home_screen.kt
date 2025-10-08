package com.example.evcs_mobileapp.screens.operator_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorHomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("evcs_prefs", android.content.Context.MODE_PRIVATE)
    val fullName = prefs.getString("fullName", "Operator") ?: "Operator"
    val email = prefs.getString("email", "operator@evcs.com") ?: "operator@evcs.com"
    val role = prefs.getString("role", "Operator") ?: "Operator"

    var showMenu by remember { mutableStateOf(false) }
    var selectedMenuIndex by remember { mutableStateOf(0) }

    val menuItems = listOf(
        Triple("Scan QR", Icons.Filled.QrCodeScanner, "scan_qr"),
        Triple("Pending Bookings", Icons.Filled.Schedule, "pending_bookings"),
        Triple("Manage Bookings", Icons.Filled.ManageAccounts, "manage_bookings"),
        Triple("Active Bookings", Icons.Filled.List, "active_bookings"),
        Triple("Logout", Icons.Filled.Logout, "operator_logout")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Operator Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(28.dp)
                            .clickable { showMenu = !showMenu }
                    )
                },
                actions = {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(28.dp)
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
        ) {
            // 🔹 Full-height NavigationRail (sidebar)
            if (showMenu) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(110.dp)
                        .padding(vertical = 8.dp)
                ) {
                    menuItems.forEachIndexed { index, item ->
                        NavigationRailItem(
                            selected = selectedMenuIndex == index,
                            onClick = {
                                selectedMenuIndex = index
                                navController.navigate(item.third)
                                showMenu = false
                            },
                            icon = { Icon(item.second, contentDescription = item.first) },
                            label = {
                                Text(
                                    item.first,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = if (item.first == "Logout") Color.White else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (item.first == "Logout") Color.White else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (item.first == "Logout") Color(0xFFD32F2F) else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // 🔹 Dashboard Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
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
                        Text(
                            text = email,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DashboardButton("Create Schedule", Icons.Filled.CalendarMonth) {
                        navController.navigate("schedule_create")
                    }
                    DashboardButton("Pending Bookings", Icons.Filled.Schedule) {
                        navController.navigate("pending_bookings")
                    }
                    DashboardButton("Manage Bookings", Icons.Filled.ManageAccounts) {
                        navController.navigate("manage_bookings")
                    }
                    DashboardButton("Active Bookings", Icons.Filled.List) {
                        navController.navigate("active_bookings")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
