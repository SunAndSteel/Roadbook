package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.utils.onError
import com.florent.carnetconduite.repository.TripRepository

/**
 * Use Case pour décider du type de trajet après l'arrivée.
 */
class DecideTripTypeUseCase(
    private val repository: TripRepository,
    private val logger: AppLogger
) {
    suspend operator fun invoke(
        tripId: Long,
        prepareReturn: Boolean
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("DecideTripType: trip $tripId, prepareReturn=$prepareReturn")

        // Récupérer le trajet - CORRECTION ICI
        val tripResult = repository.getTripById(tripId)
        val trip = tripResult.getOrNull()
            ?: throw IllegalArgumentException("Trajet introuvable (ID: $tripId)")

        if (prepareReturn) {
            // Préparer un retour
            val endKm = trip.endKm ?: throw IllegalStateException("Trip not finished")
            val endPlace = trip.endPlace ?: throw IllegalStateException("Trip not finished")
            val endTime = trip.endTime ?: System.currentTimeMillis()

            repository.finishAndPrepareReturn(
                tripId = tripId,
                endKm = endKm,
                endPlace = endPlace,
                endTime = endTime
            ).getOrThrow()

            logger.log("Return trip prepared for trip $tripId")
        } else {
            // Marquer comme trajet simple sans créer de retour.
            repository.markOutwardAsSimple(tripId).getOrThrow()
            logger.log("Trip $tripId marked as simple (no return trip created)")
        }

        logger.logOperationEnd("DecideTripType", true)
    }.onError { error ->
        logger.logError("Failed to decide trip type", error)
    }
}
