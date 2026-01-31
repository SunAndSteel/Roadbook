package com.florent.carnetconduite.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberHomeUnifiedState(): HomeUnifiedState = remember { HomeUnifiedState() }

@Stable
class HomeUnifiedState {
    val idle = IdleUiState()
    val outward = OutwardUiState()
    val arrived = ArrivedUiState()
    val returnReady = ReturnReadyUiState()
    val returnActive = ReturnActiveUiState()
}

@Stable
class IdleUiState {
    // garde tes champs existants si tu en as
}

@Stable
class OutwardUiState {
    // garde tes champs existants + tes flags de dialogs
    var showEditStartTime by mutableStateOf(false)
    var showEditEndTime by mutableStateOf(false)
}

@Stable
class ArrivedUiState {
    // ✅ Inputs ARRIVÉE (sticky bottom)
    var endKmText by mutableStateOf("")
    var arrivalPlace by mutableStateOf("")

    // flags dialogs existants
    var showEditEndKm by mutableStateOf(false)
    var showEditEndTime by mutableStateOf(false)
}

@Stable
class ReturnReadyUiState {
    // garde tes champs existants si tu en as
}

@Stable
class ReturnActiveUiState {
    // garde tes champs existants + flags dialogs
    var showEditStartTime by mutableStateOf(false)
    var showEditEndTime by mutableStateOf(false)
}
