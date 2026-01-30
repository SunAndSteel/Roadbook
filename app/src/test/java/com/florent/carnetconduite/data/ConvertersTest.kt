package com.florent.carnetconduite.data

import com.florent.carnetconduite.domain.models.TripStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `fromTripStatus stores enum name`() {
        assertThat(converters.fromTripStatus(TripStatus.COMPLETED)).isEqualTo("COMPLETED")
    }

    @Test
    fun `toTripStatus falls back to ACTIVE on unknown value`() {
        assertThat(converters.toTripStatus("UNKNOWN")).isEqualTo(TripStatus.ACTIVE)
    }
}
