package com.florent.carnetconduite.domain.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class TripStatusTest {
    @Test
    fun `fromString parses valid values`() {
        assertThat(TripStatus.fromString("active")).isEqualTo(TripStatus.ACTIVE)
        assertThat(TripStatus.fromString("COMPLETED")).isEqualTo(TripStatus.COMPLETED)
    }

    @Test
    fun `fromString throws on invalid value`() {
        assertThatThrownBy { TripStatus.fromString("unknown") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `fromStringOrNull returns null on invalid value`() {
        assertThat(TripStatus.fromStringOrNull("invalid")).isNull()
    }

    @Test
    fun `terminal and active helpers reflect status`() {
        assertThat(TripStatus.ACTIVE.isActive()).isTrue()
        assertThat(TripStatus.COMPLETED.isTerminal()).isTrue()
        assertThat(TripStatus.CANCELLED.isTerminal()).isTrue()
        assertThat(TripStatus.READY.isTerminal()).isFalse()
    }
}
