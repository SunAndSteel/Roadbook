package com.florent.carnetconduite.ui.home.components

import androidx.compose.runtime.Composable
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@DevicePreview
@Composable
private fun StatsHeaderPreview() {
    RoadbookTheme {
        StatsHeader(
            totalDistance = 820,
            totalDuration = 12 * 60 * 60 * 1000L,
            tripCount = 14,
            goalDistance = 1500
        )
    }
}
