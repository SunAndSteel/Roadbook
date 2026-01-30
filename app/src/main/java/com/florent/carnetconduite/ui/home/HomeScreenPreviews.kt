package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.home.components.PrimaryActionCard
import com.florent.carnetconduite.ui.home.components.PrimaryActionInfoLine
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.ArrivedStatsSection
import com.florent.carnetconduite.ui.home.screens.CompletedScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenContent
import com.florent.carnetconduite.ui.home.screens.IdleScreenState
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenContent
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenState
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme
import com.florent.carnetconduite.util.formatTime
import com.florent.carnetconduite.util.formatTimeRange

private const val PreviewStartTime = 1704445200000L
private const val PreviewEndTime = PreviewStartTime + 45 * 60 * 1000L

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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IdleScreenContent(state = state)
            PrimaryActionCard(
                title = "Prêt à partir",
                subtitle = "Aucun trajet en cours",
                icon = androidx.compose.material.icons.Icons.Rounded.DirectionsCar,
                infoLines = emptyList(),
                actionLabel = "Démarrer",
                onAction = {}
            )
        }
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutwardActiveScreenContent(state = state)
            PrimaryActionCard(
                title = "Trajet en cours",
                subtitle = "Depuis ${formatTime(PreviewStartTime)}",
                icon = androidx.compose.material.icons.Icons.Rounded.DirectionsCar,
                infoLines = listOf(
                    PrimaryActionInfoLine(
                        icon = androidx.compose.material.icons.Icons.Rounded.LocationOn,
                        text = "Bordeaux - Centre → ${state.endPlace}"
                    ),
                    PrimaryActionInfoLine(
                        icon = androidx.compose.material.icons.Icons.Rounded.Schedule,
                        text = formatTime(PreviewStartTime)
                    )
                ),
                actionLabel = "Arrivée",
                onAction = {}
            )
        }
    }
}

@DevicePreview
@Composable
fun HomeScreenArrivedPreview() {
    RoadbookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ArrivedScreenContent()
            ArrivedStatsSection(trip = previewArrivedTrip, onEditDistance = null)
            PrimaryActionCard(
                title = "Arrivée confirmée",
                subtitle = "Trajet aller terminé",
                icon = androidx.compose.material.icons.Icons.Rounded.Flag,
                infoLines = listOf(
                    PrimaryActionInfoLine(
                        icon = androidx.compose.material.icons.Icons.Rounded.LocationOn,
                        text = "Bordeaux - Centre → Pessac"
                    ),
                    PrimaryActionInfoLine(
                        icon = androidx.compose.material.icons.Icons.Rounded.Schedule,
                        text = formatTimeRange(PreviewStartTime, PreviewEndTime, ongoingLabel = "—")
                    )
                ),
                actionLabel = "Décision",
                onAction = {}
            )
        }
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ReturnReadyScreenContent(trip = previewReturnReadyTrip, state = state)
            PrimaryActionCard(
                title = "Retour prêt",
                subtitle = "Trajet retour prêt",
                icon = androidx.compose.material.icons.Icons.Rounded.UTurnLeft,
                infoLines = emptyList(),
                actionLabel = "Décision",
                onAction = {}
            )
        }
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ReturnActiveScreenContent(state = state)
            PrimaryActionCard(
                title = "Trajet en cours",
                subtitle = "Depuis ${formatTime(PreviewEndTime)}",
                icon = androidx.compose.material.icons.Icons.Rounded.KeyboardReturn,
                infoLines = listOf(
                    PrimaryActionInfoLine(
                        icon = androidx.compose.material.icons.Icons.Rounded.LocationOn,
                        text = "Pessac → Bordeaux - Centre"
                    ),
                    PrimaryActionInfoLine(
                        icon = androidx.compose.material.icons.Icons.Rounded.Schedule,
                        text = formatTime(PreviewEndTime)
                    )
                ),
                actionLabel = "Arrivée",
                onAction = {}
            )
        }
    }
}

@DevicePreview
@Composable
fun HomeScreenCompletedPreview() {
    RoadbookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CompletedScreenContent(tripGroups = previewTripGroups)
            PrimaryActionCard(
                title = "Trajets sauvegardés",
                subtitle = "Session terminée",
                icon = androidx.compose.material.icons.Icons.Rounded.CheckCircle,
                infoLines = emptyList(),
                actionLabel = "Nouveau trajet",
                onAction = {}
            )
        }
    }
}
