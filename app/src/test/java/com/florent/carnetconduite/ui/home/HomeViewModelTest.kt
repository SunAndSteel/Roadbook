package com.florent.carnetconduite.ui.home

import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.domain.usecases.CancelReturnUseCase
import com.florent.carnetconduite.domain.usecases.ComputeDrivingStateUseCase
import com.florent.carnetconduite.domain.usecases.DecideTripTypeUseCase
import com.florent.carnetconduite.domain.usecases.EditTripUseCase
import com.florent.carnetconduite.domain.usecases.FinishOutwardUseCase
import com.florent.carnetconduite.domain.usecases.FinishReturnUseCase
import com.florent.carnetconduite.domain.usecases.StartOutwardUseCase
import com.florent.carnetconduite.domain.usecases.StartReturnUseCase
import com.florent.carnetconduite.domain.utils.NoOpLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.testutils.MainDispatcherRule
import com.florent.carnetconduite.testutils.TripFactory
import com.florent.carnetconduite.ui.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val startOutwardUseCase = mock<StartOutwardUseCase>()
    private val finishOutwardUseCase = mock<FinishOutwardUseCase>()
    private val decideTripTypeUseCase = mock<DecideTripTypeUseCase>()
    private val startReturnUseCase = mock<StartReturnUseCase>()
    private val finishReturnUseCase = mock<FinishReturnUseCase>()
    private val cancelReturnUseCase = mock<CancelReturnUseCase>()
    private val editTripUseCase = mock<EditTripUseCase>()
    private val computeStateUseCase = mock<ComputeDrivingStateUseCase>()
    private val repository = mock<TripRepository>()

    @Test
    fun `startOutward emits toast on success`() = runTest(mainDispatcherRule.dispatcher) {
        whenever(startOutwardUseCase(any(), any(), any(), any())).thenReturn(Result.success(1L))
        whenever(repository.allTrips).thenReturn(MutableStateFlow(emptyList()))
        whenever(repository.activeTrip).thenReturn(MutableStateFlow(null))
        whenever(computeStateUseCase(any())).thenReturn(DrivingState.IDLE)

        val viewModel = HomeViewModel(
            startOutwardUseCase,
            finishOutwardUseCase,
            decideTripTypeUseCase,
            startReturnUseCase,
            finishReturnUseCase,
            cancelReturnUseCase,
            editTripUseCase,
            computeStateUseCase,
            repository,
            NoOpLogger()
        )

        val eventDeferred = async { viewModel.uiEvent.first() }

        viewModel.startOutward(100, "Paris", "", "1")
        advanceUntilIdle()

        val event = eventDeferred.await()
        assertThat(event).isEqualTo(UiEvent.ShowToast("Trajet démarré"))
    }

    @Test
    fun `finishOutward emits confirm dialog on km inconsistency`() = runTest(mainDispatcherRule.dispatcher) {
        val exception = FinishOutwardUseCase.KmInconsistencyException("bad", 100, 50)
        whenever(finishOutwardUseCase(1, 50, "Lyon", false)).thenReturn(Result.error(exception))
        whenever(finishOutwardUseCase(1, 50, "Lyon", true)).thenReturn(Result.success(Unit))
        whenever(repository.allTrips).thenReturn(MutableStateFlow(emptyList()))
        whenever(repository.activeTrip).thenReturn(MutableStateFlow(null))
        whenever(computeStateUseCase(any())).thenReturn(DrivingState.IDLE)

        val viewModel = HomeViewModel(
            startOutwardUseCase,
            finishOutwardUseCase,
            decideTripTypeUseCase,
            startReturnUseCase,
            finishReturnUseCase,
            cancelReturnUseCase,
            editTripUseCase,
            computeStateUseCase,
            repository,
            NoOpLogger()
        )

        val eventDeferred = async { viewModel.uiEvent.first() }

        viewModel.finishOutward(1, 50, "Lyon")
        advanceUntilIdle()

        val event = eventDeferred.await()
        assertThat(event).isInstanceOf(UiEvent.ShowConfirmDialog::class.java)
        val dialog = event as UiEvent.ShowConfirmDialog
        dialog.onConfirm()

        advanceUntilIdle()
        verify(finishOutwardUseCase).invoke(1, 50, "Lyon", true)
    }

    @Test
    fun `decideTripType triggers completion latch when no return`() = runTest(mainDispatcherRule.dispatcher) {
        whenever(decideTripTypeUseCase(1, false)).thenReturn(Result.success(Unit))
        whenever(repository.allTrips).thenReturn(MutableStateFlow(listOf(TripFactory.create(id = 1))))
        whenever(repository.activeTrip).thenReturn(MutableStateFlow(null))
        whenever(computeStateUseCase(any())).thenReturn(DrivingState.IDLE)

        val viewModel = HomeViewModel(
            startOutwardUseCase,
            finishOutwardUseCase,
            decideTripTypeUseCase,
            startReturnUseCase,
            finishReturnUseCase,
            cancelReturnUseCase,
            editTripUseCase,
            computeStateUseCase,
            repository,
            NoOpLogger()
        )

        val states = mutableListOf<DrivingState>()
        val job = launch {
            viewModel.drivingState.take(3).collect { state ->
                states.add(state)
            }
        }

        viewModel.decideTripType(1, prepareReturn = false)
        advanceTimeBy(600)
        advanceUntilIdle()
        job.cancel()

        assertThat(states).contains(DrivingState.COMPLETED)
    }
}
