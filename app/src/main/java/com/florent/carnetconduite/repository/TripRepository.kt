package com.florent.carnetconduite.repository

import android.content.Context
import com.florent.carnetconduite.data.SessionPreferences
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.data.TripDao
import com.florent.carnetconduite.domain.utils.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les opérations sur les trajets.
 * Toutes les méthodes suspend sont wrappées avec Result pour une gestion d'erreurs robuste.
 */
class TripRepository(
    private val tripDao: TripDao,
    private val context: Context
) {
    // Flows - pas besoin de wrapper car ils gèrent déjà les erreurs via le flow
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    val activeTrip: Flow<Trip?> = tripDao.getActiveTrip()
    val ongoingSessionId: Flow<Long?> = SessionPreferences.getOngoingSessionId(context)

    /**
     * Insère un nouveau trajet
     * @return Result<Long> ID du trajet créé ou erreur
     */
    suspend fun insert(trip: Trip): Long =
        tripDao.insertTrip(trip)

    /**
     * Met à jour un trajet existant
     */
    suspend fun update(trip: Trip): Result<Unit> = Result.runCatchingSuspend {
        tripDao.updateTrip(trip)
    }

    /**
     * Supprime un trajet
     */
    suspend fun delete(trip: Trip): Result<Unit> = Result.runCatchingSuspend {
        tripDao.deleteTrip(trip)
    }

    /**
     * Récupère un trajet par son ID
     * @return Result<Trip?> Trajet ou null si non trouvé
     */
    suspend fun getTripById(id: Long): Result<Trip?> = Result.runCatchingSuspend {
        tripDao.getTripById(id)
    }

    /**
     * Récupère le trajet retour prêt
     */
    suspend fun getReadyReturnTrip(): Result<Trip?> = Result.runCatchingSuspend {
        tripDao.getReadyReturnTrip()
    }

    /**
     * Termine un trajet
     */
    suspend fun finishTrip(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        endTime: Long
    ): Result<Unit> = Result.runCatchingSuspend {
        tripDao.finishTrip(tripId, endKm, endPlace, endTime)
        SessionPreferences.clearOngoingSessionId(context)
    }

    /**
     * Termine le trajet aller et prépare le retour
     * @return Result<Long> ID du trajet retour créé
     */
    suspend fun finishAndPrepareReturn(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        endTime: Long
    ): Result<Long> = Result.runCatchingSuspend {
        val returnId = tripDao.finishAndPrepareReturn(tripId, endKm, endPlace, endTime)
        SessionPreferences.saveOngoingSessionId(context, returnId)
        returnId
    }

    /**
     * Crée un retour SKIPPED (trajet simple, pas de retour)
     * @return Result<Long> ID du trajet skipped créé
     */
    suspend fun createSkippedReturn(tripId: Long): Result<Long> = Result.runCatchingSuspend {
        val skippedId = tripDao.createSkippedReturn(tripId)
        SessionPreferences.clearOngoingSessionId(context)
        skippedId
    }

    /**
     * Marque un trajet aller comme "simple" sans créer de retour.
     */
    suspend fun markOutwardAsSimple(tripId: Long): Result<Unit> = Result.runCatchingSuspend {
        tripDao.markOutwardAsSimple(tripId)
        SessionPreferences.clearOngoingSessionId(context)
    }

    /**
     * Démarre un trajet retour préparé
     */
    suspend fun startReturn(
        returnTripId: Long,
        actualStartKm: Int?,
        startTime: Long
    ): Result<Unit> = Result.runCatchingSuspend {
        tripDao.startReturn(returnTripId, actualStartKm, startTime)
        SessionPreferences.saveOngoingSessionId(context, returnTripId)
    }

    /**
     * Annule un trajet retour
     */
    suspend fun cancelReturn(returnTripId: Long): Result<Unit> = Result.runCatchingSuspend {
        tripDao.cancelTrip(returnTripId)
        SessionPreferences.clearOngoingSessionId(context)
    }

    /**
     * Sauvegarde l'ID de session en cours
     */
    suspend fun saveOngoingSessionId(sessionId: Long): Result<Unit> = Result.runCatchingSuspend {
        SessionPreferences.saveOngoingSessionId(context, sessionId)
    }

    /**
     * Efface l'ID de session en cours
     */
    suspend fun clearOngoingSessionId(): Result<Unit> = Result.runCatchingSuspend {
        SessionPreferences.clearOngoingSessionId(context)
    }

    /**
     * Met à jour l'heure de fin d'un trajet
     */
    suspend fun updateEndTime(tripId: Long, newEndTime: Long): Result<Unit> = Result.runCatchingSuspend {
        tripDao.updateEndTime(tripId, newEndTime)
    }

    /**
     * Met à jour le kilométrage de départ d'un trajet
     */
    suspend fun updateStartKm(tripId: Long, newStartKm: Int): Result<Unit> = Result.runCatchingSuspend {
        tripDao.updateStartKm(tripId, newStartKm)
    }

    /**
     * Met à jour le kilométrage d'arrivée d'un trajet
     */
    suspend fun updateEndKm(tripId: Long, newEndKm: Int): Result<Unit> = Result.runCatchingSuspend {
        tripDao.updateEndKm(tripId, newEndKm)
    }

    /**
     * Met à jour l'heure de départ d'un trajet
     */
    suspend fun updateStartTime(tripId: Long, newStartTime: Long): Result<Unit> = Result.runCatchingSuspend {
        tripDao.updateStartTime(tripId, newStartTime)
    }

    /**
     * Met à jour la date d'un trajet
     */
    suspend fun updateDate(tripId: Long, newDate: String): Result<Unit> = Result.runCatchingSuspend {
        tripDao.updateDate(tripId, newDate)
    }

    /**
     * Met à jour les conditions météo d'un trajet
     */
    suspend fun updateConditions(tripId: Long, newConditions: String): Result<Unit> = Result.runCatchingSuspend {
        tripDao.updateConditions(tripId, newConditions)
    }
}
