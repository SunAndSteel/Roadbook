package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.utils.onError
import com.florent.carnetconduite.repository.TripRepository

/**
 * Use Case pour supprimer un groupe de trajets (aller + retour éventuel).
 * Gère la suppression atomique du groupe complet.
 */
class DeleteTripGroupUseCase(
    private val repository: TripRepository,
    private val logger: AppLogger
) {
    /**
     * Supprime un groupe de trajets
     *
     * @param tripGroup Groupe à supprimer (contient aller + retour éventuel)
     * @return Result<Unit> Succès ou erreur
     */
    suspend operator fun invoke(tripGroup: TripGroup): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("DeleteTripGroup: aller=${tripGroup.outward.id}, retour=${tripGroup.returnTrip?.id}")

        // Supprimer le trajet aller
        val deleteOutwardResult = repository.delete(tripGroup.outward)
        if (deleteOutwardResult.isError()) {
            logger.logError("Failed to delete outward trip", (deleteOutwardResult as Result.Error).exception)
            throw deleteOutwardResult.exception
        }

        // Supprimer le trajet retour si présent
        tripGroup.returnTrip?.let { returnTrip ->
            val deleteReturnResult = repository.delete(returnTrip)
            if (deleteReturnResult.isError()) {
                logger.logError("Failed to delete return trip", (deleteReturnResult as Result.Error).exception)
                throw deleteReturnResult.exception
            }
        }

        logger.logOperationEnd("DeleteTripGroup", true)
        logger.log("Trip group deleted successfully")
    }.onError { error ->
        logger.logError("Failed to delete trip group", error)
    }
}