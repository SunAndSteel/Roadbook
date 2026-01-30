package com.florent.carnetconduite.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TimeUtilsTest {
    @Test
    fun `formatTime returns empty for zero`() {
        assertThat(formatTimeOrEmpty(0L)).isEqualTo("")
    }
}
