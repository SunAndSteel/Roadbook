package com.florent.carnetconduite.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.groupTrips
import com.florent.carnetconduite.domain.usecases.CancelReturnUseCase
import com.florent.carnetconduite.domain.usecases.ComputeDrivingStateUseCase
import com.florent.carnetconduite.domain.usecases.DecideTripTypeUseCase
import com.florent.carnetconduite.domain.usecases.EditTripUseCase
import com.florent.carnetconduite.domain.usecases.FinishOutwardUseCase
import com.florent.carnetconduite.domain.usecases.FinishReturnUseCase
import com.florent.carnetconduite.domain.usecases.StartOutwardUseCase
import com.florent.carnetconduite.domain.usecases.StartReturnUseCase
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.ui.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * ViewModel pour l'écran Home - gère l'état actuel de conduite
 */
class HomeViewModel(
    private val startOutwardUseCase: StartOutwardUseCase,
    private val finishOutwardUseCase: FinishOutwardUseCase,
    private val decideTripTypeUseCase: DecideTripTypeUseCase,
    private val startReturnUseCase: StartReturnUseCase,
    private val finishReturnUseCase: FinishReturnUseCase,
    private val cancelReturnUseCase: CancelReturnUseCase,
    private val editTripUseCase: EditTripUseCase,
    private val computeStateUseCase: ComputeDrivingStateUseCase,
    repository: TripRepository,
    private val logger: AppLogger
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val trips: StateFlow<List<Trip>> = repository.allTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tripGroups: StateFlow<List<TripGroup>> = trips
        .map { tripList -> groupTrips(tripList) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeTrip: StateFlow<Trip?> = repository.activeTrip.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val arrivedTrip: StateFlow<Trip?> = trips
        .map { tripList -> findArrivedTrip(tripList) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Petit latch pour afficher l'état "COMPLETED" sans effet de bord sur la data.
    private val completionLatch = MutableStateFlow(false)

    val drivingState: StateFlow<DrivingState> = trips
        .combine(completionLatch) { tripList, latch ->
            if (latch) {
                DrivingState.COMPLETED
            } else {
                computeStateUseCase(tripList)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DrivingState.IDLE
        )

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

    private suspend fun sendUiEvent(event: UiEvent) {
        _uiEvent.emit(event)
    }

    private suspend fun emitError(
        operation: String,
        exception: Exception,
        message: String = "Une erreur est survenue. Réessaie.",
        context: String? = null
    ) {
        val contextSuffix = context?.let { " ($it)" } ?: ""
        logger.logError("HomeViewModel:$operation failed$contextSuffix", exception)
        sendUiEvent(UiEvent.ShowError(message))
    }

    private fun triggerCompletionLatch() {
        viewModelScope.launch {
            completionLatch.value = true
            delay(600)
            completionLatch.value = false
        }
    }

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
                    emitError(
                        operation = "startOutward",
                        exception = result.exception,
                        context = "startKm=$startKm"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun finishOutward(tripId: Long, endKm: Int, endPlace: String) {
        viewModelScope.launch {
            when (val result = finishOutwardUseCase(tripId, endKm, endPlace, allowInconsistentKm = false)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Trajet terminé"))
                }
                is Result.Error -> {
                    if (result.exception is FinishOutwardUseCase.KmInconsistencyException) {
                        logger.logError(
                            "HomeViewModel:finishOutward km inconsistency (tripId=$tripId)",
                            result.exception
                        )
                        sendUiEvent(
                            UiEvent.ShowConfirmDialog(
                                title = "Confirmation",
                                message = "Le kilométrage saisi semble incohérent. Confirmer l'opération ?",
                                onConfirm = { finishOutwardForce(tripId, endKm, endPlace) }
                            )
                        )
                    } else {
                        emitError(
                            operation = "finishOutward",
                            exception = result.exception,
                            context = "tripId=$tripId"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun finishOutwardForce(tripId: Long, endKm: Int, endPlace: String) {
        viewModelScope.launch {
            when (val result = finishOutwardUseCase(tripId, endKm, endPlace, allowInconsistentKm = true)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Trajet terminé"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "finishOutwardForce",
                        exception = result.exception,
                        context = "tripId=$tripId"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun startReturn(returnTripId: Long, actualStartKm: Int?) {
        viewModelScope.launch {
            when (val result = startReturnUseCase(returnTripId, actualStartKm)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour démarré"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "startReturn",
                        exception = result.exception,
                        context = "tripId=$returnTripId"
                    )
                }

                is Result.Loading -> {}
            }
        }
    }

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
                    if (!prepareReturn) {
                        triggerCompletionLatch()
                    }
                }
                is Result.Error -> {
                    emitError(
                        operation = "decideTripType",
                        exception = result.exception,
                        context = "tripId=$tripId, prepareReturn=$prepareReturn"
                    )
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

    fun finishReturn(tripId: Long, endKm: Int) {
        viewModelScope.launch {
            when (val result = finishReturnUseCase(tripId, endKm, allowInconsistentKm = false)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour terminé"))
                    triggerCompletionLatch()
                }
                is Result.Error -> {
                    if (result.exception is FinishOutwardUseCase.KmInconsistencyException) {
                        logger.logError(
                            "HomeViewModel:finishReturn km inconsistency (tripId=$tripId)",
                            result.exception
                        )
                        sendUiEvent(
                            UiEvent.ShowConfirmDialog(
                                title = "Confirmation",
                                message = "Le kilométrage saisi semble incohérent. Confirmer l'opération ?",
                                onConfirm = { finishReturnForce(tripId, endKm) }
                            )
                        )
                    } else {
                        emitError(
                            operation = "finishReturn",
                            exception = result.exception,
                            context = "tripId=$tripId"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun finishReturnForce(tripId: Long, endKm: Int) {
        viewModelScope.launch {
            when (val result = finishReturnUseCase(tripId, endKm, allowInconsistentKm = true)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour terminé"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "finishReturnForce",
                        exception = result.exception,
                        context = "tripId=$tripId"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun cancelReturn(tripId: Long) {
        viewModelScope.launch {
            when (val result = cancelReturnUseCase(tripId)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Retour annulé"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "cancelReturn",
                        exception = result.exception,
                        context = "tripId=$tripId"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun editStartTime(tripId: Long, newStartTime: Long) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editStartTime(tripId, newStartTime)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Heure modifiée"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "editStartTime",
                        exception = result.exception,
                        context = "tripId=$tripId"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun editEndTime(tripId: Long, newEndTime: Long) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editEndTime(tripId, newEndTime)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Heure modifiée"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "editEndTime",
                        exception = result.exception,
                        context = "tripId=$tripId"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun editStartKm(tripId: Long, newStartKm: Int) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editStartKm(tripId, newStartKm)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Kilométrage modifié"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "editStartKm",
                        exception = result.exception,
                        context = "tripId=$tripId"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun editEndKm(tripId: Long, newEndKm: Int) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editEndKm(tripId, newEndKm)) {
                is Result.Success -> {
                    sendUiEvent(UiEvent.ShowToast("Kilométrage modifié"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "editEndKm",
                        exception = result.exception,
                        context = "tripId=$tripId"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
}
