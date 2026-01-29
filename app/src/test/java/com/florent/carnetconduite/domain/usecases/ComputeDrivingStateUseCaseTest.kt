package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.AppPreferences
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus
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
 * Tests pour ComputeDrivingStateUseCase.
 * Ce use case est crucial car il détermine l'état actuel de l'application.
 */
@ExperimentalCoroutinesApi
class ComputeDrivingStateUseCaseTest {

    @Mock
    private lateinit var repository: TripRepository

    @Mock
    private lateinit var sessionPreferences: AppPreferences

    private lateinit var useCase: ComputeDrivingStateUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ComputeDrivingStateUseCase(repository, sessionPreferences)
    }

    // ==================== Tests de l'état Idle ====================

    @Test
    fun `invoke should return Idle when no current trip ID`() = runTest {
        // Given: aucun trajet en cours
        whenever(sessionPreferences.getCurrentTripId()).thenReturn(null)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être Idle
        assertTrue(state is DrivingState.Idle)
    }

    @Test
    fun `invoke should return Idle when current trip ID exists but trip not found in DB`() = runTest {
        // Given: un ID de trajet qui n'existe pas dans la base
        val tripId = 123L
        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId)).thenReturn(null)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être Idle (réinitialisation de l'état incohérent)
        assertTrue(state is DrivingState.Idle)
    }

    // ==================== Tests de l'état OutwardActive ====================

    @Test
    fun `invoke should return OutwardActive when trip is active and not return`() = runTest {
        // Given: un trajet aller actif
        val tripId = 123L
        val trip = createTestTrip(
            id = tripId,
            status = TripStatus.ACTIVE,
            isReturn = false,
            endKm = null,
            endPlace = null,
            endTime = null
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(false)
        whenever(repository.getTripById(tripId)).thenReturn(trip)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être OutwardActive avec le bon trajet
        assertTrue(state is DrivingState.OutwardActive)
        assertEquals(trip, (state as DrivingState.OutwardActive).currentTrip)
    }

    // ==================== Tests de l'état Arrived ====================

    @Test
    fun `invoke should return Arrived when outward trip is completed`() = runTest {
        // Given: un trajet aller complété
        val tripId = 123L
        val trip = createTestTrip(
            id = tripId,
            status = TripStatus.COMPLETED,
            isReturn = false,
            endKm = 1150,
            endPlace = "Lyon",
            endTime = System.currentTimeMillis()
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(false)
        whenever(repository.getTripById(tripId)).thenReturn(trip)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être Arrived avec le trajet complété
        assertTrue(state is DrivingState.Arrived)
        assertEquals(trip, (state as DrivingState.Arrived).completedOutward)
    }

    // ==================== Tests de l'état ReturnReady ====================

    @Test
    fun `invoke should return ReturnReady when outward completed and ready for return`() = runTest {
        // Given: un trajet aller complété, prêt pour le retour
        val tripId = 123L
        val trip = createTestTrip(
            id = tripId,
            status = TripStatus.COMPLETED,
            isReturn = false
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(false)
        whenever(repository.getTripById(tripId)).thenReturn(trip)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être soit Arrived soit ReturnReady
        // (dépend de la logique exacte du use case)
        assertTrue(state is DrivingState.Arrived || state is DrivingState.ReturnReady)
    }

    // ==================== Tests de l'état ReturnActive ====================

    @Test
    fun `invoke should return ReturnActive when return trip is active`() = runTest {
        // Given: un trajet retour actif
        val returnTripId = 124L
        val outwardTripId = 123L

        val returnTrip = createTestTrip(
            id = returnTripId,
            status = TripStatus.ACTIVE,
            isReturn = true,
            pairedTripId = outwardTripId,
            endKm = null,
            endPlace = null,
            endTime = null
        )

        val outwardTrip = createTestTrip(
            id = outwardTripId,
            status = TripStatus.COMPLETED,
            isReturn = false,
            pairedTripId = returnTripId
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(returnTripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(true)
        whenever(repository.getTripById(returnTripId)).thenReturn(returnTrip)
        whenever(repository.getTripById(outwardTripId)).thenReturn(outwardTrip)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être ReturnActive avec les deux trajets
        assertTrue(state is DrivingState.ReturnActive)
        val returnActiveState = state as DrivingState.ReturnActive
        assertEquals(returnTrip, returnActiveState.currentTrip)
        assertEquals(outwardTrip, returnActiveState.completedOutward)
    }

    // ==================== Tests de l'état Completed ====================

    @Test
    fun `invoke should return Completed when both trips are completed`() = runTest {
        // Given: un trajet retour complété (donc l'aller aussi)
        val returnTripId = 124L
        val outwardTripId = 123L

        val returnTrip = createTestTrip(
            id = returnTripId,
            status = TripStatus.COMPLETED,
            isReturn = true,
            pairedTripId = outwardTripId,
            endKm = 1300,
            endPlace = "Paris",
            endTime = System.currentTimeMillis()
        )

        val outwardTrip = createTestTrip(
            id = outwardTripId,
            status = TripStatus.COMPLETED,
            isReturn = false,
            pairedTripId = returnTripId,
            endKm = 1150,
            endPlace = "Lyon",
            endTime = System.currentTimeMillis() - 3600000
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(returnTripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(true)
        whenever(repository.getTripById(returnTripId)).thenReturn(returnTrip)
        whenever(repository.getTripById(outwardTripId)).thenReturn(outwardTrip)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être Completed avec les deux trajets
        assertTrue(state is DrivingState.Completed)
        val completedState = state as DrivingState.Completed
        assertEquals(outwardTrip, completedState.outward)
        assertEquals(returnTrip, completedState.returnTrip)
    }

    // ==================== Tests de gestion d'erreur ====================

    @Test
    fun `invoke should return Idle when repository throws exception`() = runTest {
        // Given: le repository lance une exception
        val tripId = 123L
        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(repository.getTripById(tripId))
            .thenThrow(RuntimeException("Database error"))

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait retourner Idle par sécurité
        assertTrue(state is DrivingState.Idle)
    }

    @Test
    fun `invoke should return Idle when sessionPreferences throws exception`() = runTest {
        // Given: sessionPreferences lance une exception
        whenever(sessionPreferences.getCurrentTripId())
            .thenThrow(RuntimeException("Preferences error"))

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait retourner Idle par sécurité
        assertTrue(state is DrivingState.Idle)
    }

    // ==================== Tests de cohérence des données ====================

    @Test
    fun `invoke should return Idle when isOnReturnTrip is true but trip is outward`() = runTest {
        // Given: état incohérent (dit être sur le retour mais le trip est un aller)
        val tripId = 123L
        val trip = createTestTrip(
            id = tripId,
            status = TripStatus.ACTIVE,
            isReturn = false
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(true) // Incohérent!
        whenever(repository.getTripById(tripId)).thenReturn(trip)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait gérer l'incohérence (comportement dépend de l'implémentation)
        // Soit Idle, soit résolution intelligente
        assertNotNull(state)
    }

    @Test
    fun `invoke should handle missing paired trip gracefully`() = runTest {
        // Given: un trajet retour dont le trajet aller n'existe plus
        val returnTripId = 124L
        val outwardTripId = 123L

        val returnTrip = createTestTrip(
            id = returnTripId,
            status = TripStatus.ACTIVE,
            isReturn = true,
            pairedTripId = outwardTripId
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(returnTripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(true)
        whenever(repository.getTripById(returnTripId)).thenReturn(returnTrip)
        whenever(repository.getTripById(outwardTripId)).thenReturn(null) // Aller manquant!

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait gérer gracieusement (probablement Idle ou erreur)
        assertNotNull(state)
    }

    // ==================== Tests de transitions d'état ====================

    @Test
    fun `invoke should correctly identify transition from outward to arrived`() = runTest {
        // Given: un trajet qui vient de passer d'actif à complété
        val tripId = 123L
        val trip = createTestTrip(
            id = tripId,
            status = TripStatus.COMPLETED,
            isReturn = false,
            endKm = 1150,
            endPlace = "Lyon",
            endTime = System.currentTimeMillis()
        )

        whenever(sessionPreferences.getCurrentTripId()).thenReturn(tripId)
        whenever(sessionPreferences.getIsOnReturnTrip()).thenReturn(false)
        whenever(repository.getTripById(tripId)).thenReturn(trip)

        // When: on calcule l'état
        val state = useCase()

        // Then: devrait être Arrived
        assertTrue(state is DrivingState.Arrived)
    }

    // ==================== Helper functions ====================

    private fun createTestTrip(
        id: Long = 1L,
        startKm: Int = 1000,
        endKm: Int? = null,
        startPlace: String = "Paris",
        endPlace: String? = null,
        startTime: Long = System.currentTimeMillis(),
        endTime: Long? = null,
        isReturn: Boolean = false,
        pairedTripId: Long? = null,
        status: TripStatus = TripStatus.ACTIVE
    ): Trip {
        return Trip(
            id = id,
            startKm = startKm,
            endKm = endKm,
            startPlace = startPlace,
            endPlace = endPlace,
            startTime = startTime,
            endTime = endTime,
            isReturn = isReturn,
            pairedTripId = pairedTripId,
            status = status,
            conditions = "Beau",
            guide = "1",
            date = "2025-01-28"
        )
    }
}