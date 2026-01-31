package com.florent.carnetconduite.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.groupTrips
import com.florent.carnetconduite.domain.usecases.DeleteTripGroupUseCase
import com.florent.carnetconduite.domain.usecases.EditTripUseCase
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.ui.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran History - gère l'historique des trajets
 */
class HistoryViewModel(
    private val repository: TripRepository,
    private val deleteTripGroupUseCase: DeleteTripGroupUseCase,
    private val editTripUseCase: EditTripUseCase
) : ViewModel() {

    /**
     * Flow des groupes de trajets (aller + retour)
     */
    val tripGroups = repository.allTrips
        .map(::groupTrips)

    /**
     * Statistiques calculées
     */
    val tripStats: StateFlow<TripStats> = tripGroups
        .map { groups ->
            val totalDurationMs = groups.sumOf { group ->
                val outward = (group.outward.endTime ?: group.outward.startTime) - group.outward.startTime
                val ret = group.returnTrip?.let { t -> (t.endTime ?: t.startTime) - t.startTime } ?: 0L
                (outward.coerceAtLeast(0L) + ret.coerceAtLeast(0L))
            }

            TripStats(
                totalTrips = groups.size,
                totalKm = groups.sumOf { it.totalKms },
                totalDurationMs = totalDurationMs
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TripStats()
        )

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun deleteTripGroup(group: TripGroup) {
        viewModelScope.launch {
            when (val result = deleteTripGroupUseCase(group)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Trajet supprimé"))
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                is Result.Loading -> {}
            }
        }
    }

    fun editStartTime(tripId: Long, newStartTime: Long) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editStartTime(tripId, newStartTime)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Heure modifiée"))
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                is Result.Loading -> {}
            }
        }
    }

    fun editEndTime(tripId: Long, newEndTime: Long) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editEndTime(tripId, newEndTime)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Heure modifiée"))
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                is Result.Loading -> {}
            }
        }
    }

    fun editStartKm(tripId: Long, newStartKm: Int) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editStartKm(tripId, newStartKm)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Kilométrage modifié"))
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                is Result.Loading -> {}
            }
        }
    }

    fun editEndKm(tripId: Long, newEndKm: Int) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editEndKm(tripId, newEndKm)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Kilométrage modifié"))
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                is Result.Loading -> {}
            }
        }
    }

    fun editDate(tripId: Long, newDate: String) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editDate(tripId, newDate)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Date modifiée"))
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                is Result.Loading -> {}
            }
        }
    }

    fun editConditions(tripId: Long, newConditions: String) {
        viewModelScope.launch {
            when (val result = editTripUseCase.editConditions(tripId, newConditions)) {
                is Result.Success -> _uiEvent.emit(UiEvent.ShowToast("Conditions modifiées"))
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                is Result.Loading -> {}
            }
        }
    }
}

data class TripStats(
    val totalTrips: Int = 0,
    val totalKm: Int = 0,
    val totalDurationMs: Long = 0L
) {
    val averageKmPerTrip: Double
        get() = if (totalTrips > 0) totalKm.toDouble() / totalTrips else 0.0
}
