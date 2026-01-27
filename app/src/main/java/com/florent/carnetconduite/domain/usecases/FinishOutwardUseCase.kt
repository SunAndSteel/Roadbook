package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository
import com.florent.carnetconduite.domain.utils.onError

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
     *
     * @param tripId ID du trajet à terminer
     * @param endKm Kilométrage d'arrivée
     * @param endPlace Lieu d'arrivée
     * @param allowInconsistentKm Si true, autorise endKm < startKm (après confirmation utilisateur)
     *
     * @return Result<Unit> Succès ou erreur
     */
    suspend operator fun invoke(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        allowInconsistentKm: Boolean = false
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("FinishOutward: trip $tripId, $endKm km")

        // Récupérer le trajet
        val trip = repository.getTripById(tripId)
            ?: throw IllegalArgumentException("Trajet introuvable (ID: $tripId)")

        // Validation du lieu
        val placeValidation = validator.validatePlace(endPlace)
        if (placeValidation.isInvalid()) {
            val errorMsg = placeValidation.getErrorMessage()!!
            logger.logError("Place validation failed: $errorMsg")
            throw IllegalArgumentException(errorMsg)
        }

        // Validation du kilométrage (avec possibilité de bypass)
        if (!allowInconsistentKm) {
            val kmValidation = validator.validateEndKm(trip.startKm, endKm)
            if (kmValidation.isInvalid()) {
                val errorMsg = kmValidation.getErrorMessage()!!
                logger.logError("Km validation failed: $errorMsg")
                // On lance une exception spéciale pour que le ViewModel puisse demander confirmation
                throw KmInconsistencyException(errorMsg, trip.startKm, endKm)
            }
        } else {
            logger.log("Km inconsistency accepted by user: ${trip.startKm} -> $endKm")
        }

        // Terminer le trajet
        val now = System.currentTimeMillis()
        repository.finishTrip(tripId, endKm, endPlace.trim(), now)
        repository.clearOngoingSessionId()

        logger.logOperationEnd("FinishOutward", true)
        logger.log("Trip $tripId finished: ${trip.startKm} -> $endKm km")
    }.onError { error ->
        logger.logError("Failed to finish outward trip", error)
    }

    /**
     * Exception spéciale pour indiquer une incohérence de kilométrage
     * Le ViewModel peut capturer cette exception pour demander confirmation
     */
    class KmInconsistencyException(
        message: String,
        val startKm: Int,
        val endKm: Int
    ) : IllegalArgumentException(message)
}