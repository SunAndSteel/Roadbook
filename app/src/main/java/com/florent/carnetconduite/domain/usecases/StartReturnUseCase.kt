package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.utils.onError
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
    suspend operator fun invoke(
        returnTripId: Long,
        actualStartKm: Int?
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("StartReturn: trip $returnTripId")

        // Récupérer le trajet - CORRECTION ICI
        val tripResult = repository.getTripById(returnTripId)
        val trip = tripResult.getOrNull()
            ?: throw IllegalArgumentException("Trajet retour introuvable (ID: $returnTripId)")

        val kmToUse = actualStartKm ?: trip.startKm

        // Validation du kilométrage
        val validation = validator.validateStartKm(kmToUse)
        if (validation.isInvalid()) {
            throw IllegalArgumentException(validation.getErrorMessage() ?: "Kilométrage invalide")
        }

        // Démarrer le retour
        val now = System.currentTimeMillis()
        repository.startReturn(returnTripId, actualStartKm, now).getOrThrow()

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
    suspend operator fun invoke(
        tripId: Long,
        endKm: Int,
        allowInconsistentKm: Boolean = false
    ): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("FinishReturn: trip $tripId, $endKm km")

        // Récupérer le trajet - CORRECTION ICI
        val tripResult = repository.getTripById(tripId)
        val trip = tripResult.getOrNull()
            ?: throw IllegalArgumentException("Trajet introuvable (ID: $tripId)")

        // Validation du kilométrage
        if (!allowInconsistentKm) {
            val kmValidation = validator.validateEndKm(trip.startKm, endKm)
            if (kmValidation.isInvalid()) {
                val errorMsg = kmValidation.getErrorMessage() ?: "Kilométrage invalide"
                logger.logError("Km validation failed: $errorMsg")
                throw FinishOutwardUseCase.KmInconsistencyException(errorMsg, trip.startKm, endKm)
            }
        }

        // Terminer le retour
        val now = System.currentTimeMillis()
        val endPlace = trip.endPlace ?: trip.startPlace
        repository.finishTrip(tripId, endKm, endPlace, now).getOrThrow()

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
    suspend operator fun invoke(returnTripId: Long): Result<Unit> = Result.runCatchingSuspend {
        logger.logOperationStart("CancelReturn: trip $returnTripId")

        repository.cancelReturn(returnTripId).getOrThrow()

        logger.logOperationEnd("CancelReturn", true)
        logger.log("Return trip $returnTripId cancelled")
    }.onError { error ->
        logger.logError("Failed to cancel return trip", error)
    }
}