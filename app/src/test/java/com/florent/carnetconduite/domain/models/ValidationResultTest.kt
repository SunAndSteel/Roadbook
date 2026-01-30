package com.florent.carnetconduite.domain.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ValidationResultTest {
    @Test
    fun `combine returns first invalid result`() {
        val results = listOf(
            ValidationResult.valid(),
            ValidationResult.invalid("Erreur"),
            ValidationResult.invalid("Autre")
        )

        val combined = results.combine()

        assertThat(combined).isInstanceOf(ValidationResult.Invalid::class.java)
        assertThat(combined.getErrorMessage()).isEqualTo("Erreur")
    }

    @Test
    fun `onValid and onInvalid execute corresponding actions`() {
        var validCalled = false
        var invalidMessage: String? = null

        ValidationResult.valid()
            .onValid { validCalled = true }
            .onInvalid { invalidMessage = it }

        ValidationResult.invalid("Oops")
            .onValid { validCalled = false }
            .onInvalid { invalidMessage = it }

        assertThat(validCalled).isTrue()
        assertThat(invalidMessage).isEqualTo("Oops")
    }
}
