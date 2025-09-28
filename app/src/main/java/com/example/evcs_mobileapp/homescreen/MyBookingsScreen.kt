package com.example.evcs_mobileapp.homescreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavController,
    bookings: List<BookingItem> = emptyList()
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bookings") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("new_booking") }) {
                Icon(Icons.Default.Add, contentDescription = "Create New Booking")
            }
        }
    ) { padding ->
        if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No upcoming bookings")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(bookings) { item ->
                    if (item.status == BookingStatus.Pending || item.status == BookingStatus.Approved) {
                        BookingItemRow(item, navController)
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingItemRow(item: BookingItem, navController: NavController) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.station, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(item.status.name) })
            }
            Spacer(Modifier.height(6.dp))
            val whenText = runCatching { ZonedDateTime.parse(item.startIso).toLocalDateTime().toString().replace('T', ' ') }
                .getOrElse { item.startIso }
            Text("${item.id}  ${item.durationMin} min")
            Text(whenText, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (item.status == BookingStatus.Pending || item.status == BookingStatus.Approved) {
                    Button(onClick = { /* Edit logic if >=12h before */ }) {
                        Text("Edit")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { /* Cancel logic if >=12h before */ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

