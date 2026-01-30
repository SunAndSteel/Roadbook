package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.testutils.TripFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ComputeDrivingStateUseCaseTest {
    private val useCase = ComputeDrivingStateUseCase()

    @Test
    fun `return active has highest priority`() {
        val trips = listOf(
            TripFactory.create(id = 1, status = TripStatus.ACTIVE),
            TripFactory.create(id = 2, isReturn = true, status = TripStatus.ACTIVE)
        )

        assertThat(useCase(trips)).isEqualTo(DrivingState.RETURN_ACTIVE)
    }

    @Test
    fun `outward active comes before ready`() {
        val trips = listOf(
            TripFactory.create(id = 1, status = TripStatus.ACTIVE),
            TripFactory.create(id = 2, isReturn = true, status = TripStatus.READY)
        )

        assertThat(useCase(trips)).isEqualTo(DrivingState.OUTWARD_ACTIVE)
    }

    @Test
    fun `ready return is detected`() {
        val trips = listOf(
            TripFactory.create(id = 2, isReturn = true, status = TripStatus.READY)
        )

        assertThat(useCase(trips)).isEqualTo(DrivingState.RETURN_READY)
    }

    @Test
    fun `arrived detected for completed outward without return`() {
        val trips = listOf(
            TripFactory.create(id = 1, endKm = 120, status = TripStatus.COMPLETED)
        )

        assertThat(useCase(trips)).isEqualTo(DrivingState.ARRIVED)
    }

    @Test
    fun `idle when no trips or all terminal`() {
        val empty = useCase(emptyList())
        val terminal = useCase(
            listOf(TripFactory.create(id = 3, status = TripStatus.CANCELLED))
        )

        assertThat(empty).isEqualTo(DrivingState.IDLE)
        assertThat(terminal).isEqualTo(DrivingState.IDLE)
    }

    @Test
    fun `completed when there are non-terminal trips not matching other states`() {
        val trips = listOf(
            TripFactory.create(id = 1, status = TripStatus.READY, isReturn = false)
        )

        assertThat(useCase(trips)).isEqualTo(DrivingState.COMPLETED)
    }
}
