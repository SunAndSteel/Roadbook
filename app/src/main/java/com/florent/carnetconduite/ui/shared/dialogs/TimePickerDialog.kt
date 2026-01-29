package com.florent.carnetconduite.ui.shared.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun TimePickerDialog(
    initialTime: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val instant = Instant.ofEpochMilli(initialTime)
    val localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault())

    var hour by remember { mutableStateOf(localTime.hour.toString()) }
    var minute by remember { mutableStateOf(localTime.minute.toString().padStart(2, '0')) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier l'heure") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hour,
                    onValueChange = { if (it.length <= 2) hour = it },
                    label = { Text("HH") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text(":", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(
                    value = minute,
                    onValueChange = { if (it.length <= 2) minute = it },
                    label = { Text("MM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val h = hour.toIntOrNull() ?: 0
                val m = minute.toIntOrNull() ?: 0
                val newTime = instant.atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(h.coerceIn(0, 23), m.coerceIn(0, 59))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                onConfirm(newTime)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}