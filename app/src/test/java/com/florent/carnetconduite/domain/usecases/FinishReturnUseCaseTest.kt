package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.NoOpLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.testutils.TripFactory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

class FinishReturnUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = FinishReturnUseCase(repository, logger = logger)

    @Test
    fun `finish return uses end place fallback`() = runTest {
        val trip = TripFactory.create(id = 5, startKm = 100, endPlace = "Paris", isReturn = true)
        whenever(repository.getTripById(5)).thenReturn(Result.success(trip))
        whenever(repository.finishTrip(any(), any(), any(), any())).thenReturn(Result.success(Unit))

        val result = useCase(tripId = 5, endKm = 150, allowInconsistentKm = false)

        assertThat(result.isSuccess()).isTrue()
        verify(repository).finishTrip(eq(5), eq(150), eq("Paris"), any())
    }

    @Test
    fun `finish return detects km inconsistency`() = runTest {
        val trip = TripFactory.create(id = 5, startKm = 200, isReturn = true)
        whenever(repository.getTripById(5)).thenReturn(Result.success(trip))

        val result = useCase(tripId = 5, endKm = 100, allowInconsistentKm = false)

        assertThat(result.isError()).isTrue()
        assertThat((result as Result.Error).exception)
            .isInstanceOf(FinishOutwardUseCase.KmInconsistencyException::class.java)
    }
}
