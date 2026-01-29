package com.florent.carnetconduite.ui.history.components

import androidx.compose.runtime.Composable
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@DevicePreview
@Composable
private fun TripGroupCardPreview() {
    val previewTrip = Trip(
        id = 42L,
        startKm = 12000,
        endKm = 12240,
        startPlace = "Bordeaux - Centre",
        endPlace = "Pessac",
        startTime = 1704445200000L,
        endTime = 1704448800000L,
        isReturn = false,
        pairedTripId = 42L,
        status = TripStatus.COMPLETED,
        conditions = "Clair",
        guide = "2",
        date = "2024-01-05"
    )
    val previewGroup = TripGroup(
        outward = previewTrip,
        returnTrip = previewTrip.copy(
            id = 43L,
            startKm = 12240,
            endKm = 12460,
            startPlace = "Pessac",
            endPlace = "Bordeaux - Centre",
            startTime = 1704450600000L,
            endTime = 1704453600000L,
            isReturn = true,
            pairedTripId = 42L
        ),
        seanceNumber = 8
    )

    RoadbookTheme {
        TripGroupCard(
            tripGroup = previewGroup,
            onEdit = {},
            onDelete = {}
        )
    }
}
