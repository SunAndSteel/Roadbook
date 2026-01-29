package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.data.AppPreferences
import com.florent.carnetconduite.domain.models.ValidationResult
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Tests unitaires pour StartOutwardUseCase.
 * Ces tests utilisent des mocks pour isoler la logique du use case.
 *
 * Note: Pour exécuter ces tests, ajoutez ces dépendances dans build.gradle:
 * testImplementation "org.mockito.kotlin:mockito-kotlin:5.1.0"
 * testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
 */
@ExperimentalCoroutinesApi
class StartOutwardUseCaseTest {

    @Mock
    private lateinit var repository: TripRepository

    @Mock
    private lateinit var validator: TripValidator

    @Mock
    private lateinit var sessionPreferences: AppPreferences

    private lateinit var useCase: StartOutwardUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = StartOutwardUseCase(repository, validator, sessionPreferences)
    }

    // ==================== Tests de validation ====================

    @Test
    fun `invoke should return Error when startKm validation fails`() = runTest {
        // Given: un kilométrage invalide
        val startKm = -100
        val startPlace = "Paris"
        val conditions = "Beau"
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(startKm))
            .thenReturn(ValidationResult.Invalid("Le kilométrage doit être positif"))

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: le résultat devrait être une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le kilométrage doit être positif", (result as Result.Error).message)

        // Vérifier qu'on n'a pas appelé le repository
        verify(repository, never()).insertTrip(any())
    }

    @Test
    fun `invoke should return Error when startPlace validation fails`() = runTest {
        // Given: un lieu invalide
        val startKm = 1000
        val startPlace = ""
        val conditions = "Beau"
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(startKm))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(startPlace))
            .thenReturn(ValidationResult.Invalid("Le lieu ne peut pas être vide"))

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: le résultat devrait être une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le lieu ne peut pas être vide", (result as Result.Error).message)

        // Vérifier qu'on n'a pas appelé le repository
        verify(repository, never()).insertTrip(any())
    }

    @Test
    fun `invoke should return Error when conditions validation fails`() = runTest {
        // Given: des conditions invalides
        val startKm = 1000
        val startPlace = "Paris"
        val conditions = ""
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(startKm))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(startPlace))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(conditions))
            .thenReturn(ValidationResult.Invalid("Les conditions météo ne peuvent pas être vides"))

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: le résultat devrait être une erreur
        assertTrue(result is Result.Error)
        assertEquals("Les conditions météo ne peuvent pas être vides", (result as Result.Error).message)

        // Vérifier qu'on n'a pas appelé le repository
        verify(repository, never()).insertTrip(any())
    }

    @Test
    fun `invoke should return Error when guide validation fails`() = runTest {
        // Given: un guide invalide
        val startKm = 1000
        val startPlace = "Paris"
        val conditions = "Beau"
        val guide = "3"
        val date = "2025-01-28"

        whenever(validator.validateKm(startKm))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(startPlace))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(conditions))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validateGuide(guide))
            .thenReturn(ValidationResult.Invalid("Le guide doit être 1 ou 2"))

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: le résultat devrait être une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le guide doit être 1 ou 2", (result as Result.Error).message)

        // Vérifier qu'on n'a pas appelé le repository
        verify(repository, never()).insertTrip(any())
    }

    // ==================== Tests de succès ====================

    @Test
    fun `invoke should insert trip and update session when all validations pass`() = runTest {
        // Given: des données valides
        val startKm = 1000
        val startPlace = "Paris"
        val conditions = "Beau temps"
        val guide = "1"
        val date = "2025-01-28"
        val currentTimeMillis = System.currentTimeMillis()
        val insertedTripId = 42L

        whenever(validator.validateKm(startKm))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(startPlace))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(conditions))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validateGuide(guide))
            .thenReturn(ValidationResult.Valid)
        whenever(repository.insertTrip(any()))
            .thenReturn(insertedTripId)

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: le résultat devrait être un succès
        assertTrue(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)

        // Vérifier que le trip a été inséré avec les bonnes données
        verify(repository).insertTrip(argThat { trip ->
            trip.startKm == startKm &&
                    trip.startPlace == startPlace.trim() &&
                    trip.conditions == conditions &&
                    trip.guide == guide &&
                    trip.date == date &&
                    !trip.isReturn &&
                    trip.endKm == null &&
                    trip.endPlace == null &&
                    trip.endTime == null &&
                    trip.pairedTripId == null
        })

        // Vérifier que la session a été mise à jour
        verify(sessionPreferences).setCurrentTripId(insertedTripId)
        verify(sessionPreferences).setIsOnReturnTrip(false)
    }

    @Test
    fun `invoke should trim startPlace before inserting`() = runTest {
        // Given: un lieu avec des espaces
        val startKm = 1000
        val startPlace = "  Paris  "
        val conditions = "Beau"
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateGuide(any())).thenReturn(ValidationResult.Valid)
        whenever(repository.insertTrip(any())).thenReturn(1L)

        // When: on démarre un trajet aller
        useCase(startKm, startPlace, conditions, guide, date)

        // Then: le lieu devrait être trimé
        verify(repository).insertTrip(argThat { trip ->
            trip.startPlace == "Paris"
        })
    }

    // ==================== Tests de gestion d'erreur ====================

    @Test
    fun `invoke should return Error when repository throws exception`() = runTest {
        // Given: le repository lance une exception
        val startKm = 1000
        val startPlace = "Paris"
        val conditions = "Beau"
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateGuide(any())).thenReturn(ValidationResult.Valid)
        whenever(repository.insertTrip(any()))
            .thenThrow(RuntimeException("Database error"))

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: le résultat devrait être une erreur
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Database error"))

        // La session ne devrait pas être mise à jour
        verify(sessionPreferences, never()).setCurrentTripId(any())
        verify(sessionPreferences, never()).setIsOnReturnTrip(any())
    }

    @Test
    fun `invoke should return Error when sessionPreferences throws exception`() = runTest {
        // Given: sessionPreferences lance une exception
        val startKm = 1000
        val startPlace = "Paris"
        val conditions = "Beau"
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateGuide(any())).thenReturn(ValidationResult.Valid)
        whenever(repository.insertTrip(any())).thenReturn(1L)
        whenever(sessionPreferences.setCurrentTripId(any()))
            .thenThrow(RuntimeException("Preferences error"))

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: le résultat devrait être une erreur
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Preferences error"))
    }

    // ==================== Tests des cas limites ====================

    @Test
    fun `invoke should handle special characters in place name`() = runTest {
        // Given: un lieu avec des caractères spéciaux
        val startKm = 1000
        val startPlace = "Saint-Étienne (42)"
        val conditions = "Beau"
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateGuide(any())).thenReturn(ValidationResult.Valid)
        whenever(repository.insertTrip(any())).thenReturn(1L)

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: devrait réussir
        assertTrue(result is Result.Success)
        verify(repository).insertTrip(argThat { trip ->
            trip.startPlace == startPlace
        })
    }

    @Test
    fun `invoke should handle maximum valid km`() = runTest {
        // Given: le kilométrage maximum valide
        val startKm = 1_000_000
        val startPlace = "Paris"
        val conditions = "Beau"
        val guide = "1"
        val date = "2025-01-28"

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateConditions(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateGuide(any())).thenReturn(ValidationResult.Valid)
        whenever(repository.insertTrip(any())).thenReturn(1L)

        // When: on démarre un trajet aller
        val result = useCase(startKm, startPlace, conditions, guide, date)

        // Then: devrait réussir
        assertTrue(result is Result.Success)
        verify(repository).insertTrip(argThat { trip ->
            trip.startKm == startKm
        })
    }
}