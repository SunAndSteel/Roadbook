package com.florent.carnetconduite.ui.history

import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.domain.usecases.DeleteTripGroupUseCase
import com.florent.carnetconduite.domain.usecases.EditTripUseCase
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.testutils.MainDispatcherRule
import com.florent.carnetconduite.testutils.TripFactory
import com.florent.carnetconduite.ui.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mock<TripRepository>()
    private val deleteTripGroupUseCase = mock<DeleteTripGroupUseCase>()
    private val editTripUseCase = mock<EditTripUseCase>()

    @Test
    fun `tripStats aggregates totals`() = runTest(mainDispatcherRule.dispatcher) {
        val outward = TripFactory.create(
            id = 1,
            startTime = 0L,
            endTime = 3_600_000L,
            endKm = 120,
            status = TripStatus.COMPLETED
        )
        val returnTrip = TripFactory.create(
            id = 2,
            startTime = 0L,
            endTime = 1_800_000L,
            startKm = 120,
            endKm = 150,
            isReturn = true,
            pairedTripId = 1,
            status = TripStatus.COMPLETED
        )
        whenever(repository.allTrips).thenReturn(MutableStateFlow(listOf(outward, returnTrip)))

        val viewModel = HistoryViewModel(repository, deleteTripGroupUseCase, editTripUseCase)

        advanceUntilIdle()
        val stats = viewModel.tripStats.drop(1).first()
        assertThat(stats.totalTrips).isEqualTo(1)
        assertThat(stats.totalKm).isEqualTo(outward.kmsComptabilises + returnTrip.kmsComptabilises)
        assertThat(stats.totalDurationMs).isEqualTo(5_400_000L)
    }

    @Test
    fun `deleteTripGroup emits toast on success`() = runTest(mainDispatcherRule.dispatcher) {
        val outward = TripFactory.create(id = 1, endKm = 120, status = TripStatus.COMPLETED)
        val group = TripGroup(outward = outward, returnTrip = null, seanceNumber = 1)
        whenever(repository.allTrips).thenReturn(MutableStateFlow(listOf(outward)))
        whenever(deleteTripGroupUseCase(group)).thenReturn(Result.success(Unit))

        val viewModel = HistoryViewModel(repository, deleteTripGroupUseCase, editTripUseCase)

        val eventDeferred = async { viewModel.uiEvent.first() }

        viewModel.deleteTripGroup(group)
        advanceUntilIdle()

        val event = eventDeferred.await()
        assertThat(event).isEqualTo(UiEvent.ShowToast("Trajet supprimé"))
    }
}
