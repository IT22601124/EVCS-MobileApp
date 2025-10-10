package com.example.evcs_mobileapp.screens.homescreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Dummy data class for station
 data class Station(val name: String, val distance: Double, val type: String, val slots: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsMapScreen(
    navController: NavController,
    stations: List<Station> = listOf(
        Station("EVCS Station 1", 0.5, "DC Fast", 2),
        Station("EVCS Station 2", 1.2, "AC", 4)
    ),
    onRefreshLocation: () -> Unit = {}
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Stations Map") }) },
        bottomBar = {
            Button(
                onClick = onRefreshLocation,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Use my location")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Placeholder for Google Map
            Box(Modifier.fillMaxWidth().height(220.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                Text("[Google Map Here]", style = MaterialTheme.typography.titleMedium)
            }
            LazyColumn(Modifier.fillMaxWidth().padding(8.dp)) {
                items(stations) { station ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(station.name, style = MaterialTheme.typography.titleMedium)
                            Text("Distance: ${station.distance} km")
                            Text("Type: ${station.type}")
                            Text("Slots: ${station.slots}")
                        }
                    }
                }
            }
        }
    }
}

