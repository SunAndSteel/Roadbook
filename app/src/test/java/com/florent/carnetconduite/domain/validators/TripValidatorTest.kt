package com.florent.carnetconduite.domain.validators

import com.florent.carnetconduite.domain.models.ValidationResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour TripValidator.
 * Ces tests vérifient toutes les règles de validation métier pour les trajets.
 */
class TripValidatorTest {

    private lateinit var validator: TripValidator

    @Before
    fun setup() {
        validator = TripValidator()
    }

    // ==================== Tests de validation du kilométrage ====================

    @Test
    fun `validateKm should return Valid when km is positive`() {
        // Given: un kilométrage positif
        val km = 1000

        // When: on valide le kilométrage
        val result = validator.validateKm(km)

        // Then: la validation devrait réussir
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateKm should return Invalid when km is zero`() {
        // Given: un kilométrage à zéro
        val km = 0

        // When: on valide le kilométrage
        val result = validator.validateKm(km)

        // Then: la validation devrait échouer avec le bon message
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le kilométrage doit être positif", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateKm should return Invalid when km is negative`() {
        // Given: un kilométrage négatif
        val km = -500

        // When: on valide le kilométrage
        val result = validator.validateKm(km)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le kilométrage doit être positif", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateKm should return Invalid when km is unrealistically high`() {
        // Given: un kilométrage trop élevé (> 1 million)
        val km = 1_500_000

        // When: on valide le kilométrage
        val result = validator.validateKm(km)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le kilométrage semble irréaliste", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateKm should return Valid when km is at maximum limit`() {
        // Given: un kilométrage à la limite maximale (1 million)
        val km = 1_000_000

        // When: on valide le kilométrage
        val result = validator.validateKm(km)

        // Then: la validation devrait réussir (limite inclusive)
        assertTrue(result is ValidationResult.Valid)
    }

    // ==================== Tests de validation du lieu ====================

    @Test
    fun `validatePlace should return Valid when place is not blank`() {
        // Given: un lieu valide
        val place = "Paris"

        // When: on valide le lieu
        val result = validator.validatePlace(place)

        // Then: la validation devrait réussir
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validatePlace should return Invalid when place is empty`() {
        // Given: un lieu vide
        val place = ""

        // When: on valide le lieu
        val result = validator.validatePlace(place)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le lieu ne peut pas être vide", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validatePlace should return Invalid when place is blank`() {
        // Given: un lieu avec seulement des espaces
        val place = "   "

        // When: on valide le lieu
        val result = validator.validatePlace(place)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le lieu ne peut pas être vide", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validatePlace should return Valid when place has leading and trailing spaces`() {
        // Given: un lieu avec des espaces avant et après
        val place = "  Lyon  "

        // When: on valide le lieu
        val result = validator.validatePlace(place)

        // Then: la validation devrait réussir (trim est appliqué)
        assertTrue(result is ValidationResult.Valid)
    }

    // ==================== Tests de validation de l'intervalle de kilométrage ====================

    @Test
    fun `validateKmRange should return Valid when endKm is greater than startKm`() {
        // Given: un intervalle valide
        val startKm = 1000
        val endKm = 1150

        // When: on valide l'intervalle
        val result = validator.validateKmRange(startKm, endKm)

        // Then: la validation devrait réussir
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateKmRange should return Invalid when endKm equals startKm`() {
        // Given: un intervalle où départ = arrivée
        val startKm = 1000
        val endKm = 1000

        // When: on valide l'intervalle
        val result = validator.validateKmRange(startKm, endKm)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(
            "Le kilométrage d'arrivée doit être supérieur au kilométrage de départ",
            (result as ValidationResult.Invalid).message
        )
    }

    @Test
    fun `validateKmRange should return Invalid when endKm is less than startKm`() {
        // Given: un intervalle où arrivée < départ
        val startKm = 1000
        val endKm = 900

        // When: on valide l'intervalle
        val result = validator.validateKmRange(startKm, endKm)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(
            "Le kilométrage d'arrivée doit être supérieur au kilométrage de départ",
            (result as ValidationResult.Invalid).message
        )
    }

    @Test
    fun `validateKmRange should return Invalid when distance is unrealistically long`() {
        // Given: un intervalle avec une distance > 1000 km
        val startKm = 1000
        val endKm = 2500

        // When: on valide l'intervalle
        val result = validator.validateKmRange(startKm, endKm)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(
            "La distance parcourue semble trop importante (> 1000 km)",
            (result as ValidationResult.Invalid).message
        )
    }

    @Test
    fun `validateKmRange should return Valid when distance is at maximum limit`() {
        // Given: un intervalle exactement à la limite (1000 km)
        val startKm = 1000
        val endKm = 2000

        // When: on valide l'intervalle
        val result = validator.validateKmRange(startKm, endKm)

        // Then: la validation devrait réussir (limite inclusive)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateKmRange should return Valid for a small trip of 1 km`() {
        // Given: un très petit trajet (1 km)
        val startKm = 1000
        val endKm = 1001

        // When: on valide l'intervalle
        val result = validator.validateKmRange(startKm, endKm)

        // Then: la validation devrait réussir
        assertTrue(result is ValidationResult.Valid)
    }

    // ==================== Tests de validation des conditions météo ====================

    @Test
    fun `validateConditions should return Valid when conditions is not blank`() {
        // Given: des conditions météo valides
        val conditions = "Beau temps"

        // When: on valide les conditions
        val result = validator.validateConditions(conditions)

        // Then: la validation devrait réussir
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateConditions should return Invalid when conditions is empty`() {
        // Given: des conditions vides
        val conditions = ""

        // When: on valide les conditions
        val result = validator.validateConditions(conditions)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Les conditions météo ne peuvent pas être vides", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateConditions should return Invalid when conditions is blank`() {
        // Given: des conditions avec seulement des espaces
        val conditions = "    "

        // When: on valide les conditions
        val result = validator.validateConditions(conditions)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Les conditions météo ne peuvent pas être vides", (result as ValidationResult.Invalid).message)
    }

    // ==================== Tests de validation du guide ====================

    @Test
    fun `validateGuide should return Valid when guide is 1`() {
        // Given: guide = "1"
        val guide = "1"

        // When: on valide le guide
        val result = validator.validateGuide(guide)

        // Then: la validation devrait réussir
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateGuide should return Valid when guide is 2`() {
        // Given: guide = "2"
        val guide = "2"

        // When: on valide le guide
        val result = validator.validateGuide(guide)

        // Then: la validation devrait réussir
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateGuide should return Invalid when guide is empty`() {
        // Given: un guide vide
        val guide = ""

        // When: on valide le guide
        val result = validator.validateGuide(guide)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le guide doit être sélectionné", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateGuide should return Invalid when guide is not 1 or 2`() {
        // Given: un guide invalide
        val guide = "3"

        // When: on valide le guide
        val result = validator.validateGuide(guide)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le guide doit être 1 ou 2", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateGuide should return Invalid when guide is alphanumeric`() {
        // Given: un guide avec des lettres
        val guide = "abc"

        // When: on valide le guide
        val result = validator.validateGuide(guide)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le guide doit être 1 ou 2", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateGuide should return Invalid when guide has spaces`() {
        // Given: un guide avec des espaces
        val guide = "  "

        // When: on valide le guide
        val result = validator.validateGuide(guide)

        // Then: la validation devrait échouer
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Le guide doit être sélectionné", (result as ValidationResult.Invalid).message)
    }
}