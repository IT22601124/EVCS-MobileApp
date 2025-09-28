package com.example.evcs_mobileapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.evcs_mobileapp.homescreen.BookingHistoryScreen
import com.example.evcs_mobileapp.homescreen.MyBookingsScreen
import com.example.evcs_mobileapp.homescreen.NewBookingScreen
import com.example.evcs_mobileapp.homescreen.OwnerHomeScreen
import com.example.evcs_mobileapp.homescreen.StationsMapScreen
import com.example.evcs_mobileapp.intial.CustomSplash
import com.example.evcs_mobileapp.intial.MainViewModel
import com.example.evcs_mobileapp.ui.theme.EVCSMobileAppTheme
import com.example.evcs_mobileapp.walkthrough.walkthrough_2
import com.example.evcs_mobileapp.walkthrough.walkthrough_1
import com.example.evcs_mobileapp.walkthrough.walkthrough_3
import com.example.evcs_mobileapp.login_screens.SignupScreen
import com.example.evcs_mobileapp.login_screens.login_screen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp(vm: MainViewModel) {
    // read VM state
    val showSplash by vm.showCustomSplash.collectAsState()
    val progress by vm.progress.collectAsState()
    val isFirstLogin by vm.isFirstLogin.collectAsState()

    EVCSMobileAppTheme {
        when {
            showSplash -> CustomSplash(progress)
            isFirstLogin -> {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "walkthrough_1") {
                    composable("walkthrough_1") { walkthrough_1(navController) }
                    composable("walkthrough_2") { walkthrough_2(navController) }
                    composable("walkthrough_3") { walkthrough_3(navController) }
                    composable("signup_screen") { SignupScreen(navController) }
                    composable("login") { login_screen(navController) }

                    composable("owner_home") { OwnerHomeScreen(navController) }
                    composable("my_bookings") { MyBookingsScreen(navController) }
                    composable("new_booking") { NewBookingScreen(onConfirm = { _, _, _, _ -> navController.navigate("my_bookings") }) }
                    composable("booking_history") { BookingHistoryScreen(navController) }
                    composable("stations_map") { StationsMapScreen(navController) }
                }
            }
            else -> {
                // Your real app UI
                Scaffold { inner ->
                    Text(
                        "Home Screen",
                        modifier = Modifier
                            .padding(inner)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}


annotation class MainAppContent
