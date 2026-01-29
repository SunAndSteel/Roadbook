package com.florent.carnetconduite.ui.history.dialogs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.shared.dialogs.EditKmDialog
import com.florent.carnetconduite.ui.shared.dialogs.TimePickerDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
        icon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Modifier le trajet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${group.outward.startPlace} → ${group.outward.endPlace}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section Trajet Aller
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Trajet aller",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Boutons Aller
                item {
                    EditFieldCard(
                        icon = Icons.Rounded.Schedule,
                        label = "Heure de départ",
                        value = formatTime(group.outward.startTime),
                        onClick = { showTimePicker = group.outward to "start" },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                item {
                    EditFieldCard(
                        icon = Icons.Rounded.Schedule,
                        label = "Heure d'arrivée",
                        value = group.outward.endTime?.let { formatTime(it) } ?: "Non renseignée",
                        onClick = { showTimePicker = group.outward to "end" },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                item {
                    EditFieldCard(
                        icon = Icons.Rounded.Speed,
                        label = "Kilométrage départ",
                        value = "${group.outward.startKm} km",
                        onClick = { showKmPicker = group.outward to "start" },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                item {
                    EditFieldCard(
                        icon = Icons.Rounded.Speed,
                        label = "Kilométrage arrivée",
                        value = "${group.outward.endKm ?: 0} km",
                        onClick = { showKmPicker = group.outward to "end" },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                // Section Trajet Retour
                if (group.hasReturn && group.returnTrip != null) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardReturn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Trajet retour",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Boutons Retour
                    item {
                        EditFieldCard(
                            icon = Icons.Rounded.Schedule,
                            label = "Heure de départ",
                            value = formatTime(group.returnTrip.startTime),
                            onClick = { showTimePicker = group.returnTrip to "start" },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    item {
                        EditFieldCard(
                            icon = Icons.Rounded.Schedule,
                            label = "Heure d'arrivée",
                            value = group.returnTrip.endTime?.let { formatTime(it) } ?: "Non renseignée",
                            onClick = { showTimePicker = group.returnTrip to "end" },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    item {
                        EditFieldCard(
                            icon = Icons.Rounded.Speed,
                            label = "Kilométrage départ",
                            value = "${group.returnTrip.startKm} km",
                            onClick = { showKmPicker = group.returnTrip to "start" },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    item {
                        EditFieldCard(
                            icon = Icons.Rounded.Speed,
                            label = "Kilométrage arrivée",
                            value = "${group.returnTrip.endKm ?: 0} km",
                            onClick = { showKmPicker = group.returnTrip to "end" },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // Espacement final
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terminer")
            }
        }
    )

    // TimePickerDialog
    showTimePicker?.let { (trip, type) ->
        TimePickerDialog(
            initialTime = if (type == "start") trip.startTime else trip.endTime
                ?: System.currentTimeMillis(),
            onDismiss = { showTimePicker = null },
            onConfirm = { newTime ->
                if (type == "start") onEditStartTime(trip, newTime) else onEditEndTime(
                    trip,
                    newTime
                )
                showTimePicker = null
            }
        )
    }

    // EditKmDialog
    showKmPicker?.let { (trip, type) ->
        EditKmDialog(
            title = if (type == "start") "Modifier km départ" else "Modifier km arrivée",
            initialKm = if (type == "start") trip.startKm else trip.endKm ?: 0,
            onDismiss = { showKmPicker = null },
            onConfirm = { newKm ->
                if (type == "start") onEditStartKm(trip, newKm) else onEditEndKm(trip, newKm)
                showKmPicker = null
            }
        )
    }
}

@Composable
private fun EditFieldCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        modifier = Modifier.alpha(0.7f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(0.5f)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "N/A"
    }
}