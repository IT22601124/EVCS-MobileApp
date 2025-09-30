package com.example.evcs_mobileapp

import com.example.evcs_mobileapp.screens.login_screens.LoginScreen
import com.example.evcs_mobileapp.screens.homescreen.OwnerHomeScreen
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.evcs_mobileapp.screens.homescreen.BookingHistoryScreen
import com.example.evcs_mobileapp.screens.homescreen.BookingSchedulesThenCreateScreen
import com.example.evcs_mobileapp.screens.homescreen.MyBookingsScreen
import com.example.evcs_mobileapp.screens.homescreen.StationsMapScreen
import com.example.evcs_mobileapp.screens.login_screens.SignupScreen
import com.example.evcs_mobileapp.screens.walkthrough.walkthrough_1
import com.example.evcs_mobileapp.screens.walkthrough.walkthrough_2
import com.example.evcs_mobileapp.screens.walkthrough.walkthrough_3
import com.example.evcs_mobileapp.screens.homescreen.ProfileScreen
import com.example.evcs_mobileapp.screens.homescreen.StationsAndSchedulesScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var isFirstLaunch by remember { mutableStateOf(true) }

    // Check SharedPreferences for first launch
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
        isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            prefs.edit().putBoolean("is_first_launch", false).apply()
        }
    }

    val prefs = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
    var hasToken by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasToken = prefs.getString("token", null)?.isNotEmpty() == true
    }
    val startDestination = if (hasToken) "owner_home" else if (isFirstLaunch) "walkthrough_1" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("walkthrough_1") { walkthrough_1(navController) }
        composable("walkthrough_2") { walkthrough_2(navController) }
        composable("walkthrough_3") { walkthrough_3(navController) }
        composable("login") {LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("owner_home") { OwnerHomeScreen(navController) }
        composable("my_bookings") { MyBookingsScreen(navController) }
        composable("new_booking") { BookingSchedulesThenCreateScreen() }
        composable("booking_history") { BookingHistoryScreen(navController) }
        composable("stations_map") { StationsMapScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("stations_schedules") { StationsAndSchedulesScreen() }
    }
}
