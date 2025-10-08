package com.example.evcs_mobileapp

import com.example.evcs_mobileapp.screens.login_screens.LoginScreen
import com.example.evcs_mobileapp.screens.homescreen.OwnerHomeScreen
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.evcs_mobileapp.screens.homescreen.BookingHistoryScreen
import com.example.evcs_mobileapp.screens.homescreen.MyBookingsScreen
import com.example.evcs_mobileapp.screens.homescreen.NewBookingScreen
import com.example.evcs_mobileapp.screens.homescreen.StationsMapScreen
import com.example.evcs_mobileapp.screens.login_screens.SignupScreen
import com.example.evcs_mobileapp.screens.walkthrough.walkthrough_1
import com.example.evcs_mobileapp.screens.walkthrough.walkthrough_2
import com.example.evcs_mobileapp.screens.walkthrough.walkthrough_3
import com.example.evcs_mobileapp.screens.homescreen.ProfileScreen
import com.example.evcs_mobileapp.screens.homescreen.StationsAndSchedulesScreen
import com.example.evcs_mobileapp.screens.operator_screens.OperatorHomeScreen
import com.example.evcs_mobileapp.screens.operator_screens.ScanQrScreen
import com.example.evcs_mobileapp.screens.operator_screens.ActiveBookingsScreen
import com.example.evcs_mobileapp.screens.operator_screens.OperatorLogoutScreen
import com.example.evcs_mobileapp.screens.operator_screens.ScheduleCreateScreen
import com.example.evcs_mobileapp.viewmodel.BookingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Helper function to check user role from server
suspend fun getUserRoleFromServer(token: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:5132/api/auth/me") // Adjust endpoint if needed
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                val json = JSONObject(body ?: "")
                return@withContext json.optString("role", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val bookingViewModel: BookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current
    var isFirstLaunch by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf<String?>(null) }


    // Check SharedPreferences for first launch
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
        isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            prefs.edit().putBoolean("is_first_launch", false).apply()
        }
    }

    val prefs = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)
    val role = prefs.getString("role", null)

    Log.d("MainNavHost", "Token: $token, Role: $role, isFirstLaunch: $isFirstLaunch")
    LaunchedEffect(token, role, isFirstLaunch) {
        if (token.isNullOrEmpty()) {
            startDestination = if (isFirstLaunch) "walkthrough_1" else "login"
        } else if (!role.isNullOrEmpty()) {
            startDestination = when (role) {
                "Owner", "owner" -> "owner_home"
                "Backoffice", "admin", "operator" -> "operator_home"
                else -> "login"
            }
        } else {
            // Role missing, fetch from server
            val fetchedRole = getUserRoleFromServer(token)
            if (!fetchedRole.isNullOrEmpty()) {
                prefs.edit().putString("role", fetchedRole).apply()
                startDestination = when (fetchedRole) {
                    "Owner", "owner" -> "owner_home"
                    "Backoffice", "admin", "Operator" -> "operator_home"
                    else -> "login"
                }
            } else {
                startDestination = "login"
            }
        }
    }

    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable("walkthrough_1") { walkthrough_1(navController) }
            composable("walkthrough_2") { walkthrough_2(navController) }
            composable("walkthrough_3") { walkthrough_3(navController) }
            composable("login") {LoginScreen(navController) }
            composable("signup") { SignupScreen(navController) }
            composable("owner_home") { OwnerHomeScreen(navController) }
            composable("my_bookings") { MyBookingsScreen(navController) }
            composable("new_booking") { NewBookingScreen(navController, bookingViewModel) }
            composable("booking_history") { BookingHistoryScreen(navController) }
            composable("stations_map") { StationsMapScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("stations_schedules") { StationsAndSchedulesScreen(navController, bookingViewModel) }
            composable("operator_home") { OperatorHomeScreen(navController) }
            composable("scan_qr") { ScanQrScreen(navController) }
            composable("active_bookings") { ActiveBookingsScreen(navController) }
            composable("operator_logout") { OperatorLogoutScreen(navController) }
            composable("operator_profile") { com.example.evcs_mobileapp.screens.operator_screens.OperatorProfileScreen(navController) }
            composable(route = "schedule_create"){ ScheduleCreateScreen(navController) }
            composable(route = "pending_bookings"){ com.example.evcs_mobileapp.screens.operator_screens.PendingBookingsScreen(navController) }
        }
    }
}
