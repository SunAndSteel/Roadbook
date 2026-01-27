package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.domain.utils.onError

/**
 * Use Case pour décider du type de trajet après l'arrivée.
 * Gère la création d'un retour préparé ou l'enregistrement comme trajet simple.
 */
class DecideTripTypeUseCase(
    private val repository: TripRepository,
    private val logger: AppLogger
) {
    /**
     * Décide du type de trajet
     *
     * @param tripId ID du trajet aller terminé
     * @param prepareReturn true pour préparer un retour, false pour trajet simple
     *
     * @return Result<Unit> Succès ou erreur
     */
    suspend operator fun invoke(
        tripId: Long,
        prepareReturn: Boolean
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("DecideTripType: trip $tripId, prepareReturn=$prepareReturn")

        val trip = repository.getTripById(tripId)
            ?: throw IllegalArgumentException("Trajet introuvable (ID: $tripId)")

        if (prepareReturn) {
            // Préparer un retour
            repository.finishAndPrepareReturn(
                tripId = tripId,
                endKm = trip.endKm ?: throw IllegalStateException("Trip not finished"),
                endPlace = trip.endPlace ?: throw IllegalStateException("Trip not finished"),
                endTime = trip.endTime ?: System.currentTimeMillis()
            )
            logger.log("Return trip prepared for trip $tripId")
        } else {
            // Marquer comme trajet simple (créer un SKIPPED)
            repository.createSkippedReturn(tripId)
            logger.log("Trip $tripId marked as simple (no return)")
        }

        logger.logOperationEnd("DecideTripType", true)
    }.onError { error ->
        logger.logError("Failed to decide trip type", error)
    }
}