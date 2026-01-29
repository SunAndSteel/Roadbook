package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.CompletedScreenContent
import com.florent.carnetconduite.ui.home.screens.CompletedScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.IdleScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenState
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenState
import com.florent.carnetconduite.ui.home.screens.rememberArrivedScreenState
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

private const val PreviewStartTime = 1704445200000L
private const val PreviewEndTime = PreviewStartTime + 45 * 60 * 1000L

private val previewOutwardActiveTrip = Trip(
    id = 1L,
    startKm = 12500,
    startPlace = "Bordeaux - Centre",
    startTime = PreviewStartTime,
    isReturn = false,
    status = TripStatus.ACTIVE,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

private val previewArrivedTrip = Trip(
    id = 2L,
    startKm = 12500,
    endKm = 12620,
    startPlace = "Bordeaux - Centre",
    endPlace = "Pessac",
    startTime = PreviewStartTime,
    endTime = PreviewEndTime,
    isReturn = false,
    pairedTripId = 2L,
    status = TripStatus.COMPLETED,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

private val previewReturnReadyTrip = Trip(
    id = 3L,
    startKm = 12620,
    startPlace = "Pessac",
    startTime = PreviewEndTime + 10 * 60 * 1000L,
    isReturn = true,
    pairedTripId = 2L,
    status = TripStatus.READY,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

private val previewReturnActiveTrip = Trip(
    id = 4L,
    startKm = 12620,
    startPlace = "Pessac",
    startTime = PreviewEndTime + 10 * 60 * 1000L,
    isReturn = true,
    pairedTripId = 2L,
    status = TripStatus.ACTIVE,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

private val previewTripGroups = listOf(
    TripGroup(
        outward = previewArrivedTrip,
        returnTrip = previewReturnActiveTrip.copy(
            id = 5L,
            endKm = 12810,
            endPlace = "Bordeaux - Centre",
            endTime = PreviewEndTime + 70 * 60 * 1000L,
            status = TripStatus.COMPLETED
        ),
        seanceNumber = 12
    ),
    TripGroup(
        outward = previewArrivedTrip.copy(
            id = 6L,
            startTime = PreviewStartTime - 3 * 24 * 60 * 60 * 1000L,
            endTime = PreviewStartTime - 3 * 24 * 60 * 60 * 1000L + 35 * 60 * 1000L,
            startKm = 12000,
            endKm = 12110,
            startPlace = "Mérignac",
            endPlace = "Talence",
            status = TripStatus.COMPLETED
        ),
        returnTrip = null,
        seanceNumber = 11
    )
)

@DevicePreview
@Composable
fun HomeScreenIdlePreview() {
    RoadbookTheme {
        val state = remember {
            IdleScreenState().apply {
                startKm = "12500"
                startPlace = "Bordeaux - Centre"
                conditions = "Ensoleillé"
                guide = "2"
                advancedExpanded = true
                guideExpanded = false
            }
        }
        IdleScreenContent(state = state)
    }
}

@DevicePreview
@Composable
fun HomeScreenOutwardActivePreview() {
    RoadbookTheme {
        val state = remember {
            OutwardActiveScreenState().apply {
                endKm = "12620"
                endPlace = "Pessac"
            }
        }
        OutwardActiveScreenContent(trip = previewOutwardActiveTrip, state = state)
    }
}

@DevicePreview
@Composable
fun HomeScreenArrivedPreview() {
    RoadbookTheme {
        val state = rememberArrivedScreenState()
        ArrivedScreenContent(trip = previewArrivedTrip, state = state)
    }
}

@DevicePreview
@Composable
fun HomeScreenReturnReadyPreview() {
    RoadbookTheme {
        val state = remember {
            ReturnReadyScreenState().apply {
                editedStartKm = "12620"
            }
        }
        ReturnReadyScreenContent(trip = previewReturnReadyTrip, state = state)
    }
}

@DevicePreview
@Composable
fun HomeScreenReturnActivePreview() {
    RoadbookTheme {
        val state = remember {
            ReturnActiveScreenState().apply {
                endKm = "12810"
            }
        }
        ReturnActiveScreenContent(trip = previewReturnActiveTrip, state = state)
    }
}

@DevicePreview
@Composable
fun HomeScreenCompletedPreview() {
    RoadbookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CompletedScreenContent(tripGroups = previewTripGroups)
            CompletedScreenPrimaryAction()
        }
    }
}
