package com.florent.carnetconduite.ui.home.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.HomeViewModel
import com.florent.carnetconduite.ui.shared.dialogs.TimePickerDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReturnActiveScreen(trip: Trip, viewModel: HomeViewModel = koinViewModel()) {
    val state = rememberReturnActiveScreenState()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ReturnActiveScreenContent(state = state)
    }
    ReturnActiveScreenDialogs(trip = trip, state = state, viewModel = viewModel)
}

@Stable
class ReturnActiveScreenState {
    var endKm by mutableStateOf("")
    var showEditStartTime by mutableStateOf(false)
    var showEditEndTime by mutableStateOf(false)
}

@Composable
fun rememberReturnActiveScreenState(): ReturnActiveScreenState = remember { ReturnActiveScreenState() }

@Composable
fun ReturnActiveScreenContent(state: ReturnActiveScreenState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kilométrage d'arrivée",
            style = MaterialTheme.typography.titleMedium
        )

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
    }
}

@Composable
fun ReturnActiveScreenDialogs(
    trip: Trip?,
    state: ReturnActiveScreenState,
    viewModel: HomeViewModel
) {
    if (trip == null) return
    if (state.showEditStartTime && trip.startTime > 0L) {
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
