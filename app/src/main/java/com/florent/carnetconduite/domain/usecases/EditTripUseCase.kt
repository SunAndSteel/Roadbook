package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.utils.onError
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository

/**
 * Use Case unifié pour toutes les opérations d'édition de trajets.
 */
class EditTripUseCase(
    private val repository: TripRepository,
    private val validator: TripValidator = TripValidator,
    private val logger: AppLogger
) {
    /**
     * Modifie la date d'un trajet (format ISO-8601 attendu)
     */
    suspend fun editDate(tripId: Long, newDate: String): Result<Unit> =
        Result.runCatchingSuspend {
            logger.logOperationStart("EditDate: trip $tripId")

            val trimmedDate = newDate.trim()
            if (trimmedDate.isBlank()) {
                throw IllegalArgumentException("La date ne peut pas être vide")
            }

            try {
                java.time.LocalDate.parse(trimmedDate)
            } catch (e: Exception) {
                throw IllegalArgumentException("Format de date invalide (AAAA-MM-JJ)")
            }

            repository.updateDate(tripId, trimmedDate).getOrThrow()

            logger.logOperationEnd("EditDate", true)
            logger.log("Trip $tripId date updated")
        }.onError { error ->
            logger.logError("Failed to edit trip date", error)
        }

    /**
     * Modifie les conditions météo d'un trajet
     */
    suspend fun editConditions(tripId: Long, newConditions: String): Result<Unit> =
        Result.runCatchingSuspend {
            logger.logOperationStart("EditConditions: trip $tripId")

            val trimmedConditions = newConditions.trim()
            val validation = validator.validateConditions(trimmedConditions)
            if (validation.isInvalid()) {
                throw IllegalArgumentException(validation.getErrorMessage() ?: "Conditions invalides")
            }

            repository.updateConditions(tripId, trimmedConditions).getOrThrow()

            logger.logOperationEnd("EditConditions", true)
            logger.log("Trip $tripId conditions updated")
        }.onError { error ->
            logger.logError("Failed to edit trip conditions", error)
        }

    /**
     * Modifie l'heure de départ d'un trajet
     */
    suspend fun editStartTime(tripId: Long, newStartTime: Long): Result<Unit> =
        Result.runCatchingSuspend {
            logger.logOperationStart("EditStartTime: trip $tripId")

            // Validation du timestamp
            val validation = validator.validateTimestamp(newStartTime)
            if (validation.isInvalid()) {
                throw IllegalArgumentException(validation.getErrorMessage() ?: "Timestamp invalide")
            }

            repository.updateStartTime(tripId, newStartTime).getOrThrow()

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
                throw IllegalArgumentException(validation.getErrorMessage() ?: "Timestamp invalide")
            }

            // Vérifier que endTime > startTime - CORRECTION ICI
            val tripResult = repository.getTripById(tripId)
            val trip = tripResult.getOrNull()
                ?: throw IllegalArgumentException("Trajet introuvable")

            val timeRangeValidation = validator.validateTimeRange(trip.startTime, newEndTime)
            if (timeRangeValidation.isInvalid()) {
                throw IllegalArgumentException(timeRangeValidation.getErrorMessage() ?: "Période invalide")
            }

            repository.updateEndTime(tripId, newEndTime).getOrThrow()

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
                throw IllegalArgumentException(validation.getErrorMessage() ?: "Kilométrage invalide")
            }

            // Vérifier cohérence avec endKm - CORRECTION ICI
            val tripResult = repository.getTripById(tripId)
            val trip = tripResult.getOrNull()
                ?: throw IllegalArgumentException("Trajet introuvable")

            trip.endKm?.let { endKm ->
                if (newStartKm >= endKm) {
                    throw IllegalArgumentException(
                        "Le nouveau kilométrage de départ ($newStartKm) doit être inférieur à l'arrivée ($endKm)"
                    )
                }
            }

            repository.updateStartKm(tripId, newStartKm).getOrThrow()

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

            // Récupérer le trajet - CORRECTION ICI
            val tripResult = repository.getTripById(tripId)
            val trip = tripResult.getOrNull()
                ?: throw IllegalArgumentException("Trajet introuvable")

            // Validation du kilométrage par rapport au départ
            val validation = validator.validateEndKm(trip.startKm, newEndKm)
            if (validation.isInvalid()) {
                throw IllegalArgumentException(validation.getErrorMessage() ?: "Kilométrage invalide")
            }

            repository.updateEndKm(tripId, newEndKm).getOrThrow()

            logger.logOperationEnd("EditEndKm", true)
            logger.log("Trip $tripId end km updated to $newEndKm")
        }.onError { error ->
            logger.logError("Failed to edit end km", error)
        }
}
