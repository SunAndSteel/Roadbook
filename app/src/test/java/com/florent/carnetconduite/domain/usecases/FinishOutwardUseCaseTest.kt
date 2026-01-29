package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.data.AppPreferences
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus
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

@ExperimentalCoroutinesApi
class FinishOutwardUseCaseTest {

    @Mock
    private lateinit var repository: TripRepository

    @Mock
    private lateinit var validator: TripValidator

    @Mock
    private lateinit var sessionPreferences: AppPreferences

    private lateinit var useCase: FinishOutwardUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = FinishOutwardUseCase(repository, validator, sessionPreferences)
    }

    // ==================== Tests de récupération du trajet ====================

    @Test
    fun `invoke should return Error when no active trip exists`() = runTest {
        // Given: aucun trajet actif
        val endKm = 1150
        val endPlace = "Lyon"

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(null)

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Aucun trajet actif trouvé", (result as Result.Error).message)

        // Ne devrait pas appeler le repository
        verify(repository, never()).getTripById(any())
        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when trip not found in database`() = runTest {
        // Given: un tripId existe mais pas dans la base
        val tripId = 123L
        val endKm = 1150
        val endPlace = "Lyon"

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(null)

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Trajet non trouvé", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    // ==================== Tests de validation ====================

    @Test
    fun `invoke should return Error when endKm validation fails`() = runTest {
        // Given: un kilométrage de fin invalide
        val tripId = 123L
        val startKm = 1000
        val endKm = -100
        val endPlace = "Lyon"

        val currentTrip = createTestTrip(id = tripId, startKm = startKm)

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(endKm))
            .thenReturn(ValidationResult.Invalid("Le kilométrage doit être positif"))

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le kilométrage doit être positif", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when endPlace validation fails`() = runTest {
        // Given: un lieu de fin invalide
        val tripId = 123L
        val startKm = 1000
        val endKm = 1150
        val endPlace = ""

        val currentTrip = createTestTrip(id = tripId, startKm = startKm)

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(endKm)).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(endPlace))
            .thenReturn(ValidationResult.Invalid("Le lieu ne peut pas être vide"))

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le lieu ne peut pas être vide", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when km range validation fails`() = runTest {
        // Given: un intervalle de kilométrage invalide
        val tripId = 123L
        val startKm = 1000
        val endKm = 900 // Inférieur au départ
        val endPlace = "Lyon"

        val currentTrip = createTestTrip(id = tripId, startKm = startKm)

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(endKm)).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(endPlace)).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(startKm, endKm))
            .thenReturn(ValidationResult.Invalid("Le kilométrage d'arrivée doit être supérieur au kilométrage de départ"))

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals(
            "Le kilométrage d'arrivée doit être supérieur au kilométrage de départ",
            (result as Result.Error).message
        )

        verify(repository, never()).updateTrip(any())
    }

    // ==================== Tests de succès ====================

    @Test
    fun `invoke should update trip and session when all validations pass`() = runTest {
        // Given: des données valides
        val tripId = 123L
        val startKm = 1000
        val endKm = 1150
        val endPlace = "Lyon"
        val startTime = System.currentTimeMillis() - 3600000 // 1 heure avant

        val currentTrip = createTestTrip(
            id = tripId,
            startKm = startKm,
            startTime = startTime
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(endKm)).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(endPlace)).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(startKm, endKm)).thenReturn(ValidationResult.Valid)

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait réussir
        assertTrue(result is Result.Success)

        // Vérifier que le trip a été mis à jour avec les bonnes données
        verify(repository).updateTrip(argThat { trip ->
            trip.id == tripId &&
                    trip.endKm == endKm &&
                    trip.endPlace == endPlace.trim() &&
                    trip.endTime != null &&
                    trip.status == TripStatus.COMPLETED
        })

        // Vérifier que la session a été réinitialisée
        verify(sessionPreferences).setCurrentTripId(null)
        verify(sessionPreferences).setIsOnReturnTrip(false)
    }

    @Test
    fun `invoke should trim endPlace before updating`() = runTest {
        // Given: un lieu avec des espaces
        val tripId = 123L
        val startKm = 1000
        val endKm = 1150
        val endPlace = "  Lyon  "

        val currentTrip = createTestTrip(id = tripId, startKm = startKm)

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)

        // When: on termine le trajet
        useCase(endKm, endPlace)

        // Then: le lieu devrait être trimé
        verify(repository).updateTrip(argThat { trip ->
            trip.endPlace == "Lyon"
        })
    }

    @Test
    fun `invoke should set endTime to current time`() = runTest {
        // Given: un trajet valide
        val tripId = 123L
        val startKm = 1000
        val endKm = 1150
        val endPlace = "Lyon"
        val startTime = System.currentTimeMillis() - 3600000

        val currentTrip = createTestTrip(
            id = tripId,
            startKm = startKm,
            startTime = startTime
        )

        val beforeInvoke = System.currentTimeMillis()

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)

        // When: on termine le trajet
        useCase(endKm, endPlace)

        val afterInvoke = System.currentTimeMillis()

        // Then: endTime devrait être entre beforeInvoke et afterInvoke
        verify(repository).updateTrip(argThat { trip ->
            val endTime = trip.endTime ?: 0L
            endTime >= beforeInvoke && endTime <= afterInvoke
        })
    }

    // ==================== Tests de gestion d'erreur ====================

    @Test
    fun `invoke should return Error when repository update fails`() = runTest {
        // Given: le repository lance une exception
        val tripId = 123L
        val startKm = 1000
        val endKm = 1150
        val endPlace = "Lyon"

        val currentTrip = createTestTrip(id = tripId, startKm = startKm)

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)
        whenever(repository.updateTrip(any()))
            .thenThrow(RuntimeException("Database error"))

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Database error"))

        // La session ne devrait pas être réinitialisée
        verify(sessionPreferences, never()).setCurrentTripId(null)
    }

    // ==================== Tests des cas limites ====================

    @Test
    fun `invoke should handle minimum valid distance`() = runTest {
        // Given: une distance minimale (1 km)
        val tripId = 123L
        val startKm = 1000
        val endKm = 1001
        val endPlace = "Proche"

        val currentTrip = createTestTrip(id = tripId, startKm = startKm)

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait réussir
        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke should handle maximum valid distance`() = runTest {
        // Given: une distance maximale (1000 km)
        val tripId = 123L
        val startKm = 1000
        val endKm = 2000
        val endPlace = "Loin"

        val currentTrip = createTestTrip(id = tripId, startKm = startKm)

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(currentTrip)
        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)

        // When: on termine le trajet
        val result = useCase(endKm, endPlace)

        // Then: devrait réussir
        assertTrue(result is Result.Success)
    }

    // ==================== Helper functions ====================

    private fun createTestTrip(
        id: Long = 1L,
        startKm: Int = 1000,
        startPlace: String = "Paris",
        startTime: Long = System.currentTimeMillis(),
        isReturn: Boolean = false
    ): Trip {
        return Trip(
            id = id,
            startKm = startKm,
            startPlace = startPlace,
            startTime = startTime,
            isReturn = isReturn,
            endKm = null,
            endPlace = null,
            endTime = null,
            pairedTripId = null,
            status = TripStatus.ACTIVE,
            conditions = "Beau",
            guide = "1",
            date = "2025-01-28"
        )
    }
}