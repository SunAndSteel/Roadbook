package com.florent.carnetconduite.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DateTimeFormatterTest {
    @Test
    fun `formatDate uses dd slash format`() {
        assertThat(formatDate("2024-03-12")).isEqualTo("12/03/2024")
    }

    @Test
    fun `formatDate returns original on invalid input`() {
        assertThat(formatDate("invalid")).isEqualTo("invalid")
    }

    @Test
    fun `formatTimeRange shows ongoing label`() {
        val formatted = formatTimeRange(0L, null, ongoingLabel = "En cours")
        assertThat(formatted).contains("En cours")
    }
}
