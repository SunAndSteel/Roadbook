package com.florent.carnetconduite.data

import com.florent.carnetconduite.domain.models.TripStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TripTest {
    @Test
    fun `computed properties reflect state`() {
        val trip = Trip(
            id = 1,
            startKm = 100,
            endKm = 150,
            startPlace = "Paris",
            startTime = 1000L,
            endTime = 2000L,
            status = TripStatus.COMPLETED,
            date = "2024-01-01"
        )

        assertThat(trip.nbKmsParcours).isEqualTo(50)
        assertThat(trip.kmsComptabilises).isEqualTo(50)
        assertThat(trip.isActive).isFalse()
        assertThat(trip.isCompleted).isTrue()
        assertThat(trip.isTerminal).isTrue()
    }
}
