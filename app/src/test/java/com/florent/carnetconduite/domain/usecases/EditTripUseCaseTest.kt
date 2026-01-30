package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.NoOpLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.testutils.TripFactory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

class EditTripUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = EditTripUseCase(repository, logger = logger)

    @Test
    fun `edit date rejects blank and invalid formats`() = runTest {
        val blank = useCase.editDate(tripId = 1, newDate = "  ")
        val invalid = useCase.editDate(tripId = 1, newDate = "2024/01/01")

        assertThat(blank.isError()).isTrue()
        assertThat(invalid.isError()).isTrue()
    }

    @Test
    fun `edit conditions validates length`() = runTest {
        val invalid = useCase.editConditions(tripId = 1, newConditions = "a".repeat(201))

        assertThat(invalid.isError()).isTrue()
    }

    @Test
    fun `edit start time rejects future timestamps`() = runTest {
        val result = useCase.editStartTime(tripId = 1, newStartTime = System.currentTimeMillis() + 60_000)

        assertThat(result.isError()).isTrue()
    }

    @Test
    fun `edit end time validates time range`() = runTest {
        val trip = TripFactory.create(id = 1, startTime = 1000L)
        whenever(repository.getTripById(1)).thenReturn(Result.success(trip))

        val result = useCase.editEndTime(tripId = 1, newEndTime = 500L)

        assertThat(result.isError()).isTrue()
    }

    @Test
    fun `edit start km rejects values beyond end km`() = runTest {
        val trip = TripFactory.create(id = 1, startKm = 100, endKm = 120)
        whenever(repository.getTripById(1)).thenReturn(Result.success(trip))

        val result = useCase.editStartKm(tripId = 1, newStartKm = 130)

        assertThat(result.isError()).isTrue()
    }

    @Test
    fun `edit end km updates repository on valid input`() = runTest {
        val trip = TripFactory.create(id = 1, startKm = 100)
        whenever(repository.getTripById(1)).thenReturn(Result.success(trip))
        whenever(repository.updateEndKm(1, 120)).thenReturn(Result.success(Unit))

        val result = useCase.editEndKm(tripId = 1, newEndKm = 120)

        assertThat(result.isSuccess()).isTrue()
        verify(repository).updateEndKm(1, 120)
    }
}
