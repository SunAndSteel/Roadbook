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

class StartReturnUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = StartReturnUseCase(repository, logger = logger)

    @Test
    fun `start return uses actual start km when provided`() = runTest {
        val trip = TripFactory.create(id = 10, startKm = 100, isReturn = true)
        whenever(repository.getTripById(10)).thenReturn(Result.success(trip))
        whenever(repository.startReturn(any(), any(), any())).thenReturn(Result.success(Unit))

        val result = useCase(returnTripId = 10, actualStartKm = 120)

        assertThat(result.isSuccess()).isTrue()
        verify(repository).startReturn(eq(10), eq(120), any())
    }

    @Test
    fun `start return fails on invalid km`() = runTest {
        val trip = TripFactory.create(id = 10, startKm = 100, isReturn = true)
        whenever(repository.getTripById(10)).thenReturn(Result.success(trip))

        val result = useCase(returnTripId = 10, actualStartKm = -1)

        assertThat(result.isError()).isTrue()
    }
}
