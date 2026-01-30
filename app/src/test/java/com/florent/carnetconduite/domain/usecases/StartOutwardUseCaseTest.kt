package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.NoOpLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

class StartOutwardUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = StartOutwardUseCase(repository, logger = logger)

    @Test
    fun `start outward inserts trip and saves session`() = runTest {
        whenever(repository.insert(any())).thenReturn(42L)
        whenever(repository.saveOngoingSessionId(42L)).thenReturn(Result.success(Unit))

        val result = useCase(startKm = 120, startPlace = " Lyon ", conditions = "Sun", guide = "1")

        assertThat(result.getOrNull()).isEqualTo(42L)
        val tripCaptor = argumentCaptor<com.florent.carnetconduite.data.Trip>()
        verify(repository).insert(tripCaptor.capture())
        verify(repository).saveOngoingSessionId(42L)
        assertThat(tripCaptor.firstValue.startPlace).isEqualTo("Lyon")
        assertThat(tripCaptor.firstValue.isReturn).isFalse()
    }

    @Test
    fun `start outward fails on invalid input`() = runTest {
        val result = useCase(startKm = -1, startPlace = "", conditions = "", guide = "1")

        assertThat(result.isError()).isTrue()
    }
}
