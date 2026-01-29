package com.florent.carnetconduite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.groupTrips
import com.florent.carnetconduite.domain.usecases.*
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel refactorisé selon les principes de Clean Architecture.
 *
 * AVANT : 800+ lignes avec logique métier, validation, calcul d'état
 * APRÈS : ~250 lignes - seulement coordination + gestion d'événements UI
 *
 * Responsabilités :
 * - Observer l'état via les StateFlows
 * - Coordonner les Use Cases
 * - Gérer les événements UI (toasts, dialogs, erreurs)
 */
class DrivingViewModel(
    // Use Cases injectés
    private val startOutwardUseCase: StartOutwardUseCase,
    private val finishOutwardUseCase: FinishOutwardUseCase,
    private val decideTripTypeUseCase: DecideTripTypeUseCase,
    private val startReturnUseCase: StartReturnUseCase,
    private val finishReturnUseCase: FinishReturnUseCase,
    private val cancelReturnUseCase: CancelReturnUseCase,
    private val editTripUseCase: EditTripUseCase,
    private val computeStateUseCase: ComputeDrivingStateUseCase,
    private val deleteTripGroupUseCase: DeleteTripGroupUseCase,

    // Repository pour les flows seulement
    repository: TripRepository
) : ViewModel() {

    // ====== STATE FLOWS ======

    /**
     * Liste de tous les trajets
     */
    val trips: StateFlow<List<Trip>> = repository.allTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Groupes de trajets (aller + retour)
     */
    val tripGroups: StateFlow<List<TripGroup>> = trips
        .map { tripList -> groupTrips(tripList) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Trajet actuellement actif (en cours)
     */
    val activeTrip: StateFlow<Trip?> = repository.activeTrip.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Trajet arrivé (en attente de décision simple/retour)
     */
    val arrivedTrip: StateFlow<Trip?> = trips
        .map { tripList -> findArrivedTrip(tripList) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * État de conduite calculé par le Use Case
     */
    val drivingState: StateFlow<DrivingState> = trips
        .map { tripList -> computeStateUseCase(tripList) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DrivingState.IDLE
        )

    // ====== UI EVENTS ======

    private val _uiEvents = Channel<UiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    // ====== HELPER METHODS ======

    /**
     * Trouve le trajet "arrivé" (aller terminé sans retour)
     */
    private fun findArrivedTrip(tripList: List<Trip>): Trip? {
        val latestOutward = tripList
            .filter { !it.isReturn && it.isCompleted }
            .maxByOrNull { it.id }

        if (latestOutward != null) {
            if (latestOutward.pairedTripId == latestOutward.id) {
                return null
            }
            val hasReturn = tripList.any {
                it.pairedTripId == latestOutward.id && it.isReturn
            }
            if (!hasReturn) {
                return latestOutward
            }
        }
        return null
    }

    /**
     * Envoie un événement UI
     */
    private suspend fun sendUiEvent(event: UiEvent) {
        _uiEvents.send(event)
    }

    // ====== PUBLIC METHODS (Coordination des Use Cases) ======
    // Toute la logique métier est déléguée aux Use Cases
// ====== CONTINUATION OF DrivingViewModel ======

    /**
     * Démarre un nouveau trajet aller
     */
    fun startOutward(
        startKm: Int,
        startPlace: String,
        conditions: String,
        guide: String
    ) {
        viewModelScope.launch {
            when (val result = startOutwardUseCase(startKm, startPlace, conditions, guide)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Trajet démarré"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> { /* N/A */ }
            }
        }
    }

    /**
     * Termine le trajet aller
     */
    fun finishOutward(
        tripId: Long,
        endKm: Int,
        endPlace: String
    ) {
        viewModelScope.launch {
            when (val result = finishOutwardUseCase(tripId, endKm, endPlace, allowInconsistentKm = false)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Trajet terminé"))
                }
                is Result.Error -> {
                    // Gestion spéciale pour km incohérents
                    if (result.exception is FinishOutwardUseCase.KmInconsistencyException) {
                        val ex = result.exception
                        sendUiEvent(UiEvent.ShowConfirmDialog(
                            title = "Confirmation",
                            message = result.message ?: "Confirmer l'opération ?",
                            onConfirm = { finishOutwardForce(tripId, endKm, endPlace) }
                        ))
                    } else {
                        sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Force la fin du trajet aller même avec km incohérents (après confirmation)
     */
    private fun finishOutwardForce(tripId: Long, endKm: Int, endPlace: String) {
        viewModelScope.launch {
            when (val result = finishOutwardUseCase(tripId, endKm, endPlace, allowInconsistentKm = true)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Trajet terminé"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Décide du type de trajet (simple ou aller-retour)
     */
    fun decideTripType(tripId: Long, prepareReturn: Boolean) {
        viewModelScope.launch {
            when (val result = decideTripTypeUseCase(tripId, prepareReturn)) {
                is Result.Success -> {
                    val message = if (prepareReturn) {
                        "Retour préparé"
                    } else {
                        "Trajet simple enregistré"
                    }
                    sendUiEvent(UiEvent.ShowToast(message))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    fun confirmSimpleTrip(tripId: Long) {
        decideTripType(tripId, prepareReturn = false)
    }

    fun prepareReturnTrip(tripId: Long) {
        decideTripType(tripId, prepareReturn = true)
    }

    /**
     * Démarre le trajet retour
     */
    fun startReturn(returnTripId: Long, actualStartKm: Int?) {
        viewModelScope.launch {
            when (val result = startReturnUseCase(returnTripId, actualStartKm)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour démarré"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Termine le trajet retour
     */
    fun finishReturn(tripId: Long, endKm: Int) {
        viewModelScope.launch {
            when (val result = finishReturnUseCase(tripId, endKm, allowInconsistentKm = false)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour terminé"))
                }
                is Result.Error -> {
                    // Gestion spéciale pour km incohérents
                    if (result.exception is FinishOutwardUseCase.KmInconsistencyException) {
                        sendUiEvent(UiEvent.ShowConfirmDialog(
                            title = "Confirmation",
                            message = result.message ?: "Confirmer l'opération ?",
                            onConfirm = { finishReturnForce(tripId, endKm) }
                        ))
                    } else {
                        sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Force la fin du retour avec km incohérents (après confirmation)
     */
    private fun finishReturnForce(tripId: Long, endKm: Int) {
        viewModelScope.launch {
            when (val result = finishReturnUseCase(tripId, endKm, allowInconsistentKm = true)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour terminé"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Annule le trajet retour
     */
    fun cancelReturn(tripId: Long) {
        viewModelScope.launch {
            when (val result = cancelReturnUseCase(tripId)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour annulé"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Modifie l'heure de départ d'un trajet
     */
    fun editStartTime(tripId: Long, newStartTime: Long) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editStartTime(tripId, newStartTime)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Heure modifiée"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Modifie l'heure d'arrivée d'un trajet
     */
    fun editEndTime(tripId: Long, newEndTime: Long) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editEndTime(tripId, newEndTime)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Heure modifiée"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Modifie le kilométrage de départ d'un trajet
     */
    fun editStartKm(tripId: Long, newStartKm: Int) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editStartKm(tripId, newStartKm)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Kilométrage modifié"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Modifie le kilométrage d'arrivée d'un trajet
     */
    fun editEndKm(tripId: Long, newEndKm: Int) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editEndKm(tripId, newEndKm)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Kilométrage modifié"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Supprime un groupe de trajets
     */
    fun deleteTripGroup(tripGroup: TripGroup) {
        viewModelScope.launch {
            when (val result = deleteTripGroupUseCase(tripGroup)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Trajet supprimé"))
                }
                is Result.Error -> {
                    sendUiEvent(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
                is Result.Loading -> {}
            }
        }
    }
}
