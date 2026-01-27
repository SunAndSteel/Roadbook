package com.florent.carnetconduite.ui.components

import com.florent.carnetconduite.ui.dialogs.EditTripGroupDialog
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
import com.florent.carnetconduite.domain.models.TripGroup

@Composable
fun TripGroupCard(
    group: TripGroup,
    onDelete: () -> Unit,
    onEditStartTime: (Trip, Long) -> Unit,
    onEditEndTime: (Trip, Long) -> Unit,
    onEditStartKm: (Trip, Int) -> Unit,
    onEditEndKm: (Trip, Int) -> Unit
) {
    var showEditMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (group.hasReturn) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Séance ${group.seanceNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (group.hasReturn) {
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                Text("Aller-retour", color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                    Text("${group.outward.date}", style = MaterialTheme.typography.bodySmall)
                }

                Row {
                    IconButton(onClick = { showEditMenu = true }) {
                        Icon(Icons.Default.Edit, "Modifier")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Supprimer")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Trajet aller
            TripDetails(
                trip = group.outward,
                label = if (group.hasReturn) "Aller" else null
            )

            // Trajet retour si existe
            if (group.hasReturn) {
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                group.returnTrip?.let { returnTrip ->
                    TripDetails(trip = returnTrip, label = "Retour")
                }
            }

            // Total
            Spacer(Modifier.height(8.dp))
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total comptabilisé", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${group.totalKms} km",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showEditMenu) {
        EditTripGroupDialog(
            group = group,
            onDismiss = { showEditMenu = false },
            onEditStartTime = onEditStartTime,
            onEditEndTime = onEditEndTime,
            onEditStartKm = onEditStartKm,
            onEditEndKm = onEditEndKm
        )
    }
}