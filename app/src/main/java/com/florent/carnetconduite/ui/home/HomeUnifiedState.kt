package com.florent.carnetconduite.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.florent.carnetconduite.ui.home.screens.ArrivedScreenState
import com.florent.carnetconduite.ui.home.screens.IdleScreenState
import com.florent.carnetconduite.ui.home.screens.OutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.ReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.ReturnReadyScreenState
import com.florent.carnetconduite.ui.home.screens.rememberArrivedScreenState
import com.florent.carnetconduite.ui.home.screens.rememberIdleScreenState
import com.florent.carnetconduite.ui.home.screens.rememberOutwardActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnActiveScreenState
import com.florent.carnetconduite.ui.home.screens.rememberReturnReadyScreenState

@Stable
data class HomeUnifiedState(
    val idle: IdleScreenState,
    val outward: OutwardActiveScreenState,
    val arrived: ArrivedScreenState,
    val returnReady: ReturnReadyScreenState,
    val returnActive: ReturnActiveScreenState
)

@Composable
fun rememberHomeUnifiedState(): HomeUnifiedState {
    return HomeUnifiedState(
        idle = rememberIdleScreenState(),
        outward = rememberOutwardActiveScreenState(),
        arrived = rememberArrivedScreenState(),
        returnReady = rememberReturnReadyScreenState(),
        returnActive = rememberReturnActiveScreenState()
    )
}
