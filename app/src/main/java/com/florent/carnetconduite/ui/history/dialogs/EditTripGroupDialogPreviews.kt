package com.florent.carnetconduite.ui.history.dialogs

import androidx.compose.runtime.Composable
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@DevicePreview
@Composable
private fun EditTripGroupDialogPreview() {
    val previewGroup = TripGroup(
        outward = Trip(
            id = 310L,
            startKm = 12000,
            endKm = 12180,
            startPlace = "Bordeaux - Centre",
            endPlace = "Pessac",
            startTime = 1704445200000L,
            endTime = 1704448800000L,
            isReturn = false,
            pairedTripId = 310L,
            status = TripStatus.COMPLETED,
            conditions = "Soleil",
            guide = "1",
            date = "2024-01-05"
        ),
        returnTrip = Trip(
            id = 311L,
            startKm = 12180,
            endKm = 12360,
            startPlace = "Pessac",
            endPlace = "Bordeaux - Centre",
            startTime = 1704450600000L,
            endTime = 1704454200000L,
            isReturn = true,
            pairedTripId = 310L,
            status = TripStatus.COMPLETED,
            conditions = "Soleil",
            guide = "1",
            date = "2024-01-05"
        ),
        seanceNumber = 9
    )

    RoadbookTheme {
        EditTripGroupDialog(
            group = previewGroup,
            onDismiss = {},
            onEditStartTime = { _, _ -> },
            onEditEndTime = { _, _ -> },
            onEditStartKm = { _, _ -> },
            onEditEndKm = { _, _ -> },
            onEditDate = { _, _ -> },
            onEditConditions = { _, _ -> }
        )
    }
}
