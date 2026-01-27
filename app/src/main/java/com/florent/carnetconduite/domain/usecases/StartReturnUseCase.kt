package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.onError
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository

/**
 * Use Case pour démarrer un trajet retour préparé
 */
class StartReturnUseCase(
    private val repository: TripRepository,
    private val validator: TripValidator = TripValidator,
    private val logger: AppLogger
) {
    /**
     * Démarre un trajet retour
     *
     * @param returnTripId ID du trajet retour (en statut READY)
     * @param actualStartKm Kilométrage réel de départ (peut différer de celui prévu)
     *
     * @return Result<Unit> Succès ou erreur
     */
    suspend operator fun invoke(
        returnTripId: Long,
        actualStartKm: Int?
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("StartReturn: trip $returnTripId")

        val trip = repository.getTripById(returnTripId)
            ?: throw IllegalArgumentException("Trajet retour introuvable (ID: $returnTripId)")

        val kmToUse = actualStartKm ?: trip.startKm

        // Validation du kilométrage
        val validation = validator.validateStartKm(kmToUse)
        if (validation.isInvalid()) {
            throw IllegalArgumentException(validation.getErrorMessage()!!)
        }

        // Démarrer le retour
        val now = System.currentTimeMillis()
        repository.startReturn(returnTripId, actualStartKm, now)

        logger.logOperationEnd("StartReturn", true)
        logger.log("Return trip $returnTripId started with $kmToUse km")
    }.onError { error ->
        logger.logError("Failed to start return trip", error)
    }
}

/**
 * Use Case pour terminer un trajet retour
 */
class FinishReturnUseCase(
    private val repository: TripRepository,
    private val validator: TripValidator = TripValidator,
    private val logger: AppLogger
) {
    /**
     * Termine un trajet retour
     *
     * @param tripId ID du trajet retour
     * @param endKm Kilométrage d'arrivée
     * @param allowInconsistentKm Si true, autorise endKm < startKm
     *
     * @return Result<Unit> Succès ou erreur
     */
    suspend operator fun invoke(
        tripId: Long,
        endKm: Int,
        allowInconsistentKm: Boolean = false
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("FinishReturn: trip $tripId, $endKm km")

        val trip = repository.getTripById(tripId)
            ?: throw IllegalArgumentException("Trajet introuvable (ID: $tripId)")

        // Validation du kilométrage
        if (!allowInconsistentKm) {
            val kmValidation = validator.validateEndKm(trip.startKm, endKm)
            if (kmValidation.isInvalid()) {
                val errorMsg = kmValidation.getErrorMessage()!!
                logger.logError("Km validation failed: $errorMsg")
                throw FinishOutwardUseCase.KmInconsistencyException(errorMsg, trip.startKm, endKm)
            }
        }

        // Terminer le retour
        val now = System.currentTimeMillis()
        repository.finishTrip(tripId, endKm, trip.endPlace ?: "", now)
        repository.clearOngoingSessionId()

        logger.logOperationEnd("FinishReturn", true)
        logger.log("Return trip $tripId finished: ${trip.startKm} -> $endKm km")
    }.onError { error ->
        logger.logError("Failed to finish return trip", error)
    }
}

/**
 * Use Case pour annuler un trajet retour préparé
 */
class CancelReturnUseCase(
    private val repository: TripRepository,
    private val logger: AppLogger
) {
    /**
     * Annule un trajet retour
     *
     * @param returnTripId ID du trajet retour à annuler
     *
     * @return Result<Unit> Succès ou erreur
     */
    suspend operator fun invoke(returnTripId: Long): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("CancelReturn: trip $returnTripId")

        repository.cancelReturn(returnTripId)

        logger.logOperationEnd("CancelReturn", true)
        logger.log("Return trip $returnTripId cancelled")
    }.onError { error ->
        logger.logError("Failed to cancel return trip", error)
    }
}