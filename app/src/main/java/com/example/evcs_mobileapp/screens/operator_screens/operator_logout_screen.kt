package com.example.evcs_mobileapp.screens.operator_screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.room.Room
import com.example.evcs_mobileapp.db.AppDatabase

@Composable
fun OperatorLogoutScreen(navController: NavHostController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
        sharedPref.edit()
            .remove("token")
            .remove("role")
            .apply()
        // Clear local DB auth info
        GlobalScope.launch(Dispatchers.IO) {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "evcs_db"
            ).build()
            db.authResponseDao().clear()
        }
        // Navigate to login after clearing
        navController.navigate("login") {
            popUpTo(0)
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Logging out...")
            }
        }
    }
}

