package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.NoOpLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.testutils.TripFactory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

class FinishOutwardUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = FinishOutwardUseCase(repository, logger = logger)

    @Test
    fun `finish outward updates trip with trimmed end place`() = runTest {
        val trip = TripFactory.create(id = 1, startKm = 100)
        whenever(repository.getTripById(1)).thenReturn(Result.success(trip))
        whenever(repository.finishTrip(any(), any(), any(), any())).thenReturn(Result.success(Unit))

        val result = useCase(tripId = 1, endKm = 150, endPlace = " Lyon ", allowInconsistentKm = false)

        assertThat(result.isSuccess()).isTrue()
        val placeCaptor = argumentCaptor<String>()
        verify(repository).finishTrip(eq(1L), eq(150), placeCaptor.capture(), any())
        assertThat(placeCaptor.firstValue).isEqualTo("Lyon")
    }

    @Test
    fun `finish outward rejects invalid place`() = runTest {
        val trip = TripFactory.create(id = 1, startKm = 100)
        whenever(repository.getTripById(1)).thenReturn(Result.success(trip))

        val result = useCase(tripId = 1, endKm = 150, endPlace = "", allowInconsistentKm = false)

        assertThat(result.isError()).isTrue()
    }

    @Test
    fun `finish outward detects km inconsistency`() = runTest {
        val trip = TripFactory.create(id = 1, startKm = 200)
        whenever(repository.getTripById(1)).thenReturn(Result.success(trip))

        val result = useCase(tripId = 1, endKm = 100, endPlace = "Lyon", allowInconsistentKm = false)

        assertThat(result.isError()).isTrue()
        assertThat((result as Result.Error).exception)
            .isInstanceOf(FinishOutwardUseCase.KmInconsistencyException::class.java)
    }
}
