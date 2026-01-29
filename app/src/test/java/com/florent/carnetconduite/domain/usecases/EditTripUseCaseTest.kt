package com.florent.carnetconduite.domain.usecases

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
class EditTripUseCaseTest {

    @Mock
    private lateinit var repository: TripRepository

    @Mock
    private lateinit var validator: TripValidator

    private lateinit var useCase: EditTripUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = EditTripUseCase(repository, validator)
    }

    // ==================== Tests de validation de l'aller ====================

    @Test
    fun `invoke should return Error when outward startKm validation fails`() = runTest {
        // Given: un kilométrage de départ invalide
        val outwardId = 1L
        val returnId = 2L
        val outwardStartKm = -100
        val outwardEndKm = 1150

        whenever(validator.validateKm(outwardStartKm))
            .thenReturn(ValidationResult.Invalid("Le kilométrage doit être positif"))

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = outwardStartKm,
            outwardEndKm = outwardEndKm,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = 1150,
            returnEndKm = 1300,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le kilométrage doit être positif", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when outward km range validation fails`() = runTest {
        // Given: un intervalle de kilométrage invalide pour l'aller
        val outwardId = 1L
        val returnId = 2L
        val outwardStartKm = 1000
        val outwardEndKm = 900 // Inférieur au départ

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(outwardStartKm, outwardEndKm))
            .thenReturn(ValidationResult.Invalid("Le kilométrage d'arrivée doit être supérieur au kilométrage de départ"))

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = outwardStartKm,
            outwardEndKm = outwardEndKm,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = 900,
            returnEndKm = 1000,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals(
            "Le kilométrage d'arrivée doit être supérieur au kilométrage de départ",
            (result as Result.Error).message
        )

        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when outward place validation fails`() = runTest {
        // Given: un lieu de départ invalide pour l'aller
        val outwardId = 1L
        val returnId = 2L

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(""))
            .thenReturn(ValidationResult.Invalid("Le lieu ne peut pas être vide"))
        whenever(validator.validatePlace(not(eq("")))).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)

        // When: on édite avec un lieu vide
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1150,
            outwardStartPlace = "", // Lieu vide
            outwardEndPlace = "Lyon",
            returnStartKm = 1150,
            returnEndKm = 1300,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le lieu ne peut pas être vide", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    // ==================== Tests de validation du retour ====================

    @Test
    fun `invoke should return Error when return startKm validation fails`() = runTest {
        // Given: un kilométrage de départ invalide pour le retour
        val outwardId = 1L
        val returnId = 2L
        val returnStartKm = -100

        whenever(validator.validateKm(argThat { it != returnStartKm }))
            .thenReturn(ValidationResult.Valid)
        whenever(validator.validateKm(returnStartKm))
            .thenReturn(ValidationResult.Invalid("Le kilométrage doit être positif"))
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(1000, 1150)).thenReturn(ValidationResult.Valid)

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1150,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = returnStartKm,
            returnEndKm = 1300,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Le kilométrage doit être positif", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when return km range validation fails`() = runTest {
        // Given: un intervalle invalide pour le retour
        val outwardId = 1L
        val returnId = 2L
        val returnStartKm = 1150
        val returnEndKm = 1100 // Inférieur au départ

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(1000, 1150)).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(returnStartKm, returnEndKm))
            .thenReturn(ValidationResult.Invalid("Le kilométrage d'arrivée doit être supérieur au kilométrage de départ"))

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1150,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = returnStartKm,
            returnEndKm = returnEndKm,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals(
            "Le kilométrage d'arrivée doit être supérieur au kilométrage de départ",
            (result as Result.Error).message
        )

        verify(repository, never()).updateTrip(any())
    }

    // ==================== Tests de validation de la continuité ====================

    @Test
    fun `invoke should return Error when outward end km does not match return start km`() = runTest {
        // Given: les kilométrages ne se suivent pas
        val outwardId = 1L
        val returnId = 2L
        val outwardEndKm = 1150
        val returnStartKm = 1200 // Différent de outwardEndKm

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = outwardEndKm,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = returnStartKm,
            returnEndKm = 1300,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals(
            "Le kilométrage de départ du retour doit correspondre au kilométrage d'arrivée de l'aller",
            (result as Result.Error).message
        )

        verify(repository, never()).updateTrip(any())
    }

    // ==================== Tests de succès ====================

    @Test
    fun `invoke should update both trips when all validations pass`() = runTest {
        // Given: des données valides
        val outwardId = 1L
        val returnId = 2L
        val outwardStartKm = 1000
        val outwardEndKm = 1150
        val outwardStartPlace = "Paris"
        val outwardEndPlace = "Lyon"
        val returnStartKm = 1150
        val returnEndKm = 1300
        val returnStartPlace = "Lyon"
        val returnEndPlace = "Paris"

        val outwardTrip = createTestTrip(id = outwardId, startKm = outwardStartKm, isReturn = false)
        val returnTrip = createTestTrip(id = returnId, startKm = returnStartKm, isReturn = true)

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)
        whenever(repository.getTripById(outwardId)).thenReturn(outwardTrip)
        whenever(repository.getTripById(returnId)).thenReturn(returnTrip)

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = outwardStartKm,
            outwardEndKm = outwardEndKm,
            outwardStartPlace = outwardStartPlace,
            outwardEndPlace = outwardEndPlace,
            returnStartKm = returnStartKm,
            returnEndKm = returnEndKm,
            returnStartPlace = returnStartPlace,
            returnEndPlace = returnEndPlace
        )

        // Then: devrait réussir
        assertTrue(result is Result.Success)

        // Vérifier que l'aller a été mis à jour
        verify(repository).updateTrip(argThat { trip ->
            trip.id == outwardId &&
                    trip.startKm == outwardStartKm &&
                    trip.endKm == outwardEndKm &&
                    trip.startPlace == outwardStartPlace &&
                    trip.endPlace == outwardEndPlace
        })

        // Vérifier que le retour a été mis à jour
        verify(repository).updateTrip(argThat { trip ->
            trip.id == returnId &&
                    trip.startKm == returnStartKm &&
                    trip.endKm == returnEndKm &&
                    trip.startPlace == returnStartPlace &&
                    trip.endPlace == returnEndPlace
        })

        // Vérifier que updateTrip a été appelé exactement 2 fois
        verify(repository, times(2)).updateTrip(any())
    }

    @Test
    fun `invoke should trim all places before updating`() = runTest {
        // Given: des lieux avec des espaces
        val outwardId = 1L
        val returnId = 2L
        val outwardTrip = createTestTrip(id = outwardId, isReturn = false)
        val returnTrip = createTestTrip(id = returnId, isReturn = true)

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)
        whenever(repository.getTripById(outwardId)).thenReturn(outwardTrip)
        whenever(repository.getTripById(returnId)).thenReturn(returnTrip)

        // When: on édite avec des lieux contenant des espaces
        useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1150,
            outwardStartPlace = "  Paris  ",
            outwardEndPlace = "  Lyon  ",
            returnStartKm = 1150,
            returnEndKm = 1300,
            returnStartPlace = "  Lyon  ",
            returnEndPlace = "  Paris  "
        )

        // Then: les lieux devraient être trimés
        verify(repository).updateTrip(argThat { trip ->
            trip.id == outwardId &&
                    trip.startPlace == "Paris" &&
                    trip.endPlace == "Lyon"
        })

        verify(repository).updateTrip(argThat { trip ->
            trip.id == returnId &&
                    trip.startPlace == "Lyon" &&
                    trip.endPlace == "Paris"
        })
    }

    // ==================== Tests de gestion d'erreur ====================

    @Test
    fun `invoke should return Error when outward trip not found`() = runTest {
        // Given: le trajet aller n'existe pas
        val outwardId = 1L
        val returnId = 2L

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)
        whenever(repository.getTripById(outwardId)).thenReturn(null)

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1150,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = 1150,
            returnEndKm = 1300,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Trajet aller non trouvé", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when return trip not found`() = runTest {
        // Given: le trajet retour n'existe pas
        val outwardId = 1L
        val returnId = 2L
        val outwardTrip = createTestTrip(id = outwardId, isReturn = false)

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)
        whenever(repository.getTripById(outwardId)).thenReturn(outwardTrip)
        whenever(repository.getTripById(returnId)).thenReturn(null)

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1150,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = 1150,
            returnEndKm = 1300,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertEquals("Trajet retour non trouvé", (result as Result.Error).message)

        verify(repository, never()).updateTrip(any())
    }

    @Test
    fun `invoke should return Error when repository throws exception`() = runTest {
        // Given: le repository lance une exception
        val outwardId = 1L
        val returnId = 2L
        val outwardTrip = createTestTrip(id = outwardId, isReturn = false)
        val returnTrip = createTestTrip(id = returnId, isReturn = true)

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)
        whenever(repository.getTripById(outwardId)).thenReturn(outwardTrip)
        whenever(repository.getTripById(returnId)).thenReturn(returnTrip)
        whenever(repository.updateTrip(any()))
            .thenThrow(RuntimeException("Database error"))

        // When: on édite le groupe de trajets
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1150,
            outwardStartPlace = "Paris",
            outwardEndPlace = "Lyon",
            returnStartKm = 1150,
            returnEndKm = 1300,
            returnStartPlace = "Lyon",
            returnEndPlace = "Paris"
        )

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Database error"))
    }

    // ==================== Tests des cas limites ====================

    @Test
    fun `invoke should handle minimum distances for both trips`() = runTest {
        // Given: distances minimales (1 km chacune)
        val outwardId = 1L
        val returnId = 2L
        val outwardTrip = createTestTrip(id = outwardId, isReturn = false)
        val returnTrip = createTestTrip(id = returnId, isReturn = true)

        whenever(validator.validateKm(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validatePlace(any())).thenReturn(ValidationResult.Valid)
        whenever(validator.validateKmRange(any(), any())).thenReturn(ValidationResult.Valid)
        whenever(repository.getTripById(outwardId)).thenReturn(outwardTrip)
        whenever(repository.getTripById(returnId)).thenReturn(returnTrip)

        // When: on édite avec des distances minimales
        val result = useCase(
            outwardId = outwardId,
            returnId = returnId,
            outwardStartKm = 1000,
            outwardEndKm = 1001, // 1 km
            outwardStartPlace = "Paris",
            outwardEndPlace = "Banlieue",
            returnStartKm = 1001,
            returnEndKm = 1002, // 1 km
            returnStartPlace = "Banlieue",
            returnEndPlace = "Paris"
        )

        // Then: devrait réussir
        assertTrue(result is Result.Success)
    }

    // ==================== Helper functions ====================

    private fun createTestTrip(
        id: Long = 1L,
        startKm: Int = 1000,
        isReturn: Boolean = false
    ): Trip {
        return Trip(
            id = id,
            startKm = startKm,
            startPlace = "Paris",
            startTime = System.currentTimeMillis(),
            isReturn = isReturn,
            endKm = if (isReturn) 1300 else 1150,
            endPlace = if (isReturn) "Paris" else "Lyon",
            endTime = System.currentTimeMillis() + 3600000,
            pairedTripId = if (isReturn) 1L else 2L,
            status = TripStatus.COMPLETED,
            conditions = "Beau",
            guide = "1",
            date = "2025-01-28"
        )
    }
}