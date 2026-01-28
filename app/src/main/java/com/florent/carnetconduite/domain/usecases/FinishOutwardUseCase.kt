package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.utils.onError
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository

/**
 * Use Case pour terminer un trajet aller.
 * Gère la validation et la mise à jour du trajet.
 */
class FinishOutwardUseCase(
    private val repository: TripRepository,
    private val validator: TripValidator = TripValidator,
    private val logger: AppLogger
) {
    /**
     * Termine un trajet aller
     */
    suspend operator fun invoke(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        allowInconsistentKm: Boolean = false
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("FinishOutward: trip $tripId, $endKm km")

        // Récupérer le trajet - CORRECTION ICI
        val tripResult = repository.getTripById(tripId)
        val trip = tripResult.getOrNull()
            ?: throw IllegalArgumentException("Trajet introuvable (ID: $tripId)")

        // Validation du lieu
        val placeValidation = validator.validatePlace(endPlace)
        if (placeValidation.isInvalid()) {
            val errorMsg = placeValidation.getErrorMessage() ?: "Lieu invalide"
            logger.logError("Place validation failed: $errorMsg")
            throw IllegalArgumentException(errorMsg)
        }

        // Validation du kilométrage
        if (!allowInconsistentKm) {
            val kmValidation = validator.validateEndKm(trip.startKm, endKm)
            if (kmValidation.isInvalid()) {
                val errorMsg = kmValidation.getErrorMessage() ?: "Kilométrage invalide"
                logger.logError("Km validation failed: $errorMsg")
                throw KmInconsistencyException(errorMsg, trip.startKm, endKm)
            }
        } else {
            logger.log("Km inconsistency accepted by user: ${trip.startKm} -> $endKm")
        }

        // Terminer le trajet
        val now = System.currentTimeMillis()
        repository.finishTrip(tripId, endKm, endPlace.trim(), now).getOrThrow()

        logger.logOperationEnd("FinishOutward", true)
        logger.log("Trip $tripId finished: ${trip.startKm} -> $endKm km")
    }.onError { error ->
        logger.logError("Failed to finish outward trip", error)
    }

    /**
     * Exception spéciale pour indiquer une incohérence de kilométrage
     */
    class KmInconsistencyException(
        message: String,
        val startKm: Int,
        val endKm: Int
    ) : IllegalArgumentException(message)
}