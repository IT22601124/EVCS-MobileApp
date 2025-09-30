package com.example.evcs_mobileapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.example.evcs_mobileapp.screens.intial.MainViewModel
import com.example.evcs_mobileapp.ui.theme.EVCSMobileAppTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp(vm: MainViewModel) {
    EVCSMobileAppTheme {
        MainNavHost()
    }
}


annotation class MainAppContent
