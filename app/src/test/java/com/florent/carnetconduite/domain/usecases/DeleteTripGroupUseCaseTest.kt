package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
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
class DeleteTripGroupUseCaseTest {

    @Mock
    private lateinit var repository: TripRepository

    @Mock
    private lateinit var logger: AppLogger

    private lateinit var useCase: DeleteTripGroupUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = DeleteTripGroupUseCase(repository, logger)
    }

    // ==================== Tests de succès ====================

    @Test
    fun `invoke should delete both trips when given valid IDs`() = runTest {
        // Given: deux IDs de trajets valides
        val outwardId = 1L
        val returnId = 2L

        // When: on supprime le groupe de trajets
        val result = useCase(outwardId, returnId)

        // Then: devrait réussir
        assertTrue(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)

        // Vérifier que les deux trajets ont été supprimés
        verify(repository).deleteTrip(outwardId)
        verify(repository).deleteTrip(returnId)
    }

    @Test
    fun `invoke should delete outward first then return`() = runTest {
        // Given: deux IDs de trajets
        val outwardId = 1L
        val returnId = 2L

        // When: on supprime le groupe de trajets
        useCase(outwardId, returnId)

        // Then: l'ordre de suppression devrait être aller puis retour
        inOrder(repository) {
            verify(repository).deleteTrip(outwardId)
            verify(repository).deleteTrip(returnId)
        }
    }

    @Test
    fun `invoke should handle same IDs for outward and return`() = runTest {
        // Given: le même ID pour l'aller et le retour (cas dégénéré)
        val tripId = 1L

        // When: on supprime le groupe
        val result = useCase(tripId, tripId)

        // Then: devrait quand même réussir
        assertTrue(result is Result.Success)

        // Les deux appels de suppression devraient être faits
        verify(repository, times(2)).deleteTrip(tripId)
    }

    // ==================== Tests de gestion d'erreur ====================

    @Test
    fun `invoke should return Error when deleting outward trip fails`() = runTest {
        // Given: la suppression de l'aller échoue
        val outwardId = 1L
        val returnId = 2L

        whenever(repository.deleteTrip(outwardId))
            .thenThrow(RuntimeException("Failed to delete outward trip"))

        // When: on supprime le groupe
        val result = useCase(outwardId, returnId)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Failed to delete outward trip"))

        // Le trajet retour ne devrait pas être supprimé
        verify(repository).deleteTrip(outwardId)
        verify(repository, never()).deleteTrip(returnId)
    }

    @Test
    fun `invoke should return Error when deleting return trip fails`() = runTest {
        // Given: la suppression du retour échoue
        val outwardId = 1L
        val returnId = 2L

        whenever(repository.deleteTrip(returnId))
            .thenThrow(RuntimeException("Failed to delete return trip"))

        // When: on supprime le groupe
        val result = useCase(outwardId, returnId)

        // Then: devrait retourner une erreur
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Failed to delete return trip"))

        // Les deux méthodes devraient avoir été appelées
        verify(repository).deleteTrip(outwardId)
        verify(repository).deleteTrip(returnId)
    }

    @Test
    fun `invoke should handle database constraint violations`() = runTest {
        // Given: une violation de contrainte de base de données
        val outwardId = 1L
        val returnId = 2L

        whenever(repository.deleteTrip(outwardId))
            .thenThrow(RuntimeException("FOREIGN KEY constraint failed"))

        // When: on supprime le groupe
        val result = useCase(outwardId, returnId)

        // Then: devrait retourner une erreur appropriée
        assertTrue(result is Result.Error)
        val errorMessage = (result as Result.Error).message
        assertTrue(errorMessage.contains("FOREIGN KEY") || errorMessage.contains("constraint"))
    }

    // ==================== Tests des cas limites ====================

    @Test
    fun `invoke should handle zero IDs`() = runTest {
        // Given: des IDs à zéro (potentiellement invalides)
        val outwardId = 0L
        val returnId = 0L

        // When: on supprime le groupe
        val result = useCase(outwardId, returnId)

        // Then: le use case devrait quand même appeler le repository
        // (c'est au repository de décider si l'ID est valide)
        verify(repository).deleteTrip(outwardId)
        verify(repository).deleteTrip(returnId)
    }

    @Test
    fun `invoke should handle negative IDs`() = runTest {
        // Given: des IDs négatifs (potentiellement invalides)
        val outwardId = -1L
        val returnId = -2L

        // When: on supprime le groupe
        val result = useCase(outwardId, returnId)

        // Then: le use case devrait quand même appeler le repository
        verify(repository).deleteTrip(outwardId)
        verify(repository).deleteTrip(returnId)
    }

    @Test
    fun `invoke should handle very large IDs`() = runTest {
        // Given: des IDs très grands
        val outwardId = Long.MAX_VALUE
        val returnId = Long.MAX_VALUE - 1

        // When: on supprime le groupe
        val result = useCase(outwardId, returnId)

        // Then: devrait fonctionner normalement
        verify(repository).deleteTrip(outwardId)
        verify(repository).deleteTrip(returnId)
    }

    // ==================== Tests de comportement atomique ====================

    @Test
    fun `invoke should not rollback outward deletion if return deletion fails`() = runTest {
        // Given: la suppression du retour échoue après que l'aller a réussi
        val outwardId = 1L
        val returnId = 2L

        // L'aller réussit (comportement par défaut du mock)
        whenever(repository.deleteTrip(returnId))
            .thenThrow(RuntimeException("Return deletion failed"))

        // When: on supprime le groupe
        val result = useCase(outwardId, returnId)

        // Then: l'aller reste supprimé (pas de rollback automatique)
        assertTrue(result is Result.Error)

        // Les deux appels devraient avoir été faits
        verify(repository).deleteTrip(outwardId)
        verify(repository).deleteTrip(returnId)

        // Note: Dans un vrai scénario, on pourrait vouloir implémenter
        // une transaction pour éviter ce cas. Ce test documente le comportement actuel.
    }

    // ==================== Tests de performance ====================

    @Test
    fun `invoke should execute deletions sequentially not in parallel`() = runTest {
        // Given: deux IDs de trajets
        val outwardId = 1L
        val returnId = 2L

        // When: on supprime le groupe
        useCase(outwardId, returnId)

        // Then: les suppressions devraient être séquentielles
        // (vérifié par inOrder qui vérifie l'ordre d'exécution)
        inOrder(repository) {
            verify(repository).deleteTrip(outwardId)
            verify(repository).deleteTrip(returnId)
        }
    }
}
