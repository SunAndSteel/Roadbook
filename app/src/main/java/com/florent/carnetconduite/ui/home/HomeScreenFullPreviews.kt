package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.CompletedScreenContent
import com.florent.carnetconduite.ui.home.screens.CompletedScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.IdleScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenPrimaryAction
import com.florent.carnetconduite.ui.home.screens.rememberArrivedScreenState
import com.florent.carnetconduite.ui.home.screens.rememberIdleScreenState
import com.florent.carnetconduite.ui.home.screens.rememberOutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnReadyScreenState
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@Composable
private fun HomeScreenPreviewLayout(
    drivingState: DrivingState,
    trips: List<Trip>,
    tripGroups: List<TripGroup>
) {
    val scrollState = rememberScrollState()
    val idleState = rememberIdleScreenState().apply {
        startKm = "12500"
        startPlace = "Bordeaux - Centre"
        conditions = "Ensoleillé"
        guide = "2"
        advancedExpanded = true
        guideExpanded = false
    }
    val outwardState = rememberOutwardActiveScreenState().apply {
        endKm = "12620"
        endPlace = "Pessac"
    }
    val arrivedState = rememberArrivedScreenState()
    val returnReadyState = rememberReturnReadyScreenState().apply {
        editedStartKm = "12620"
    }
    val returnActiveState = rememberReturnActiveScreenState().apply {
        endKm = "12810"
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentTrip = findTripForState(drivingState, trips)
            val header = headerForState(drivingState)
            val colors = colorsForState(drivingState)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TripHeader(
                    header = header,
                    statusColor = colors.statusColor,
                    containerColor = colors.headerContainer,
                    onContainerColor = colors.onHeaderContainer,
                    showActiveIndicator = drivingState == DrivingState.OUTWARD_ACTIVE || drivingState == DrivingState.RETURN_ACTIVE
                )
                TripSummary(
                    trip = currentTrip,
                    stateLabel = header.subtitle,
                    accentColor = colors.statusColor,
                    showIdleSetup = drivingState == DrivingState.IDLE,
                    idleState = idleState
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = colors.cardContainer
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (drivingState) {
                            DrivingState.IDLE -> IdleScreenContent(state = idleState)
                            DrivingState.OUTWARD_ACTIVE -> currentTrip?.let {
                                OutwardActiveScreenContent(trip = it, state = outwardState)
                            }
                            DrivingState.ARRIVED -> currentTrip?.let {
                                ArrivedScreenContent(trip = it, state = arrivedState)
                            }
                            DrivingState.RETURN_READY -> currentTrip?.let {
                                ReturnReadyScreenContent(trip = it, state = returnReadyState)
                            }
                            DrivingState.RETURN_ACTIVE -> currentTrip?.let {
                                ReturnActiveScreenContent(trip = it, state = returnActiveState)
                            }
                            DrivingState.COMPLETED -> CompletedScreenContent(tripGroups = tripGroups)
                        }
                    }
                }

                PrimaryActionArea {
                    when (drivingState) {
                        DrivingState.IDLE -> IdleScreenPrimaryAction(
                            state = idleState,
                            onStartOutward = { _, _, _, _ -> }
                        )
                        DrivingState.OUTWARD_ACTIVE -> currentTrip?.let {
                            OutwardActiveScreenPrimaryAction(
                                trip = it,
                                state = outwardState,
                                onFinishOutward = { _, _, _ -> }
                            )
                        }
                        DrivingState.ARRIVED -> currentTrip?.let {
                            ArrivedScreenPrimaryAction(
                                trip = it,
                                onPrepareReturn = {},
                                onConfirmSimpleTrip = {}
                            )
                        }
                        DrivingState.RETURN_READY -> currentTrip?.let {
                            ReturnReadyScreenPrimaryAction(
                                trip = it,
                                state = returnReadyState,
                                onStartReturn = { _, _ -> },
                                onCancelReturn = {}
                            )
                        }
                        DrivingState.RETURN_ACTIVE -> currentTrip?.let {
                            ReturnActiveScreenPrimaryAction(
                                trip = it,
                                state = returnActiveState,
                                onFinishReturn = { _, _ -> }
                            )
                        }
                        DrivingState.COMPLETED -> CompletedScreenPrimaryAction()
                    }
                }
            }
        }
    }
}

@DevicePreview
@Composable
private fun HomeScreenFullIdlePreview() {
    RoadbookTheme {
        HomeScreenPreviewLayout(
            drivingState = DrivingState.IDLE,
            trips = emptyList(),
            tripGroups = previewTripGroups
        )
    }
}

@DevicePreview
@Composable
private fun HomeScreenFullOutwardActivePreview() {
    RoadbookTheme {
        HomeScreenPreviewLayout(
            drivingState = DrivingState.OUTWARD_ACTIVE,
            trips = listOf(previewOutwardActiveTrip),
            tripGroups = previewTripGroups
        )
    }
}

@DevicePreview
@Composable
private fun HomeScreenFullArrivedPreview() {
    RoadbookTheme {
        HomeScreenPreviewLayout(
            drivingState = DrivingState.ARRIVED,
            trips = listOf(previewArrivedTrip),
            tripGroups = previewTripGroups
        )
    }
}

@DevicePreview
@Composable
private fun HomeScreenFullReturnReadyPreview() {
    RoadbookTheme {
        HomeScreenPreviewLayout(
            drivingState = DrivingState.RETURN_READY,
            trips = listOf(previewReturnReadyTrip),
            tripGroups = previewTripGroups
        )
    }
}

@DevicePreview
@Composable
private fun HomeScreenFullReturnActivePreview() {
    RoadbookTheme {
        HomeScreenPreviewLayout(
            drivingState = DrivingState.RETURN_ACTIVE,
            trips = listOf(previewReturnActiveTrip),
            tripGroups = previewTripGroups
        )
    }
}

@DevicePreview
@Composable
private fun HomeScreenFullCompletedPreview() {
    RoadbookTheme {
        HomeScreenPreviewLayout(
            drivingState = DrivingState.COMPLETED,
            trips = listOf(previewArrivedTrip, previewReturnActiveTrip),
            tripGroups = previewTripGroups
        )
    }
}
