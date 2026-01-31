package com.florent.carnetconduite.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.HomeUnifiedState
import com.florent.carnetconduite.ui.home.HomeViewModel
import com.florent.carnetconduite.ui.home.sections.ArrivedDecisionPrimaryAction
import com.florent.carnetconduite.ui.home.sections.CompletedSummaryPrimaryAction
import com.florent.carnetconduite.ui.home.sections.IdleFormPrimaryAction
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormPrimaryAction
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormPrimaryAction
import com.florent.carnetconduite.ui.home.sections.ReturnReadyFormPrimaryAction

@Composable
fun StickyBottomArea(
    drivingState: DrivingState,
    outwardTrip: Trip?,
    arrivedTrip: Trip?,
    returnReadyTrip: Trip?,
    returnActiveTrip: Trip?,
    ui: HomeUnifiedState,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ✅ Pendant ARRIVED : carte “kilométrage + lieu” collée au CTA
            if (drivingState == DrivingState.ARRIVED) {
                ArrivalInputsCard(
                    endKmText = ui.arrived.endKmText,
                    onEndKmChange = { ui.arrived.endKmText = it },
                    arrivalPlace = ui.arrived.arrivalPlace,
                    onArrivalPlaceChange = { ui.arrived.arrivalPlace = it }
                )
            }

            PrimaryActionArea {
                when (drivingState) {
                    DrivingState.IDLE ->
                        IdleFormPrimaryAction(ui.arrived, viewModel)

                    DrivingState.OUTWARD_ACTIVE ->
                        outwardTrip?.let { trip ->
                            OutwardActiveFormPrimaryAction(trip, ui.outward, viewModel)
                        }

                    DrivingState.ARRIVED ->
                        arrivedTrip?.let { trip ->
                            // Ici ton PrimaryAction “Termine le trajet” peut utiliser ui.arrived.endKmText / arrivalPlace
                            ArrivedDecisionPrimaryAction(trip, viewModel)
                        }

                    DrivingState.RETURN_READY ->
                        returnReadyTrip?.let { trip ->
                            ReturnReadyFormPrimaryAction(trip, ui.returnReady, viewModel)
                        }

                    DrivingState.RETURN_ACTIVE ->
                        returnActiveTrip?.let { trip ->
                            ReturnActiveFormPrimaryAction(trip, ui.returnActive, viewModel)
                        }

                    DrivingState.COMPLETED ->
                        CompletedSummaryPrimaryAction()
                }
            }
        }
    }
}

@Composable
private fun ArrivalInputsCard(
    endKmText: String,
    onEndKmChange: (String) -> Unit,
    arrivalPlace: String,
    onArrivalPlaceChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Arrivée",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Renseigne uniquement l’arrivée. Le départ est déjà validé.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = endKmText,
                onValueChange = onEndKmChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kilométrage arrivée") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Rounded.Speed, null) },
                singleLine = true
            )

            OutlinedTextField(
                value = arrivalPlace,
                onValueChange = onArrivalPlaceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Lieu d’arrivée") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Rounded.Place, null) },
                singleLine = true
            )
        }
    }
}
