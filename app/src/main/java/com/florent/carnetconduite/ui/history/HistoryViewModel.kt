package com.florent.carnetconduite.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.groupTrips
import com.florent.carnetconduite.domain.usecases.DeleteTripGroupUseCase
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
    private val deleteTripGroupUseCase: DeleteTripGroupUseCase
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
                    _uiEvent.emit(UiEvent.ShowError(result.message ?: "Une erreur est survenue"))
                }
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
