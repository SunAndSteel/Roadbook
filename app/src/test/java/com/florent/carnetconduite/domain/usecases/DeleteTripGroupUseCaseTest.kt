package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.models.TripGroup
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

class DeleteTripGroupUseCaseTest {
    private val repository = mock<TripRepository>()
    private val logger = NoOpLogger()
    private val useCase = DeleteTripGroupUseCase(repository, logger = logger)

    @Test
    fun `delete trip group deletes outward and return`() = runTest {
        val outward = TripFactory.create(id = 1)
        val returnTrip = TripFactory.create(id = 2, isReturn = true)
        val group = TripGroup(outward = outward, returnTrip = returnTrip, seanceNumber = 1)
        whenever(repository.delete(outward)).thenReturn(Result.success(Unit))
        whenever(repository.delete(returnTrip)).thenReturn(Result.success(Unit))

        val result = useCase(group)

        assertThat(result.isSuccess()).isTrue()
        verify(repository).delete(outward)
        verify(repository).delete(returnTrip)
    }

    @Test
    fun `delete trip group returns error when outward delete fails`() = runTest {
        val outward = TripFactory.create(id = 1)
        val group = TripGroup(outward = outward, returnTrip = null, seanceNumber = 1)
        whenever(repository.delete(outward)).thenReturn(Result.error(IllegalStateException("fail")))

        val result = useCase(group)

        assertThat(result.isError()).isTrue()
    }
}
