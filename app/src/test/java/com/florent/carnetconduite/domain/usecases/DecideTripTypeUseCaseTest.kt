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

class DecideTripTypeUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = DecideTripTypeUseCase(repository, logger = logger)

    @Test
    fun `prepare return finishes and prepares return trip`() = runTest {
        val trip = TripFactory.create(
            id = 7,
            endKm = 120,
            endPlace = "Lyon",
            endTime = 1000L
        )
        whenever(repository.getTripById(7)).thenReturn(Result.success(trip))
        whenever(repository.finishAndPrepareReturn(7, 120, "Lyon", 1000L)).thenReturn(Result.success(99L))

        val result = useCase(tripId = 7, prepareReturn = true)

        assertThat(result.isSuccess()).isTrue()
        verify(repository).finishAndPrepareReturn(7, 120, "Lyon", 1000L)
    }

    @Test
    fun `simple trip marks outward as simple`() = runTest {
        val trip = TripFactory.create(id = 7, endKm = 120, endPlace = "Lyon", endTime = 1000L)
        whenever(repository.getTripById(7)).thenReturn(Result.success(trip))
        whenever(repository.markOutwardAsSimple(7)).thenReturn(Result.success(Unit))

        val result = useCase(tripId = 7, prepareReturn = false)

        assertThat(result.isSuccess()).isTrue()
        verify(repository).markOutwardAsSimple(7)
    }
}
