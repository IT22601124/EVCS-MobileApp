
package com.example.evcs_mobileapp.ui.bookings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.ZonedDateTime

enum class BookingStatus { Pending, Approved }

data class BookingItem(
    val id: String,
    val station: String,
    val startIso: String, // ISO-8601 with zone, e.g., 2025-10-01T10:00:00+05:30
    val durationMin: Int,
    val status: BookingStatus
)

@RequiresApi(Build.VERSION_CODES.O)
fun canEditOrCancel(startIso: String): Boolean = runCatching {
    val start = ZonedDateTime.parse(startIso)
    Duration.between(ZonedDateTime.now(), start).toHours() >= 12
}.getOrDefault(false)

// ---- Screen ----


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)

@Preview(showBackground = true)
@Composable
fun MyBookingsScreen(
    bookings: List<BookingItem>,
    onCreateNew: () -> Unit,
    onEdit: (BookingItem) -> Unit,
    onCancel: (BookingItem) -> Unit,
    onOpen: (BookingItem) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bookings") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNew) {
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
                    BookingRow(
                        item = item,
                        onClick = { onOpen(item) },
                        onEdit = { onEdit(item) },
                        onCancel = { onCancel(item) }
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingRow(
    item: BookingItem,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
) {
    val editable = remember(item.startIso) { canEditOrCancel(item.startIso) }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
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
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, enabled = editable) { Text("Edit") }
                OutlinedButton(onClick = onCancel, enabled = editable) { Text("Cancel") }
            }
        }
    }
}

// ---- Preview with sample data ----

@RequiresApi(Build.VERSION_CODES.O)
private fun sampleBookings(): List<BookingItem> = listOf(
    BookingItem("BK-1201", "Orion DC Fast • 50kW", ZonedDateTime.now().plusDays(2).withHour(17).withMinute(30).toString(), 60, BookingStatus.Pending),
    BookingItem("BK-1202", "City Mall AC • 7kW", ZonedDateTime.now().plusDays(3).withHour(10).withMinute(0).toString(), 45, BookingStatus.Approved),
)

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun MyBookingsScreenPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MyBookingsScreen(
            bookings = sampleBookings(),
            onCreateNew = {},
            onEdit = {},
            onCancel = {},
            onOpen = {}
        )
    }
}
