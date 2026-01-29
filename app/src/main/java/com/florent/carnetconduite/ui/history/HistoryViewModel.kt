package com.florent.carnetconduite.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.groupTrips
import com.florent.carnetconduite.domain.usecases.DeleteTripGroupUseCase
import com.florent.carnetconduite.domain.usecases.EditTripUseCase
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.ui.UiEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran History - gère l'historique des trajets
 */
class HistoryViewModel(
    private val repository: TripRepository,
    private val deleteTripGroupUseCase: DeleteTripGroupUseCase,
    private val editTripUseCase: EditTripUseCase,
    private val logger: AppLogger
) : ViewModel() {

    /**
     * Flow des groupes de trajets (aller + retour)
     */
    val tripGroups: Flow<List<TripGroup>> = repository.allTrips
        .map(::groupTrips)


    /**
     * Statistiques calculées
     */
    val tripStats: StateFlow<TripStats> = tripGroups
        .map { groups ->
            TripStats(
                totalTrips = groups.size,
                totalKm = groups.sumOf { it.totalKms },
                totalHours = groups.sumOf { group ->
                    val outwardDuration = group.outward.endTime?.let { it - group.outward.startTime } ?: 0L
                    val returnDuration = group.returnTrip?.endTime?.let { it - group.returnTrip.startTime } ?: 0L
                    outwardDuration + returnDuration
                } / 3600000.0 // ms → hours
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TripStats()
        )

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private suspend fun emitError(
        operation: String,
        exception: Exception,
        message: String = "Une erreur est survenue. Réessaie.",
        context: String? = null
    ) {
        val contextSuffix = context?.let { " ($it)" } ?: ""
        logger.logError("HistoryViewModel:$operation failed$contextSuffix", exception)
        _uiEvent.emit(UiEvent.ShowError(message))
    }

    /**
     * Supprime un groupe de trajets
     */
    fun deleteTripGroup(group: TripGroup) {
        viewModelScope.launch {
            when (val result = deleteTripGroupUseCase(group)) {
                is Result.Success -> {
                    _uiEvent.emit(UiEvent.ShowToast("Trajet supprimé"))
                }
                is Result.Error -> {
                    emitError(
                        operation = "deleteTripGroup",
                        exception = result.exception,
                        context = "outwardTripId=${group.outward.id}"
                    )
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
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Heure modifiée"))
                is Result.Error -> emitError(
                    operation = "editStartTime",
                    exception = result.exception,
                    context = "tripId=$tripId"
                )
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
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Heure modifiée"))
                is Result.Error -> emitError(
                    operation = "editEndTime",
                    exception = result.exception,
                    context = "tripId=$tripId"
                )
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
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Kilométrage modifié"))
                is Result.Error -> emitError(
                    operation = "editStartKm",
                    exception = result.exception,
                    context = "tripId=$tripId"
                )
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
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Kilométrage modifié"))
                is Result.Error -> emitError(
                    operation = "editEndKm",
                    exception = result.exception,
                    context = "tripId=$tripId"
                )
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Modifie la date d'un trajet
     */
    fun editDate(tripId: Long, newDate: String) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editDate(tripId, newDate)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Date modifiée"))
                is Result.Error -> emitError(
                    operation = "editDate",
                    exception = result.exception,
                    context = "tripId=$tripId"
                )
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Modifie les conditions météo d'un trajet
     */
    fun editConditions(tripId: Long, newConditions: String) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editConditions(tripId, newConditions)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Conditions modifiées"))
                is Result.Error -> emitError(
                    operation = "editConditions",
                    exception = result.exception,
                    context = "tripId=$tripId"
                )
                is Result.Loading -> {}
            }
        }
    }
}

/**
 * Statistiques globales
 */
data class TripStats(
    val totalTrips: Int = 0,
    val totalKm: Int = 0,
    val totalHours: Double = 0.0
) {
    val averageKmPerTrip: Double
        get() = if (totalTrips > 0) totalKm.toDouble() / totalTrips else 0.0
}
