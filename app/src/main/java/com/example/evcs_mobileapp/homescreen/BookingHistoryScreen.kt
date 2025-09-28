package com.example.evcs_mobileapp.homescreen

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
import java.time.ZonedDateTime

enum class BookingStatus { Pending, Approved, Completed, Canceled }

data class BookingItem(
    val id: String,
    val station: String,
    val startIso: String,
    val durationMin: Int,
    val status: BookingStatus
)

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
                    if (item.status == BookingStatus.Completed || item.status == BookingStatus.Canceled) {
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
                Text(item.station, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(item.status.name) })
            }
            Spacer(Modifier.height(6.dp))
            val whenText = runCatching { ZonedDateTime.parse(item.startIso).toLocalDateTime().toString().replace('T', ' ') }
                .getOrElse { item.startIso }
            Text("${item.id} • ${item.durationMin} min")
            Text(whenText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun BookingHistoryScreenPreview() {
    val sample = listOf(
        BookingItem("BK-1101", "Orion DC Fast • 50kW", ZonedDateTime.now().minusDays(2).withHour(17).withMinute(30).toString(), 60, BookingStatus.Completed),
        BookingItem("BK-1102", "City Mall AC • 7kW", ZonedDateTime.now().minusDays(3).withHour(10).withMinute(0).toString(), 45, BookingStatus.Canceled),
    )
    MaterialTheme(colorScheme = lightColorScheme()) {
        BookingHistoryScreen(
            bookings = sample,
            navController = TODO()
        )
    }
}
