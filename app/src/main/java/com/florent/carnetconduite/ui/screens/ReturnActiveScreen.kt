package com.florent.carnetconduite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.util.formatTime
import com.florent.carnetconduite.ui.dialogs.TimePickerDialog

@Composable
fun ReturnActiveScreen(trip: Trip, viewModel: DrivingViewModel) {
    var endKm by remember { mutableStateOf("") }
    var showEditStartTime by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Retour en cours", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text("${trip.startPlace} → ${trip.endPlace ?: ""}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text("Km départ: ${trip.startKm}", style = MaterialTheme.typography.bodyMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                val timeDisplay = if (trip.startTime > 0L) formatTime(trip.startTime) else "Maintenant"
                Text("Début: $timeDisplay", style = MaterialTheme.typography.bodyMedium)
                if (trip.startTime > 0L) {
                    IconButton(onClick = { showEditStartTime = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier l'heure", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = endKm,
                onValueChange = { endKm = it },
                label = { Text("Kilométrage arrivée") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.finishReturn(
                        tripId = trip.id,
                        endKm = endKm.toIntOrNull() ?: 0
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Flag, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Arriver (fin du retour)")
            }
        }
    }

    if (showEditStartTime && trip.startTime > 0L) {
        TimePickerDialog(
            initialTime = trip.startTime,
            onDismiss = { showEditStartTime = false },
            onConfirm = { newTime ->
                viewModel.editStartTime(trip.id, newTime)
                showEditStartTime = false
            }
        )
    }
}