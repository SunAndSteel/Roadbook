package com.florent.carnetconduite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
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
