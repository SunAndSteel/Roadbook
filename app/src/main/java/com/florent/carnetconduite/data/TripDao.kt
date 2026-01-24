package com.florent.carnetconduite.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): Trip?

    @Query("SELECT * FROM trips WHERE endKm IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveTrip(): Flow<Trip?>

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

    @Query("UPDATE trips SET endKm = :endKm, endPlace = :endPlace, endTime = :endTime, status = 'COMPLETED' WHERE id = :tripId")
    suspend fun finishTrip(tripId: Long, endKm: Int, endPlace: String, endTime: Long)

    @Query("UPDATE trips SET startTime = :startTime, startKm = :startKm, status = 'ACTIVE' WHERE id = :tripId")
    suspend fun startTrip(tripId: Long, startTime: Long, startKm: Int)

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

    // Transaction : finir l'aller et préparer le retour
    @Transaction
    suspend fun finishAndPrepareReturn(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        endTime: Long
    ): Long {
        val trip = getTripById(tripId) ?: throw IllegalStateException("Trip not found")

        // Finir l'aller
        finishTrip(tripId, endKm, endPlace, endTime)

        // Créer le retour en mode READY
        val returnTrip = Trip(
            startKm = endKm,
            startPlace = endPlace,
            endPlace = trip.startPlace, // Pré-remplir avec le départ de l'aller
            startTime = 0L, // Pas encore démarré
            isReturn = true,
            pairedTripId = tripId,
            status = "READY",
            guide = trip.guide,
            date = trip.date
        )

        return insertTrip(returnTrip)
    }

    // Transaction : créer un retour "skipped" pour trajet simple
    @Transaction
    suspend fun createSkippedReturn(tripId: Long): Long {
        val trip = getTripById(tripId) ?: throw IllegalStateException("Trip not found")

        val skippedReturn = Trip(
            startKm = trip.endKm ?: trip.startKm,
            startPlace = trip.endPlace ?: "",
            endPlace = trip.startPlace,
            startTime = trip.endTime ?: System.currentTimeMillis(),
            endTime = trip.endTime ?: System.currentTimeMillis(),
            isReturn = true,
            pairedTripId = tripId,
            status = "SKIPPED",
            guide = trip.guide,
            date = trip.date
        )

        return insertTrip(skippedReturn)
    }

    @Transaction
    suspend fun startReturnTrip(returnTripId: Long, actualStartKm: Int?, startTime: Long) {
        val trip = getTripById(returnTripId) ?: throw IllegalStateException("Return trip not found")
        val newStartKm = actualStartKm ?: trip.startKm
        startTrip(returnTripId, startTime, newStartKm)
    }
}