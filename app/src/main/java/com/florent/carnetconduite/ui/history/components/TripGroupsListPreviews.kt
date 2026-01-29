package com.florent.carnetconduite.ui.history.components

import androidx.compose.runtime.Composable
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

private val previewTripGroups = listOf(
    TripGroup(
        outward = Trip(
            id = 201L,
            startKm = 9800,
            endKm = 10040,
            startPlace = "Bordeaux - Centre",
            endPlace = "Mérignac",
            startTime = 1704445200000L,
            endTime = 1704448800000L,
            isReturn = false,
            pairedTripId = 201L,
            status = TripStatus.COMPLETED,
            conditions = "Clair",
            guide = "1",
            date = "2024-01-10"
        ),
        returnTrip = Trip(
            id = 202L,
            startKm = 10040,
            endKm = 10260,
            startPlace = "Mérignac",
            endPlace = "Bordeaux - Centre",
            startTime = 1704450600000L,
            endTime = 1704454200000L,
            isReturn = true,
            pairedTripId = 201L,
            status = TripStatus.COMPLETED,
            conditions = "Clair",
            guide = "1",
            date = "2024-01-10"
        ),
        seanceNumber = 7
    )
)

@DevicePreview
@Composable
private fun TripGroupsListEmptyPreview() {
    RoadbookTheme {
        TripGroupsList(
            tripGroups = emptyList(),
            onDelete = {},
            onEditStartTime = { _, _ -> },
            onEditEndTime = { _, _ -> },
            onEditStartKm = { _, _ -> },
            onEditEndKm = { _, _ -> },
            onEditDate = { _, _ -> },
            onEditConditions = { _, _ -> }
        )
    }
}

@DevicePreview
@Composable
private fun TripGroupsListPreview() {
    RoadbookTheme {
        TripGroupsList(
            tripGroups = previewTripGroups,
            onDelete = {},
            onEditStartTime = { _, _ -> },
            onEditEndTime = { _, _ -> },
            onEditStartKm = { _, _ -> },
            onEditEndKm = { _, _ -> },
            onEditDate = { _, _ -> },
            onEditConditions = { _, _ -> }
        )
    }
}
