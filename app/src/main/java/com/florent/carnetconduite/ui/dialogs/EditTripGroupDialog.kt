package com.florent.carnetconduite.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.util.formatTime

@Composable
fun EditTripGroupDialog(
    group: TripGroup,
    onDismiss: () -> Unit,
    onEditStartTime: (Trip, Long) -> Unit,
    onEditEndTime: (Trip, Long) -> Unit,
    onEditStartKm: (Trip, Int) -> Unit,
    onEditEndKm: (Trip, Int) -> Unit
) {
    var showTimePicker by remember { mutableStateOf<Pair<Trip, String>?>(null) }
    var showKmPicker by remember { mutableStateOf<Pair<Trip, String>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le trajet") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Que veux-tu modifier ?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                // Aller
                item {
                    Text("--- Aller ---", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    Button(
                        onClick = { showTimePicker = group.outward to "start" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Heure départ: ${formatTime(group.outward.startTime)}")
                    }
                }

                item {
                    Button(
                        onClick = { showTimePicker = group.outward to "end" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Heure arrivée: ${group.outward.endTime?.let { formatTime(it) } ?: ""}")
                    }
                }

                item {
                    Button(
                        onClick = { showKmPicker = group.outward to "start" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Km départ: ${group.outward.startKm}")
                    }
                }

                item {
                    Button(
                        onClick = { showKmPicker = group.outward to "end" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Km arrivée: ${group.outward.endKm ?: 0}")
                    }
                }

                // Retour
                if (group.hasReturn && group.returnTrip != null) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("--- Retour ---", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Button(
                            onClick = { showTimePicker = group.returnTrip to "start" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Heure départ: ${formatTime(group.returnTrip.startTime)}")
                        }
                    }

                    item {
                        Button(
                            onClick = { showTimePicker = group.returnTrip to "end" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Heure arrivée: ${group.returnTrip.endTime?.let { formatTime(it) } ?: ""}")
                        }
                    }

                    item {
                        Button(
                            onClick = { showKmPicker = group.returnTrip to "start" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Km départ: ${group.returnTrip.startKm}")
                        }
                    }

                    item {
                        Button(
                            onClick = { showKmPicker = group.returnTrip to "end" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Km arrivée: ${group.returnTrip.endKm ?: 0}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )

    showTimePicker?.let { (trip, type) ->
        TimePickerDialog(
            initialTime = if (type == "start") trip.startTime else trip.endTime ?: System.currentTimeMillis(),
            onDismiss = { showTimePicker = null },
            onConfirm = { newTime ->
                if (type == "start") onEditStartTime(trip, newTime) else onEditEndTime(trip, newTime)
                showTimePicker = null
                onDismiss()
            }
        )
    }

    showKmPicker?.let { (trip, type) ->
        EditKmDialog(
            title = if (type == "start") "Modifier km départ" else "Modifier km arrivée",
            initialKm = if (type == "start") trip.startKm else trip.endKm ?: 0,
            onDismiss = { showKmPicker = null },
            onConfirm = { newKm ->
                if (type == "start") onEditStartKm(trip, newKm) else onEditEndKm(trip, newKm)
                showKmPicker = null
                onDismiss()
            }
        )
    }
}