package com.florent.carnetconduite.ui.home.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.KeyboardReturn
import androidx.compose.material.icons.rounded.UTurnLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.home.components.TripHeaderData
import com.florent.carnetconduite.util.formatTimeRange

internal data class StepColors(
    val headerContainer: Color,
    val onHeaderContainer: Color,
    val statusColor: Color,
    val cardContainer: Color
)

internal fun headerForState(state: DrivingState): TripHeaderData {
    return when (state) {
        DrivingState.IDLE -> TripHeaderData(
            icon = Icons.Rounded.DirectionsCar,
            title = "Prêt à partir",
            subtitle = "Nouveau trajet",
            statusLabel = "Prêt"
        )
        DrivingState.OUTWARD_ACTIVE -> TripHeaderData(
            icon = Icons.Rounded.DirectionsCar,
            title = "Trajet en cours",
            subtitle = "Trajet aller",
            statusLabel = "Actif"
        )
        DrivingState.ARRIVED -> TripHeaderData(
            icon = Icons.Rounded.Flag,
            title = "Arrivée confirmée",
            subtitle = "Trajet aller terminé",
            statusLabel = "Décision"
        )
        DrivingState.RETURN_READY -> TripHeaderData(
            icon = Icons.Rounded.UTurnLeft,
            title = "Retour prêt",
            subtitle = "Aller-retour",
            statusLabel = "Prêt"
        )
        DrivingState.RETURN_ACTIVE -> TripHeaderData(
            icon = Icons.Rounded.KeyboardReturn,
            title = "Retour en cours",
            subtitle = "Trajet retour",
            statusLabel = "Actif"
        )
        DrivingState.COMPLETED -> TripHeaderData(
            icon = Icons.Rounded.CheckCircle,
            title = "Trajets sauvegardés",
            subtitle = "Session terminée",
            statusLabel = "Terminé"
        )
    }
}

@Composable
internal fun colorsForState(state: DrivingState): StepColors {
    val scheme = MaterialTheme.colorScheme
    return when (state) {
        DrivingState.IDLE -> StepColors(
            headerContainer = scheme.primaryContainer,
            onHeaderContainer = scheme.onPrimaryContainer,
            statusColor = scheme.primary,
            cardContainer = scheme.surface
        )
        DrivingState.OUTWARD_ACTIVE -> StepColors(
            headerContainer = scheme.secondaryContainer,
            onHeaderContainer = scheme.onSecondaryContainer,
            statusColor = scheme.secondary,
            cardContainer = scheme.surface
        )
        DrivingState.ARRIVED -> StepColors(
            headerContainer = scheme.secondaryContainer,
            onHeaderContainer = scheme.onSecondaryContainer,
            statusColor = scheme.secondary,
            cardContainer = scheme.surface
        )
        DrivingState.RETURN_READY -> StepColors(
            headerContainer = scheme.primaryContainer,
            onHeaderContainer = scheme.onPrimaryContainer,
            statusColor = scheme.primary,
            cardContainer = scheme.surface
        )
        DrivingState.RETURN_ACTIVE -> StepColors(
            headerContainer = scheme.tertiaryContainer,
            onHeaderContainer = scheme.onTertiaryContainer,
            statusColor = scheme.tertiary,
            cardContainer = scheme.surface
        )
        DrivingState.COMPLETED -> StepColors(
            headerContainer = scheme.surfaceVariant,
            onHeaderContainer = scheme.onSurfaceVariant,
            statusColor = scheme.primary,
            cardContainer = scheme.surface
        )
    }
}

internal fun findTripForState(state: DrivingState, trips: List<Trip>): Trip? {
    return when (state) {
        DrivingState.OUTWARD_ACTIVE -> trips.find {
            it.endKm == null && !it.isReturn && it.status == TripStatus.ACTIVE
        }
        DrivingState.ARRIVED -> trips.find {
            !it.isReturn && it.endKm != null &&
                trips.none { return@none it.pairedTripId == trips.find { !it.isReturn }?.id && it.isReturn }
        }
        DrivingState.RETURN_READY -> trips.find { it.isReturn && it.status == TripStatus.READY }
        DrivingState.RETURN_ACTIVE -> trips.find {
            it.isReturn && it.endKm == null && it.status == TripStatus.ACTIVE
        }
        else -> null
    }
}

internal fun formatTripTime(trip: Trip): String {
    return formatTimeRange(trip.startTime, trip.endTime, ongoingLabel = "En cours")
}
