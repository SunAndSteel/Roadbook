package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenContent
import com.florent.carnetconduite.ui.home.screens.CompletedScreenPrimaryAction
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

/**
 * Previews alignés avec les signatures actuellement utilisées dans HomeScreen :
 * - OutwardActiveScreenContent(state)
 * - ArrivedScreenContent()
 * - ReturnActiveScreenContent(state)
 * - ReturnReadyScreenContent(trip, state)
 */
private const val PreviewStartTime = 1704445200000L
private const val PreviewEndTime = PreviewStartTime + 45 * 60 * 1000L

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
        IdleScreenContent(state)
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
        OutwardActiveScreenContent(state)
    }
}

@DevicePreview
@Composable
fun HomeScreenArrivedPreview() {
    RoadbookTheme {
        ArrivedScreenContent()
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
        ReturnReadyScreenContent(previewReturnReadyTrip, state)
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
        ReturnActiveScreenContent(state)
    }
}

@DevicePreview
@Composable
fun HomeScreenCompletedPreview() {
    RoadbookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // CompletedScreenContent dépend du ViewModel dans ton code actuel.
            // Preview fiable => on prévisualise l'action area.
            CompletedScreenPrimaryAction()
        }
    }
}
