package com.example.evcs_mobileapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.evcs_mobileapp.intial.CustomSplash
import com.example.evcs_mobileapp.intial.MainViewModel
import com.example.evcs_mobileapp.ui.theme.EVCSMobileAppTheme
import com.example.evcs_mobileapp.walkthrough.Walkthrough
import com.example.evcs_mobileapp.walkthrough.walkthrough_1

@Composable
fun MyApp(vm: MainViewModel) {
    // read VM state
    val showSplash by vm.showCustomSplash.collectAsState()
    val progress by vm.progress.collectAsState()
    val isFirstLogin by vm.isFirstLogin.collectAsState()

    EVCSMobileAppTheme {
        when {
            showSplash -> CustomSplash(progress)
            isFirstLogin -> walkthrough_1()
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
