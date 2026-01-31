package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.home.sections.ArrivedScreen
import com.florent.carnetconduite.ui.home.sections.CompletedScreen
import com.florent.carnetconduite.ui.home.sections.IdleScreen
import com.florent.carnetconduite.ui.home.sections.OutwardActiveScreen
import com.florent.carnetconduite.ui.home.sections.ReturnActiveScreen
import com.florent.carnetconduite.ui.home.sections.ReturnReadyScreen

@Composable
fun HomeFormSection(
    drivingState: DrivingState,
    ui: HomeUnifiedState,
    outwardTrip: Trip?,
    arrivedTrip: Trip?,
    returnReadyTrip: Trip?,
    returnActiveTrip: Trip?,
    tripGroups: List<TripGroup>,
    showArrivalInputsInForm: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (drivingState) {
            DrivingState.IDLE -> {
                IdleScreen.Content(ui.idle)
            }

            DrivingState.OUTWARD_ACTIVE -> {
                // Ton contenu “trajet en cours” sans le bloc d’arrivée
                OutwardActiveScreen.Content(ui.outward)
            }

            DrivingState.ARRIVED -> {
                // On garde la logique “décision” ici, mais PAS les inputs km/lieu si on les a déplacés
                ArrivedScreen.Content(
                    state = ui.arrived,
                    showArrivalInputs = showArrivalInputsInForm
                )

                if (!showArrivalInputsInForm) {
                    Text(
                        text = "Les infos d’arrivée se complètent en bas, juste avant de terminer le trajet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DrivingState.RETURN_READY -> {
                ReturnReadyScreen.Content(ui.returnReady)
            }

            DrivingState.RETURN_ACTIVE -> {
                ReturnActiveScreen.Content(ui.returnActive)
            }

            DrivingState.COMPLETED -> {
                CompletedScreen.Content(tripGroups)
            }
        }
    }
}
