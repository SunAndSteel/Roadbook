package com.florent.carnetconduite.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.florent.carnetconduite.ui.home.screens.ArrivedDecisionState
import com.florent.carnetconduite.ui.home.screens.IdleFormState
import com.florent.carnetconduite.ui.home.screens.OutwardActiveFormState
import com.florent.carnetconduite.ui.home.screens.ReturnActiveFormState
import com.florent.carnetconduite.ui.home.screens.ReturnReadyFormState
import com.florent.carnetconduite.ui.home.screens.rememberArrivedDecisionState
import com.florent.carnetconduite.ui.home.screens.rememberIdleFormState
import com.florent.carnetconduite.ui.home.screens.rememberOutwardActiveFormState
import com.florent.carnetconduite.ui.home.screens.rememberReturnActiveFormState
import com.florent.carnetconduite.ui.home.screens.rememberReturnReadyFormState

@Stable
data class HomeUnifiedState(
    val idle: IdleFormState,
    val outward: OutwardActiveFormState,
    val arrived: ArrivedDecisionState,
    val returnReady: ReturnReadyFormState,
    val returnActive: ReturnActiveFormState
)

@Composable
fun rememberHomeUnifiedState(): HomeUnifiedState {
    return HomeUnifiedState(
        idle = rememberIdleFormState(),
        outward = rememberOutwardActiveFormState(),
        arrived = rememberArrivedDecisionState(),
        returnReady = rememberReturnReadyFormState(),
        returnActive = rememberReturnActiveFormState()
    )
}
