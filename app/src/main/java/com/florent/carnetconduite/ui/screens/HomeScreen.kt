package com.florent.carnetconduite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.ui.DrivingViewModel
import com.florent.carnetconduite.ui.components.StatsHeader

@Composable
fun HomeScreen(viewModel: DrivingViewModel) {
    val tripGroups by viewModel.tripGroups.collectAsState()
    val drivingState by viewModel.drivingState.collectAsState()
    val activeTrip by viewModel.activeTrip.collectAsState()
    val arrivedTrip by viewModel.arrivedTrip.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats header
        val completedCount = tripGroups.count { it.isComplete }
        StatsHeader(totalKms = viewModel.getTotalKms(), totalTrips = completedCount)

        Spacer(modifier = Modifier.height(16.dp))

        // État actuel + Actions
        when (drivingState) {
            DrivingState.IDLE -> IdleScreen(viewModel)
            DrivingState.OUTWARD_ACTIVE -> activeTrip?.let { OutwardActiveScreen(it, viewModel) }
            DrivingState.ARRIVED -> arrivedTrip?.let { ArrivedScreen(it, viewModel) }
            DrivingState.RETURN_READY -> {
                tripGroups.flatMap { listOfNotNull(it.returnTrip) }
                    .firstOrNull { it.status == "READY" && it.isReturn }?.let {
                        ReturnReadyScreen(it, viewModel)
                    }
            }
            DrivingState.RETURN_ACTIVE -> activeTrip?.let { ReturnActiveScreen(it, viewModel) }
            DrivingState.COMPLETED -> CompletedScreen()
        }
    }
}
