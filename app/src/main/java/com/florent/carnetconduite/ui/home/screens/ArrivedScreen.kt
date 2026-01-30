package com.florent.carnetconduite.ui.home.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.HomeViewModel
import com.florent.carnetconduite.ui.shared.dialogs.EditKmDialog
import com.florent.carnetconduite.ui.shared.dialogs.TimePickerDialog
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.util.Locale

@Composable
fun ArrivedScreen(trip: Trip, viewModel: HomeViewModel = koinViewModel()) {
    val state = rememberArrivedScreenState()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ArrivedScreenContent()
    }
    ArrivedScreenDialogs(trip = trip, state = state, viewModel = viewModel)
}

@Stable
class ArrivedScreenState {
    var showEditEndTime by mutableStateOf(false)
    var showEditEndKm by mutableStateOf(false)
    var showDecisionDialog by mutableStateOf(false)
}

@Composable
fun rememberArrivedScreenState(): ArrivedScreenState = remember { ArrivedScreenState() }

@Composable
fun ArrivedScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Suite du trajet",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Décide de la suite du trajet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * StatsSection for the arrived state; owns distance + duration to avoid duplication
 * with the summary header.
 */
@Composable
fun ArrivedStatsSection(
    trip: Trip,
    onEditDistance: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    onEditDistance?.let {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Modifier la distance"
                            )
                        }
                    }
                }
                Text(
                    text = "${trip.nbKmsParcours} km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Distance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = formatDuration(trip.startTime, trip.endTime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Durée",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ArrivedScreenDialogs(
    trip: Trip?,
    state: ArrivedScreenState,
    viewModel: HomeViewModel
) {
    if (trip == null) return
    if (state.showEditEndTime) {
        TimePickerDialog(
            initialTime = trip.endTime ?: System.currentTimeMillis(),
            onDismiss = { state.showEditEndTime = false },
            onConfirm = { newTime ->
                viewModel.editEndTime(trip.id, newTime)
                state.showEditEndTime = false
            }
        )
    }

    if (state.showEditEndKm) {
        EditKmDialog(
            title = "Modifier km arrivée",
            initialKm = trip.endKm ?: 0,
            onDismiss = { state.showEditEndKm = false },
            onConfirm = { newKm ->
                viewModel.editEndKm(trip.id, newKm)
                state.showEditEndKm = false
            }
        )
    }

    if (state.showDecisionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { state.showDecisionDialog = false },
            title = { Text("Suite du trajet") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null
                    )
                    Text("Choisis la suite du trajet.")
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.prepareReturnTrip(trip.id)
                        state.showDecisionDialog = false
                    }
                ) {
                    Text("Préparer un retour")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.confirmSimpleTrip(trip.id)
                        state.showDecisionDialog = false
                    }
                ) {
                    Text("Trajet simple")
                }
            }
        )
    }
}

private fun formatDuration(start: Long, end: Long?): String {
    if (end == null || end <= start) return "—"
    val duration = Duration.ofMillis(end - start)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    return if (hours > 0) {
        String.format(Locale.FRENCH, "%dh%02d", hours, minutes)
    } else {
        String.format(Locale.FRENCH, "%d min", minutes)
    }
}
