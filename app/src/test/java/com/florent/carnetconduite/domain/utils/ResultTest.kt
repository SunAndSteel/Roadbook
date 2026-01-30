package com.florent.carnetconduite.domain.utils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class ResultTest {
    @Test
    fun `map and flatMap transform success`() {
        val result = Result.success(2)
            .map { it * 2 }
            .flatMap { Result.success(it + 1) }

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(result.getOrNull()).isEqualTo(5)
    }

    @Test
    fun `getOrThrow throws on error`() {
        val error = Result.error(IllegalStateException("boom"))

        assertThatThrownBy { error.getOrThrow() }
            .isInstanceOf(IllegalStateException::class.java)
    }
}
