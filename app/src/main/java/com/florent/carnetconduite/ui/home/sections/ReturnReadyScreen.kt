package com.florent.carnetconduite.ui.home.sections

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.HomeViewModel

@Stable
class ReturnReadyFormState {
    // Kilométrage proposé pour le départ du retour.
    var editedStartKm by mutableStateOf("")
}

@Composable
fun rememberReturnReadyFormState(): ReturnReadyFormState = remember { ReturnReadyFormState() }

@Composable
fun ReturnReadyFormContent(trip: Trip, state: ReturnReadyFormState) {
    // Formulaire pour confirmer le démarrage du retour.
    if (state.editedStartKm.isEmpty()) {
        state.editedStartKm = trip.startKm.toString()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Préparer le retour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Vérifie le kilométrage avant de repartir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        OutlinedTextField(
            value = state.editedStartKm,
            onValueChange = { state.editedStartKm = it },
            label = { Text("Kilométrage de départ retour") },
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

@Composable
fun ReturnReadyFormPrimaryAction(
    trip: Trip,
    state: ReturnReadyFormState,
    viewModel: HomeViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(
            onClick = {
                viewModel.startReturn(
                    returnTripId = trip.id,
                    actualStartKm = state.editedStartKm.toIntOrNull()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Démarrer le retour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(
            onClick = { viewModel.cancelReturn(trip.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Cancel,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Annuler le retour",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
