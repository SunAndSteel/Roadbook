package com.florent.carnetconduite.domain.usecase

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.utils.onError
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository

/**
 * Use Case unifié pour toutes les opérations d'édition de trajets.
 * Regroupe les éditions d'heure et de kilométrage.
 */
class EditTripUseCase(
    private val repository: TripRepository,
    private val validator: TripValidator = TripValidator,
    private val logger: AppLogger
) {
    /**
     * Modifie l'heure de départ d'un trajet
     */
    suspend fun editStartTime(tripId: Long, newStartTime: Long): Result<Unit> =
        Result.runCatchingSuspend {
            logger.logOperationStart("EditStartTime: trip $tripId")

            // Validation du timestamp
            val validation = validator.validateTimestamp(newStartTime)
            if (validation.isInvalid()) {
                throw IllegalArgumentException(validation.getErrorMessage()!!)
            }

            repository.updateStartTime(tripId, newStartTime)

            logger.logOperationEnd("EditStartTime", true)
            logger.log("Trip $tripId start time updated")
        }.onError { error ->
            logger.logError("Failed to edit start time", error)
        }

    /**
     * Modifie l'heure d'arrivée d'un trajet
     */
    suspend fun editEndTime(tripId: Long, newEndTime: Long): Result<Unit> =
        Result.runCatchingSuspend {
            logger.logOperationStart("EditEndTime: trip $tripId")

            // Validation du timestamp
            val validation = validator.validateTimestamp(newEndTime)
            if (validation.isInvalid()) {
                throw IllegalArgumentException(validation.getErrorMessage()!!)
            }

            // Vérifier que endTime > startTime
            val trip = repository.getTripById(tripId)
                ?: throw IllegalArgumentException("Trajet introuvable")

            val timeRangeValidation = validator.validateTimeRange(trip.startTime, newEndTime)
            if (timeRangeValidation.isInvalid()) {
                throw IllegalArgumentException(timeRangeValidation.getErrorMessage()!!)
            }

            repository.updateEndTime(tripId, newEndTime)

            logger.logOperationEnd("EditEndTime", true)
            logger.log("Trip $tripId end time updated")
        }.onError { error ->
            logger.logError("Failed to edit end time", error)
        }

    /**
     * Modifie le kilométrage de départ d'un trajet
     */
    suspend fun editStartKm(tripId: Long, newStartKm: Int): Result<Unit> =
        Result.runCatchingSuspend {
            logger.logOperationStart("EditStartKm: trip $tripId to $newStartKm")

            // Validation du kilométrage
            val validation = validator.validateStartKm(newStartKm)
            if (validation.isInvalid()) {
                throw IllegalArgumentException(validation.getErrorMessage()!!)
            }

            // Vérifier que ça reste cohérent avec endKm si présent
            val trip = repository.getTripById(tripId)
                ?: throw IllegalArgumentException("Trajet introuvable")

            trip.endKm?.let { endKm ->
                if (newStartKm >= endKm) {
                    throw IllegalArgumentException(
                        "Le nouveau kilométrage de départ ($newStartKm) doit être inférieur à l'arrivée ($endKm)"
                    )
                }
            }

            repository.updateStartKm(tripId, newStartKm)

            logger.logOperationEnd("EditStartKm", true)
            logger.log("Trip $tripId start km updated to $newStartKm")
        }.onError { error ->
            logger.logError("Failed to edit start km", error)
        }

    /**
     * Modifie le kilométrage d'arrivée d'un trajet
     */
    suspend fun editEndKm(tripId: Long, newEndKm: Int): Result<Unit> =
        Result.runCatchingSuspend {
            logger.logOperationStart("EditEndKm: trip $tripId to $newEndKm")

            // Récupérer le trajet
            val trip = repository.getTripById(tripId)
                ?: throw IllegalArgumentException("Trajet introuvable")

            // Validation du kilométrage par rapport au départ
            val validation = validator.validateEndKm(trip.startKm, newEndKm)
            if (validation.isInvalid()) {
                throw IllegalArgumentException(validation.getErrorMessage()!!)
            }

            repository.updateEndKm(tripId, newEndKm)

            logger.logOperationEnd("EditEndKm", true)
            logger.log("Trip $tripId end km updated to $newEndKm")
        }.onError { error ->
            logger.logError("Failed to edit end km", error)
        }
}