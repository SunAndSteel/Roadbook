package com.florent.carnetconduite.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.domain.usecases.*
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.ui.UiEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran Home - gère l'état actuel de conduite
 */
class HomeViewModel(
    private val computeDrivingStateUseCase: ComputeDrivingStateUseCase,
    private val startOutwardUseCase: StartOutwardUseCase,
    private val finishOutwardUseCase: FinishOutwardUseCase,
    private val startReturnUseCase: StartReturnUseCase,
    private val decideTripTypeUseCase: DecideTripTypeUseCase,
    private val repository: TripRepository
) : ViewModel() {

    private val _drivingState = MutableStateFlow<DrivingState>(DrivingState.IDLE)
    val drivingState: StateFlow<DrivingState> = _drivingState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        computeDrivingState()
    }

    private fun computeDrivingState() {
        viewModelScope.launch {
            val trips = repository.getAllTrips()
            // Calculer l'état
            val state = computeDrivingStateUseCase(trips)
            _drivingState.value = state
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
                    computeDrivingState()
                }
                is Result.Error -> {
                    _uiEvent.emit(UiEvent.ShowError(result.message.toString()))
                }
                else -> {

                }
            }
        }
    }

    fun finishOutward(tripId: Long, endKm: Int, endPlace: String) {
        viewModelScope.launch {
            when (val result = finishOutwardUseCase(tripId, endKm, endPlace)) {
                is Result.Success -> {
                    computeDrivingState()
                }
                is Result.Error -> {
                    _uiEvent.emit(UiEvent.ShowError(result.message.toString()))
                }
                else -> {

                }
            }
        }
    }

    fun startReturn(returnTripId: Long, actualStartKm: Int?) {
        viewModelScope.launch {
            when (val result = startReturnUseCase(returnTripId, actualStartKm)) {
                is Result.Success -> {
                    computeDrivingState()
                }
                is Result.Error -> {
                    _uiEvent.emit(UiEvent.ShowError(result.message.toString()))
                }

                else -> {

                }
            }
        }
    }

    fun decideTripType(tripId: Long, prepareReturn: Boolean) {
        viewModelScope.launch {
            when (val result = decideTripTypeUseCase(tripId, prepareReturn)) {
                is Result.Success -> {
                    computeDrivingState()
                }
                is Result.Error -> {
                    _uiEvent.emit(UiEvent.ShowError(result.message.toString()))
                }
                else -> {

                }
            }
        }
    }
}