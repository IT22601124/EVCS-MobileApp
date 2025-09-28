package com.example.evcs_mobileapp.homescreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)

@Preview(showBackground = true)
@Composable
fun NewBookingScreen(
    onConfirm: (station: String, date: LocalDate, time: LocalTime, duration: Int) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    var station by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var duration by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = station,
            onValueChange = { station = it },
            label = { Text("Station") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = date.toString(),
            onValueChange = {},
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val now = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) { Icon(Icons.Default.DateRange, contentDescription = "Pick date") }
            }
        )

        OutlinedTextField(
            value = time.toString(),
            onValueChange = {},
            label = { Text("Time") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val now = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, h, min -> time = LocalTime.of(h, min) },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                    ).show()
                }) { Icon(Icons.Default.AccessTime, contentDescription = "Pick time") }
            }
        )

        OutlinedTextField(
            value = duration,
            onValueChange = { if (it.all { c -> c.isDigit() }) duration = it },
            label = { Text("Duration (min)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                val dur = duration.toIntOrNull() ?: 0
                if (station.isNotBlank() && dur > 0) {
                    onConfirm(station, date, time, dur)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Confirm Reservation", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
        }

        Text(
            "Can modify/cancel ≥12h before start.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
