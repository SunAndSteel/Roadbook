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
import com.florent.carnetconduite.ui.home.sections.ArrivedFormContent
import com.florent.carnetconduite.ui.home.sections.CompletedSummaryContent
import com.florent.carnetconduite.ui.home.sections.IdleFormContent
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormContent
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormContent
import com.florent.carnetconduite.ui.home.sections.ReturnReadyFormContent

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
                // Écran de départ: saisie des informations initiales.
                IdleFormContent(ui.idle)
            }

            DrivingState.OUTWARD_ACTIVE -> {
                // Trajet aller en cours (formulaire d'arrivée).
                OutwardActiveFormContent(ui.outward)
            }

            DrivingState.ARRIVED -> {
                // Décision du trajet retour + info d'arrivée (selon l'emplacement choisi).
                ArrivedFormContent(
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
                // Préparation du retour.
                returnReadyTrip?.let { trip ->
                    ReturnReadyFormContent(trip, ui.returnReady)
                }
            }

            DrivingState.RETURN_ACTIVE -> {
                // Retour en cours (formulaire de fin).
                ReturnActiveFormContent(ui.returnActive)
            }

            DrivingState.COMPLETED -> {
                // Écran de fin avec résumé.
                CompletedSummaryContent(tripGroups)
            }
        }
    }
}
