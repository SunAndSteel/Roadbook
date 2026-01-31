package com.florent.carnetconduite.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.florent.carnetconduite.ui.home.sections.ArrivedDecisionState
import com.florent.carnetconduite.ui.home.sections.IdleFormState
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormState
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormState
import com.florent.carnetconduite.ui.home.sections.ReturnReadyFormState
import com.florent.carnetconduite.ui.home.sections.rememberArrivedDecisionState
import com.florent.carnetconduite.ui.home.sections.rememberIdleFormState
import com.florent.carnetconduite.ui.home.sections.rememberOutwardActiveFormState
import com.florent.carnetconduite.ui.home.sections.rememberReturnActiveFormState
import com.florent.carnetconduite.ui.home.sections.rememberReturnReadyFormState

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
