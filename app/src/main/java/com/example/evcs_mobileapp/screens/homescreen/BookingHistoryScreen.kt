package com.example.evcs_mobileapp.screens.homescreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.evcs_mobileapp.screens.homescreen.BookingStatus
import com.example.evcs_mobileapp.screens.homescreen.BookingItem
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun BookingHistoryScreen(
    navController: NavController,
    bookings: List<BookingItem> = emptyList()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Event, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No past bookings")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(bookings) { item ->
                    if (item.status == "Completed" || item.status == "Canceled") {
                        BookingHistoryRow(item)
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingHistoryRow(item: BookingItem) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.stationName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(item.status) })
            }
            Spacer(Modifier.height(6.dp))
            val whenText = "${item.date} ${item.start} - ${item.end}"
            Text("${item.id} • ${item.stationType}")
            Text(whenText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun BookingHistoryScreenPreview() {
    val sample = listOf(
        BookingItem(
            id = "BK-1101",
            nic = "200011701132",
            ownerName = "Orion DC Fast",
            ownerEmail = "orion@email.com",
            ownerPhone = "0771234567",
            stationId = "station1",
            stationName = "Orion DC Fast • 50kW",
            stationAddress = "123 Main St",
            stationType = "DC",
            date = "2025-10-07",
            start = "17:30:00",
            end = "18:30:00",
            status = "Completed",
            qrToken = null,
            createdAt = "2025-10-07T17:30:00Z",
            updatedAt = null
        ),
        BookingItem(
            id = "BK-1102",
            nic = "200011701132",
            ownerName = "City Mall AC",
            ownerEmail = "citymall@email.com",
            ownerPhone = "0779876543",
            stationId = "station2",
            stationName = "City Mall AC • 7kW",
            stationAddress = "456 City Mall",
            stationType = "AC",
            date = "2025-10-06",
            start = "10:00:00",
            end = "10:45:00",
            status = "Canceled",
            qrToken = null,
            createdAt = "2025-10-06T10:00:00Z",
            updatedAt = null
        )
    )
    MaterialTheme(colorScheme = lightColorScheme()) {
        BookingHistoryScreen(
            bookings = sample,
            navController = TODO()
        )
    }
}
