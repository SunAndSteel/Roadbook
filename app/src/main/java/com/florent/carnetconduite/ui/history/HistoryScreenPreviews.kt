package com.florent.carnetconduite.ui.history

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

private const val PreviewStartTime = 1704445200000L
private const val PreviewEndTime = PreviewStartTime + 50 * 60 * 1000L

private val previewHistoryGroups = listOf(
    TripGroup(
        outward = Trip(
            id = 10L,
            startKm = 9820,
            endKm = 10040,
            startPlace = "Bordeaux - Centre",
            endPlace = "Mérignac",
            startTime = PreviewStartTime,
            endTime = PreviewEndTime,
            isReturn = false,
            pairedTripId = 10L,
            status = TripStatus.COMPLETED,
            conditions = "Clair",
            guide = "1",
            date = "2024-01-10"
        ),
        returnTrip = Trip(
            id = 11L,
            startKm = 10040,
            endKm = 10260,
            startPlace = "Mérignac",
            endPlace = "Bordeaux - Centre",
            startTime = PreviewEndTime + 15 * 60 * 1000L,
            endTime = PreviewEndTime + 65 * 60 * 1000L,
            isReturn = true,
            pairedTripId = 10L,
            status = TripStatus.COMPLETED,
            conditions = "Clair",
            guide = "1",
            date = "2024-01-10"
        ),
        seanceNumber = 7
    ),
    TripGroup(
        outward = Trip(
            id = 12L,
            startKm = 10500,
            endKm = 10640,
            startPlace = "Talence",
            endPlace = "Pessac",
            startTime = PreviewStartTime - 2 * 24 * 60 * 60 * 1000L,
            endTime = PreviewEndTime - 2 * 24 * 60 * 60 * 1000L,
            isReturn = false,
            pairedTripId = 12L,
            status = TripStatus.COMPLETED,
            conditions = "Nuageux",
            guide = "2",
            date = "2024-01-08"
        ),
        returnTrip = null,
        seanceNumber = 6
    )
)

private fun buildTripStats(tripGroups: List<TripGroup>): TripStats {
    val totalHours = tripGroups.sumOf { group ->
        val outwardDuration = group.outward.endTime?.let { it - group.outward.startTime } ?: 0L
        val returnDuration = group.returnTrip?.endTime?.let { it - group.returnTrip.startTime } ?: 0L
        outwardDuration + returnDuration
    } / 3600000.0
    return TripStats(
        totalTrips = tripGroups.size,
        totalKm = tripGroups.sumOf { it.totalKms },
        totalHours = totalHours
    )
}

@DevicePreview
@Composable
fun HistoryScreenEmptyPreview() {
    RoadbookTheme {
        HistoryScreenContent(
            tripGroups = emptyList(),
            tripStats = TripStats(),
            snackbarHostState = remember { SnackbarHostState() },
            onEdit = {},
            onDelete = {}
        )
    }
}

@DevicePreview
@Composable
fun HistoryScreenWithTripsPreview() {
    RoadbookTheme {
        HistoryScreenContent(
            tripGroups = previewHistoryGroups,
            tripStats = buildTripStats(previewHistoryGroups),
            snackbarHostState = remember { SnackbarHostState() },
            onEdit = {},
            onDelete = {}
        )
    }
}
