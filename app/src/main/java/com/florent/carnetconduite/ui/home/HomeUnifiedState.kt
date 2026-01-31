package com.florent.carnetconduite.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.florent.carnetconduite.ui.home.sections.IdleFormState
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormState
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormState
import com.florent.carnetconduite.ui.home.sections.ReturnReadyFormState

@Composable
fun rememberHomeUnifiedState(): HomeUnifiedState = remember { HomeUnifiedState() }

// État centralisé pour l'écran Home (un seul point de vérité côté UI).
class HomeUnifiedState {
    // Les états de formulaire sont partagés entre sections et actions sticky.
    val idle = IdleFormState()
    val outward = OutwardActiveFormState()
    val arrived = ArrivedUiState()
    val returnReady = ReturnReadyFormState()
    val returnActive = ReturnActiveFormState()
}

class ArrivedUiState {
    // Champs saisis par l'utilisateur pour l'arrivée (sticky ou formulaire).
    var endKmText by mutableStateOf("")
    var arrivalPlace by mutableStateOf("")

    // Flags de dialogues d'édition (heure/km).
    var showEditEndKm by mutableStateOf(false)
    var showEditEndTime by mutableStateOf(false)
}
