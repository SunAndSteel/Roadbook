package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DecideTripTypeUseCaseTest {

    @Test
    fun simpleTripMarksOutwardWithoutCreatingReturn() = runTest {
        val repository = mock<TripRepository>()
        val logger = mock<AppLogger>()
        val useCase = DecideTripTypeUseCase(repository, logger)
        val tripId = 42L
        val trip = Trip(
            id = tripId,
            startKm = 10,
            endKm = 20,
            startPlace = "A",
            endPlace = "B",
            startTime = 1000L,
            endTime = 2000L,
            isReturn = false,
            pairedTripId = null,
            status = TripStatus.COMPLETED
        )

        whenever(repository.getTripById(tripId)).thenReturn(Result.Success(trip))
        whenever(repository.markOutwardAsSimple(tripId)).thenReturn(Result.Success(Unit))

        val result = useCase(tripId, prepareReturn = false)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(repository).markOutwardAsSimple(tripId)
        verify(repository, never()).finishAndPrepareReturn(any(), any(), any(), any())
        verify(repository, never()).createSkippedReturn(eq(tripId))
    }
}
