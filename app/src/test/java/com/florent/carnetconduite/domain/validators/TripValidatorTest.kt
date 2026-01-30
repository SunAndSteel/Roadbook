package com.florent.carnetconduite.domain.validators

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TripValidatorTest {
    @Test
    fun `validateStartKm rejects negative and too large values`() {
        val negative = TripValidator.validateStartKm(-1)
        val tooLarge = TripValidator.validateStartKm(1_000_000)

        assertThat(negative.isInvalid()).isTrue()
        assertThat(tooLarge.isInvalid()).isTrue()
    }

    @Test
    fun `validateEndKm enforces range and difference from start`() {
        val lower = TripValidator.validateEndKm(startKm = 10, endKm = 5)
        val equal = TripValidator.validateEndKm(startKm = 10, endKm = 10)
        val valid = TripValidator.validateEndKm(startKm = 10, endKm = 11)

        assertThat(lower.isInvalid()).isTrue()
        assertThat(equal.isInvalid()).isTrue()
        assertThat(valid.isValid()).isTrue()
    }

    @Test
    fun `validatePlace enforces length constraints`() {
        val blank = TripValidator.validatePlace("   ")
        val short = TripValidator.validatePlace("A")
        val long = TripValidator.validatePlace("a".repeat(101))
        val valid = TripValidator.validatePlace("Paris")

        assertThat(blank.isInvalid()).isTrue()
        assertThat(short.isInvalid()).isTrue()
        assertThat(long.isInvalid()).isTrue()
        assertThat(valid.isValid()).isTrue()
    }

    @Test
    fun `validateGuide enforces numeric range`() {
        val blank = TripValidator.validateGuide("")
        val invalidNumber = TripValidator.validateGuide("abc")
        val outOfRange = TripValidator.validateGuide("10")
        val valid = TripValidator.validateGuide("2")

        assertThat(blank.isInvalid()).isTrue()
        assertThat(invalidNumber.isInvalid()).isTrue()
        assertThat(outOfRange.isInvalid()).isTrue()
        assertThat(valid.isValid()).isTrue()
    }

    @Test
    fun `validateConditions allows empty and rejects long text`() {
        val empty = TripValidator.validateConditions("")
        val long = TripValidator.validateConditions("a".repeat(201))

        assertThat(empty.isValid()).isTrue()
        assertThat(long.isInvalid()).isTrue()
    }

    @Test
    fun `validateTimestamp rejects future and negative timestamps`() {
        val future = TripValidator.validateTimestamp(System.currentTimeMillis() + 10_000)
        val negative = TripValidator.validateTimestamp(-1)
        val valid = TripValidator.validateTimestamp(System.currentTimeMillis())

        assertThat(future.isInvalid()).isTrue()
        assertThat(negative.isInvalid()).isTrue()
        assertThat(valid.isValid()).isTrue()
    }

    @Test
    fun `validateOutwardStart aggregates errors`() {
        val result = TripValidator.validateOutwardStart(
            startKm = -1,
            startPlace = "",
            conditions = "",
            guide = "1"
        )

        assertThat(result.isInvalid()).isTrue()
    }
}
