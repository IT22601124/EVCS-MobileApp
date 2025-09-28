package com.example.evcs_mobileapp

import com.example.evcs_mobileapp.login_screens.login_screen
import com.example.evcs_mobileapp.homescreen.OwnerHomeScreen
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.evcs_mobileapp.homescreen.BookingHistoryScreen
import com.example.evcs_mobileapp.homescreen.NewBookingScreen
import com.example.evcs_mobileapp.homescreen.MyBookingsScreen
import com.example.evcs_mobileapp.homescreen.StationsMapScreen
import com.example.evcs_mobileapp.login_screens.SignupScreen
import com.example.evcs_mobileapp.walkthrough.walkthrough_1
import com.example.evcs_mobileapp.walkthrough.walkthrough_2
import com.example.evcs_mobileapp.walkthrough.walkthrough_3

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

    val startDestination = if (isFirstLaunch) "walkthrough_1" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("walkthrough_1") { walkthrough_1(navController) }
        composable("walkthrough_2") { walkthrough_2(navController) }
        composable("walkthrough_3") { walkthrough_3(navController) }
        composable("login") {login_screen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("owner_home") { OwnerHomeScreen(navController) }
        composable("my_bookings") { MyBookingsScreen(navController) }
        composable("new_booking") { NewBookingScreen(onConfirm = { _, _, _, _ -> navController.navigate("my_bookings") }) }
        composable("booking_history") { BookingHistoryScreen(navController) }
        composable("stations_map") { StationsMapScreen(navController) }
    }
}
