package com.florent.carnetconduite.data

import androidx.room.*
import com.florent.carnetconduite.domain.models.TripStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations sur les trajets.
 * Migré pour utiliser TripStatus enum.
 */
@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): Trip?

    @Query("SELECT * FROM trips WHERE endKm IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveTrip(): Flow<Trip?>

    /**
     * Récupère le trajet retour prêt (utilise TripStatus enum)
     */
    @Query("SELECT * FROM trips WHERE status = 'READY' AND isReturn = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getReadyReturnTrip(): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()

    /**
     * Termine un trajet (utilise TripStatus.COMPLETED)
     */
    @Query("UPDATE trips SET endKm = :endKm, endPlace = :endPlace, endTime = :endTime, status = 'COMPLETED' WHERE id = :tripId")
    suspend fun finishTrip(tripId: Long, endKm: Int, endPlace: String, endTime: Long)

    /**
     * Démarre un trajet (utilise TripStatus.ACTIVE)
     */
    @Query("UPDATE trips SET startTime = :startTime, startKm = :startKm, status = 'ACTIVE' WHERE id = :tripId")
    suspend fun startTrip(tripId: Long, startTime: Long, startKm: Int)

    /**
     * Annule un trajet (utilise TripStatus.CANCELLED)
     */
    @Query("UPDATE trips SET status = 'CANCELLED' WHERE id = :tripId")
    suspend fun cancelTrip(tripId: Long)

    @Query("UPDATE trips SET endTime = :newEndTime WHERE id = :tripId")
    suspend fun updateEndTime(tripId: Long, newEndTime: Long)

    @Query("UPDATE trips SET startKm = :newStartKm WHERE id = :tripId")
    suspend fun updateStartKm(tripId: Long, newStartKm: Int)

    @Query("UPDATE trips SET endKm = :newEndKm WHERE id = :tripId")
    suspend fun updateEndKm(tripId: Long, newEndKm: Int)

    @Query("UPDATE trips SET startTime = :newStartTime WHERE id = :tripId")
    suspend fun updateStartTime(tripId: Long, newStartTime: Long)

    @Query("UPDATE trips SET date = :newDate WHERE id = :tripId")
    suspend fun updateDate(tripId: Long, newDate: String)

    @Query("UPDATE trips SET conditions = :newConditions WHERE id = :tripId")
    suspend fun updateConditions(tripId: Long, newConditions: String)

    /**
     * Marque un trajet aller comme "simple" sans créer de retour.
     * On réutilise pairedTripId pour indiquer une décision explicite.
     */
    @Query("UPDATE trips SET pairedTripId = :tripId WHERE id = :tripId AND isReturn = 0")
    suspend fun markOutwardAsSimple(tripId: Long)

    /**
     * Transaction : finir l'aller et préparer le retour
     */
    @Transaction
    suspend fun finishAndPrepareReturn(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        endTime: Long
    ): Long {
        val trip = getTripById(tripId) ?: throw IllegalStateException("Trip not found")

        // Finir le trajet aller
        finishTrip(tripId, endKm, endPlace, endTime)

        // Créer le trajet retour en statut READY
        val returnTrip = Trip(
            startKm = endKm,
            startPlace = endPlace,
            endPlace = trip.startPlace,
            startTime = 0L,
            isReturn = true,
            pairedTripId = tripId,
            status = TripStatus.READY,
            conditions = trip.conditions,
            guide = trip.guide,
            date = trip.date
        )

        return insertTrip(returnTrip)
    }

    /**
     * Crée un trajet retour SKIPPED (trajet simple, pas de retour)
     */
    @Transaction
    suspend fun createSkippedReturn(outwardTripId: Long): Long {
        val trip = getTripById(outwardTripId) ?: throw IllegalStateException("Trip not found")

        val skippedReturn = Trip(
            startKm = trip.endKm ?: trip.startKm,
            startPlace = trip.endPlace ?: "",
            endPlace = trip.startPlace,
            startTime = 0L,
            endTime = 0L,
            isReturn = true,
            pairedTripId = outwardTripId,
            status = TripStatus.SKIPPED,
            conditions = trip.conditions,
            guide = trip.guide,
            date = trip.date
        )

        return insertTrip(skippedReturn)
    }

    /**
     * Démarre un trajet retour préparé
     */
    @Transaction
    suspend fun startReturn(returnTripId: Long, actualStartKm: Int?, startTime: Long) {
        val trip = getTripById(returnTripId) ?: throw IllegalStateException("Return trip not found")

        if (actualStartKm != null && actualStartKm != trip.startKm) {
            // Mettre à jour le km de départ si différent
            startTrip(returnTripId, startTime, actualStartKm)
        } else {
            startTrip(returnTripId, startTime, trip.startKm)
        }
    }
}
