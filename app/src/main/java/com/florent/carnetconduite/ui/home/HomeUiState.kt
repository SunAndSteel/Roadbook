package com.florent.carnetconduite.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import com.florent.carnetconduite.ui.home.sections.IdleFormState
import com.florent.carnetconduite.ui.home.sections.OutwardActiveFormState
import com.florent.carnetconduite.ui.home.sections.ReturnActiveFormState
import com.florent.carnetconduite.ui.home.sections.ReturnReadyFormState

@Composable
fun rememberHomeUiState(): HomeUiState = remember { HomeUiState() }

// État centralisé pour l'écran Home (un seul point de vérité côté UI).
@Stable
class HomeUiState {
    // Les états de formulaire sont partagés entre sections et actions sticky.
    val idle = IdleFormState()
    val outward = OutwardActiveFormState()
    val arrival = ArrivalInputsState()
    val returnReady = ReturnReadyFormState()
    val returnActive = ReturnActiveFormState()
}

@Stable
class ArrivalInputsState {
    // Champs saisis par l'utilisateur pour l'arrivée (sticky ou formulaire).
    var arrivalKmText by mutableStateOf("")
    var arrivalPlace by mutableStateOf("")

    // Flags de dialogues d'édition (heure/km).
    var showEditArrivalKm by mutableStateOf(false)
    var showEditArrivalTime by mutableStateOf(false)
}
