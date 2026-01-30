package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.NoOpLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

class CancelReturnUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = CancelReturnUseCase(repository, logger = logger)

    @Test
    fun `cancel return delegates to repository`() = runTest {
        whenever(repository.cancelReturn(12)).thenReturn(Result.success(Unit))

        val result = useCase(12)

        assertThat(result.isSuccess()).isTrue()
        verify(repository).cancelReturn(12)
    }
}
