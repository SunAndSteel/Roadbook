package com.florent.carnetconduite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.components.TripGroupsList

@Composable
fun HistoryScreen(viewModel: DrivingViewModel) {
    val tripGroups by viewModel.tripGroups.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TripGroupsList(
            tripGroups = tripGroups.filter { it.isComplete },
            onDelete = { viewModel.deleteTripGroup(it) },
            onEditStartTime = { trip, time -> viewModel.editStartTime(trip.id, time) },
            onEditEndTime = { trip, time -> viewModel.editEndTime(trip.id, time) },
            onEditStartKm = { trip, km -> viewModel.editStartKm(trip.id, km) },
            onEditEndKm = { trip, km -> viewModel.editEndKm(trip.id, km) }
        )
    }
}