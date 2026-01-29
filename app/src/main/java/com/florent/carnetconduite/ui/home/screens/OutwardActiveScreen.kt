package com.florent.carnetconduite.ui.home.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.HomeViewModel
import com.florent.carnetconduite.ui.shared.dialogs.TimePickerDialog
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OutwardActiveScreen(trip: Trip, viewModel: HomeViewModel = koinViewModel()) {
    val state = rememberOutwardActiveScreenState()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutwardActiveScreenContent(trip = trip, state = state)
        OutwardActiveScreenPrimaryAction(trip = trip, state = state, viewModel = viewModel)
    }
    OutwardActiveScreenDialogs(trip = trip, state = state, viewModel = viewModel)
}

@Stable
class OutwardActiveScreenState {
    var endKm by mutableStateOf("")
    var endPlace by mutableStateOf("")
    var showEditStartTime by mutableStateOf(false)
}

@Composable
fun rememberOutwardActiveScreenState(): OutwardActiveScreenState = remember { OutwardActiveScreenState() }

@Composable
fun OutwardActiveScreenContent(trip: Trip, state: OutwardActiveScreenState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Arrivée",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Renseigne uniquement l'arrivée. Le départ est déjà validé.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ListItem(
            headlineContent = { Text("Modifier l'heure de départ") },
            supportingContent = { Text(formatTime(trip.startTime)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            trailingContent = {
                IconButton(onClick = { state.showEditStartTime = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Modifier l'heure"
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.endKm,
                onValueChange = { state.endKm = it },
                label = { Text("Kilométrage arrivée") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            OutlinedTextField(
                value = state.endPlace,
                onValueChange = { state.endPlace = it },
                label = { Text("Lieu d'arrivée") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun OutwardActiveScreenPrimaryAction(
    trip: Trip,
    state: OutwardActiveScreenState,
    viewModel: HomeViewModel
) {
    FilledTonalButton(
        onClick = {
            viewModel.finishOutward(
                tripId = trip.id,
                endKm = state.endKm.toIntOrNull() ?: 0,
                endPlace = state.endPlace
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Flag,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Terminer le trajet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OutwardActiveScreenDialogs(
    trip: Trip?,
    state: OutwardActiveScreenState,
    viewModel: HomeViewModel
) {
    if (trip == null) return
    if (state.showEditStartTime) {
        TimePickerDialog(
            initialTime = trip.startTime,
            onDismiss = { state.showEditStartTime = false },
            onConfirm = { newTime ->
                viewModel.editStartTime(trip.id, newTime)
                state.showEditStartTime = false
            }
        )
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
