package com.florent.carnetconduite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.dialogs.EditKmDialog
import com.florent.carnetconduite.ui.dialogs.TimePickerDialog
import com.florent.carnetconduite.util.formatTime

@Composable
fun ArrivedScreen(trip: Trip, viewModel: DrivingViewModel) {
    var showEditEndTime by remember { mutableStateOf(false) }
    var showEditEndKm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Trajet terminé !", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text(
                "${trip.startPlace} → ${trip.endPlace ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Km arrivée: ${trip.endKm ?: 0}", style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { showEditEndKm = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(18.dp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Heure arrivée: ${trip.endTime?.let { formatTime(it) } ?: ""}", style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { showEditEndTime = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Text(
                    "${trip.nbKmsParcours} km",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Que veux-tu faire ?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.decideTripType(tripId = trip.id, prepareReturn = false)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Text("Trajet simple")
                    }
                }

                Button(
                    onClick = {
                        viewModel.decideTripType(tripId = trip.id, prepareReturn = true)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null)
                        Text("Aller-retour")
                    }
                }
            }
        }
    }

    if (showEditEndTime) {
        TimePickerDialog(
            initialTime = trip.endTime ?: System.currentTimeMillis(),
            onDismiss = { showEditEndTime = false },
            onConfirm = { newTime ->
                viewModel.editEndTime(trip.id, newTime)
                showEditEndTime = false
            }
        )
    }

    if (showEditEndKm) {
        EditKmDialog(
            title = "Modifier km arrivée",
            initialKm = trip.endKm ?: 0,
            onDismiss = { showEditEndKm = false },
            onConfirm = { newKm ->
                viewModel.editEndKm(trip.id, newKm)
                showEditEndKm = false
            }
        )
    }
}
