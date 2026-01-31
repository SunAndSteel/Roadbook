package com.florent.carnetconduite.ui.home.sections

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
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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

@Stable
class OutwardActiveFormState {
    // Champs d'arrivée pour clôturer l'aller.
    var endKm by mutableStateOf("")
    var endPlace by mutableStateOf("")
    var showEditStartTime by mutableStateOf(false)
    var showEditEndTime by mutableStateOf(false)
}

@Composable
fun rememberOutwardActiveFormState(): OutwardActiveFormState = remember { OutwardActiveFormState() }

@Composable
fun OutwardActiveFormContent(state: OutwardActiveFormState) {
    // Formulaire d'arrivée du trajet aller.
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
fun OutwardActiveFormPrimaryAction(
    trip: Trip,
    state: OutwardActiveFormState,
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
fun OutwardActiveFormDialogs(
    trip: Trip?,
    state: OutwardActiveFormState,
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
}
