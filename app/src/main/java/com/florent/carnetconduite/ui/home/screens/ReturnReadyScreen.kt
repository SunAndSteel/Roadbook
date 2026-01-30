package com.florent.carnetconduite.ui.home.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReturnReadyScreen(trip: Trip, viewModel: HomeViewModel = koinViewModel()) {
    val state = rememberReturnReadyScreenState()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ReturnReadyScreenContent(trip = trip, state = state)
    }
}

@Stable
class ReturnReadyScreenState {
    var editedStartKm by mutableStateOf("")
    var showDecisionDialog by mutableStateOf(false)
}

@Composable
fun rememberReturnReadyScreenState(): ReturnReadyScreenState = remember { ReturnReadyScreenState() }

@Composable
fun ReturnReadyScreenContent(trip: Trip, state: ReturnReadyScreenState) {
    if (state.editedStartKm.isEmpty()) {
        state.editedStartKm = trip.startKm.toString()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kilométrage avant départ",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = state.editedStartKm,
            onValueChange = { state.editedStartKm = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = null
                )
            },
            supportingText = {
                Text("Valeur suggérée basée sur le trajet aller")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Le retour repart du dernier kilométrage validé.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
