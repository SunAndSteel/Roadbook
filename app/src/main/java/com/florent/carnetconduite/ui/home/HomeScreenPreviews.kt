package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.home.components.TripHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryHeader
import com.florent.carnetconduite.ui.home.components.TripSummaryVariant
import com.florent.carnetconduite.ui.home.mapper.colorsForState
import com.florent.carnetconduite.ui.home.mapper.headerForState
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

private const val PreviewStartTime = 1704445200000L
private const val PreviewEndTime = PreviewStartTime + 45 * 60 * 1000L

private val previewOutwardActiveTrip = Trip(
    id = 2L,
    startKm = 12500,
    startPlace = "Bordeaux - Centre",
    startTime = PreviewStartTime,
    status = TripStatus.ACTIVE,
    conditions = "Soleil",
    guide = "2",
    date = "2024-01-05"
)

private val previewArrivedTrip = Trip(
    id = 3L,
    startKm = 12500,
    endKm = 12620,
    startPlace = "Bordeaux - Centre",
    endPlace = "Pessac",
    startTime = PreviewStartTime,
    endTime = PreviewEndTime,
    status = TripStatus.COMPLETED,
    conditions = "Soleil",
    guide = "2",
    date = "2024-01-05"
)

private val previewReturnReadyTrip = Trip(
    id = 4L,
    startKm = 12620,
    startPlace = "Pessac",
    startTime = PreviewEndTime + 10 * 60 * 1000L,
    isReturn = true,
    pairedTripId = 3L,
    status = TripStatus.READY,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

private val previewReturnActiveTrip = Trip(
    id = 5L,
    startKm = 12620,
    endKm = 12810,
    startPlace = "Pessac",
    endPlace = "Bordeaux - Centre",
    startTime = PreviewEndTime + 10 * 60 * 1000L,
    endTime = PreviewEndTime + 55 * 60 * 1000L,
    isReturn = true,
    pairedTripId = 3L,
    status = TripStatus.ACTIVE,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

private val previewTripGroups = listOf(
    TripGroup(
        outward = previewArrivedTrip.copy(status = TripStatus.COMPLETED),
        returnTrip = previewReturnActiveTrip.copy(status = TripStatus.COMPLETED),
        seanceNumber = 12
    )
)

@DevicePreview
@Composable
fun HomeScreenIdlePreview() {
    RoadbookTheme {
        HomeUnifiedPreviewLayout(drivingState = DrivingState.IDLE)
    }
}

@DevicePreview
@Composable
fun HomeScreenOutwardActivePreview() {
    RoadbookTheme {
        HomeUnifiedPreviewLayout(
            drivingState = DrivingState.OUTWARD_ACTIVE,
            headerTrip = previewOutwardActiveTrip
        )
    }
}

@DevicePreview
@Composable
fun HomeScreenArrivedPreview() {
    RoadbookTheme {
        HomeUnifiedPreviewLayout(
            drivingState = DrivingState.ARRIVED,
            headerTrip = previewArrivedTrip
        )
    }
}

@DevicePreview
@Composable
fun HomeScreenReturnReadyPreview() {
    RoadbookTheme {
        HomeUnifiedPreviewLayout(
            drivingState = DrivingState.RETURN_READY,
            headerTrip = previewReturnReadyTrip
        )
    }
}

@DevicePreview
@Composable
fun HomeScreenReturnActivePreview() {
    RoadbookTheme {
        HomeUnifiedPreviewLayout(
            drivingState = DrivingState.RETURN_ACTIVE,
            headerTrip = previewReturnActiveTrip
        )
    }
}

@DevicePreview
@Composable
fun HomeScreenCompletedPreview() {
    RoadbookTheme {
        HomeUnifiedPreviewLayout(drivingState = DrivingState.COMPLETED)
    }
}

@Composable
private fun HomeUnifiedPreviewLayout(
    drivingState: DrivingState,
    headerTrip: Trip? = null
) {
    val ui = rememberHomeUnifiedState().apply {
        idle.startKm = "12500"
        idle.startPlace = "Bordeaux - Centre"
        idle.conditions = "Ensoleillé"
        idle.guide = "2"
        idle.advancedExpanded = true
        idle.guideExpanded = false

        outward.endKm = "12620"
        outward.endPlace = "Pessac"

        returnReady.editedStartKm = "12620"
        returnActive.endKm = "12810"
    }

    val colors = colorsForState(drivingState)
    val header = headerForState(drivingState)
    val showActiveIndicator =
        drivingState == DrivingState.OUTWARD_ACTIVE || drivingState == DrivingState.RETURN_ACTIVE

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TripHeader(
            header,
            colors.statusColor,
            colors.headerContainer,
            colors.onHeaderContainer,
            showActiveIndicator
        )

        if (headerTrip != null && drivingState != DrivingState.COMPLETED) {
            TripSummaryHeader(
                headerTrip,
                TripSummaryVariant.Minimal,
                header.statusLabel,
                colors.statusColor,
                false
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeFormSection(
                drivingState = drivingState,
                ui = ui,
                outwardTrip = previewOutwardActiveTrip,
                arrivedTrip = previewArrivedTrip,
                returnReadyTrip = previewReturnReadyTrip,
                returnActiveTrip = previewReturnActiveTrip,
                tripGroups = previewTripGroups
            )
        }
    }
}
