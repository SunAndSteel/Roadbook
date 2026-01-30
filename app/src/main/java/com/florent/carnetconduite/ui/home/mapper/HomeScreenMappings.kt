package com.florent.carnetconduite.ui.home.mapper

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus

internal data class StepColors(
    val headerContainer: Color,
    val onHeaderContainer: Color,
    val statusColor: Color,
    val cardContainer: Color
)

@Composable
internal fun colorsForState(state: DrivingState): StepColors {
    val scheme = MaterialTheme.colorScheme
    return when (state) {
        DrivingState.IDLE -> StepColors(
            headerContainer = scheme.primaryContainer,
            onHeaderContainer = scheme.onPrimaryContainer,
            statusColor = scheme.primary,
            cardContainer = scheme.primaryContainer
        )
        DrivingState.OUTWARD_ACTIVE -> StepColors(
            headerContainer = scheme.secondaryContainer,
            onHeaderContainer = scheme.onSecondaryContainer,
            statusColor = scheme.secondary,
            cardContainer = scheme.secondaryContainer
        )
        DrivingState.ARRIVED -> StepColors(
            headerContainer = scheme.secondaryContainer,
            onHeaderContainer = scheme.onSecondaryContainer,
            statusColor = scheme.secondary,
            cardContainer = scheme.secondaryContainer
        )
        DrivingState.RETURN_READY -> StepColors(
            headerContainer = scheme.primaryContainer,
            onHeaderContainer = scheme.onPrimaryContainer,
            statusColor = scheme.primary,
            cardContainer = scheme.primaryContainer
        )
        DrivingState.RETURN_ACTIVE -> StepColors(
            headerContainer = scheme.tertiaryContainer,
            onHeaderContainer = scheme.onTertiaryContainer,
            statusColor = scheme.tertiary,
            cardContainer = scheme.tertiaryContainer
        )
        DrivingState.COMPLETED -> StepColors(
            headerContainer = scheme.surfaceVariant,
            onHeaderContainer = scheme.onSurfaceVariant,
            statusColor = scheme.primary,
            cardContainer = scheme.surfaceVariant
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
