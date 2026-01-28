package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus
import com.florent.carnetconduite.domain.utils.AppLogger
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.domain.utils.onError
import com.florent.carnetconduite.domain.validators.TripValidator
import com.florent.carnetconduite.repository.TripRepository
import java.time.LocalDate

/**
 * Use Case pour démarrer un trajet aller.
 * Encapsule la logique métier de validation et création.
 */
class StartOutwardUseCase(
    private val repository: TripRepository,
    private val validator: TripValidator = TripValidator,
    private val logger: AppLogger
) {
    /**
     * Démarre un nouveau trajet aller
     *
     * @param startKm Kilométrage de départ
     * @param startPlace Lieu de départ
     * @param conditions Conditions météo (optionnel)
     * @param guide Numéro du guide accompagnateur
     *
     * @return Result<Long> ID du trajet créé, ou erreur si validation échoue
     */
    suspend operator fun invoke(
        startKm: Int,
        startPlace: String,
        conditions: String = "",
        guide: String = "1"
    ): Result<Long> = Result.runCatchingSuspend {
        logger.logOperationStart("StartOutward: $startPlace, $startKm km")

        // Validation des données
        val validation = validator.validateOutwardStart(startKm, startPlace, conditions, guide)
        if (validation.isInvalid()) {
            val errorMsg = validation.getErrorMessage()!!
            logger.logError("Validation failed: $errorMsg")
            throw IllegalArgumentException(errorMsg)
        }

        // Création du trajet
        val now = System.currentTimeMillis()
        val date = LocalDate.now().toString()

        val trip = Trip(
            startKm = startKm,
            startPlace = startPlace.trim(),
            startTime = now,
            status = TripStatus.ACTIVE,
            conditions = conditions.trim(),
            guide = guide,
            date = date,
            endKm = null,
            endPlace = null,
            endTime = null,
            isReturn = false,
            pairedTripId = null
        )

        // Insertion en base
        val tripId = repository.insert(trip)

        repository.saveOngoingSessionId(tripId)

        logger.logOperationEnd("StartOutward", true)
        logger.log("Outward trip created with ID: $tripId")

        tripId
    }.onError { error ->
        logger.logError("Failed to start outward trip", error)
    }
}