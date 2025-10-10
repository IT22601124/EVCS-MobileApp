package com.example.evcs_mobileapp.screens.homescreen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.evcs_mobileapp.R
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.example.evcs_mobileapp.db.AppDatabase
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.collections.remove
import androidx.core.content.edit
import android.util.Log
import kotlin.apply
import kotlin.collections.remove
import kotlin.text.clear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember(context) {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "evcs_db"
        ).build()
    }
    val dao = db.authResponseDao()
    var user by remember { mutableStateOf<com.example.evcs_mobileapp.db.AuthResponseEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        user = dao.getUser()
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            if (user == null) {
                Spacer(modifier = Modifier.height(64.dp))
                Text("No profile data found.", fontSize = 18.sp, color = Color.Gray)
            } else {
                Image(
                    painter = painterResource(id = R.drawable.signupimage),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(user!!.fullName ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(user!!.email ?: "", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(user!!.phone ?: "", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("NIC: ${user!!.nic ?: ""}", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val sharedPref = context.getSharedPreferences("evcs_prefs", Context.MODE_PRIVATE)
                            sharedPref.edit()
                                .remove("token")
                                .remove("role")
                                .apply()

                            dao.clear()
                            navController.navigate("login") {
                                popUpTo("profile") { inclusive = true }
                            }
                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Logout", fontSize = 14.sp)
                }
            }
        }
    }
}
