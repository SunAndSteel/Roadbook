package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.ArrivedStatsSection
import com.florent.carnetconduite.ui.home.screens.CompletedScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenContent

/**
 * Une seule section “Form” qui change selon DrivingState.
 * Le layout parent (header/summary/cta sticky) reste stable.
 */
@Composable
fun HomeFormSection(
    drivingState: DrivingState,
    ui: HomeUnifiedState,
    outwardTrip: Trip?,
    arrivedTrip: Trip?,
    returnReadyTrip: Trip?,
    returnActiveTrip: Trip?,
    tripGroups: List<TripGroup>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (drivingState) {
            DrivingState.IDLE -> {
                IdleScreenContent(ui.idle)
            }

            DrivingState.OUTWARD_ACTIVE -> {
                // Ton design actuel : content = state only
                OutwardActiveScreenContent(ui.outward)
            }

            DrivingState.ARRIVED -> {
                // Ton ArrivedScreenContent() est “stateless” (UI texte)
                ArrivedScreenContent()

                // Mais pour unifier et rendre ça utile: on ajoute les stats ici,
                // et on branche l'édition vers les dialogs via ui.arrived.
                arrivedTrip?.let { trip ->
                    ArrivedStatsSection(
                        trip = trip,
                        onEditDistance = { ui.arrived.showEditEndKm = true }
                    )
                    // Optionnel : si tu veux aussi éditer l’heure d’arrivée depuis la section:
                    // ui.arrived.showEditEndTime = true
                }
            }

            DrivingState.RETURN_READY -> {
                returnReadyTrip?.let { trip ->
                    ReturnReadyScreenContent(trip, ui.returnReady)
                }
            }

            DrivingState.RETURN_ACTIVE -> {
                ReturnActiveScreenContent(ui.returnActive)
            }

            DrivingState.COMPLETED -> {
                CompletedScreenContent(tripGroups)
            }
        }
    }
}
package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.ArrivedStatsSection
import com.florent.carnetconduite.ui.home.screens.CompletedScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenContent

/**
 * Une seule section “Form” qui change selon DrivingState.
 * Le layout parent (header/summary/cta sticky) reste stable.
 */
@Composable
fun HomeFormSection(
    drivingState: DrivingState,
    ui: HomeUnifiedState,
    outwardTrip: Trip?,
    arrivedTrip: Trip?,
    returnReadyTrip: Trip?,
    returnActiveTrip: Trip?,
    tripGroups: List<TripGroup>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (drivingState) {
            DrivingState.IDLE -> {
                IdleScreenContent(ui.idle)
            }

            DrivingState.OUTWARD_ACTIVE -> {
                // Ton design actuel : content = state only
                OutwardActiveScreenContent(ui.outward)
            }

            DrivingState.ARRIVED -> {
                // Ton ArrivedScreenContent() est “stateless” (UI texte)
                ArrivedScreenContent()

                // Mais pour unifier et rendre ça utile: on ajoute les stats ici,
                // et on branche l'édition vers les dialogs via ui.arrived.
                arrivedTrip?.let { trip ->
                    ArrivedStatsSection(
                        trip = trip,
                        onEditDistance = { ui.arrived.showEditEndKm = true }
                    )
                    // Optionnel : si tu veux aussi éditer l’heure d’arrivée depuis la section:
                    // ui.arrived.showEditEndTime = true
                }
            }

            DrivingState.RETURN_READY -> {
                returnReadyTrip?.let { trip ->
                    ReturnReadyScreenContent(trip, ui.returnReady)
                }
            }

            DrivingState.RETURN_ACTIVE -> {
                ReturnActiveScreenContent(ui.returnActive)
            }

            DrivingState.COMPLETED -> {
                CompletedScreenContent(tripGroups)
            }
        }
    }
}
