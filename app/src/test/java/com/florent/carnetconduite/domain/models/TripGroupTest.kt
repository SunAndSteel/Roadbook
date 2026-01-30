package com.florent.carnetconduite.domain.models

import com.florent.carnetconduite.testutils.TripFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TripGroupTest {
    @Test
    fun `groupTrips pairs outward and return trips and orders by recency`() {
        val outward1 = TripFactory.create(id = 1, startTime = 100, endKm = 120, status = TripStatus.COMPLETED)
        val return1 = TripFactory.create(
            id = 2,
            startTime = 200,
            endKm = 140,
            isReturn = true,
            pairedTripId = 1,
            status = TripStatus.COMPLETED
        )
        val outward2 = TripFactory.create(id = 3, startTime = 300, endKm = 180, status = TripStatus.COMPLETED)

        val groups = groupTrips(listOf(outward1, return1, outward2))

        assertThat(groups).hasSize(2)
        assertThat(groups.first().outward.id).isEqualTo(3)
        assertThat(groups.first().returnTrip).isNull()
        assertThat(groups.last().outward.id).isEqualTo(1)
        assertThat(groups.last().returnTrip?.id).isEqualTo(2)
    }

    @Test
    fun `tripGroup flags computed values`() {
        val outward = TripFactory.create(id = 10, endKm = 150, status = TripStatus.COMPLETED)
        val returnTrip = TripFactory.create(
            id = 11,
            startKm = 150,
            endKm = 170,
            isReturn = true,
            pairedTripId = 10,
            status = TripStatus.SKIPPED
        )

        val group = TripGroup(outward, returnTrip, seanceNumber = 1)

        assertThat(group.totalKms).isEqualTo(outward.kmsComptabilises + returnTrip.kmsComptabilises)
        assertThat(group.hasReturn).isFalse()
        assertThat(group.isComplete).isTrue()
    }
}
