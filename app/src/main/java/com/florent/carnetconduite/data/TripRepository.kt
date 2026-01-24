package com.florent.carnetconduite.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val context: Context
) {
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    val activeTrip: Flow<Trip?> = tripDao.getActiveTrip()
    val ongoingSessionId: Flow<Long?> = SessionPreferences.getOngoingSessionId(context)

    suspend fun insert(trip: Trip): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun update(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun delete(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    suspend fun getTripById(id: Long): Trip? {
        return tripDao.getTripById(id)
    }

    suspend fun getReadyReturnTrip(): Trip? {
        return tripDao.getReadyReturnTrip()
    }

    suspend fun finishTrip(tripId: Long, endKm: Int, endPlace: String, endTime: Long) {
        tripDao.finishTrip(tripId, endKm, endPlace, endTime)
        SessionPreferences.clearOngoingSessionId(context)
    }

    suspend fun finishAndPrepareReturn(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        endTime: Long
    ): Long {
        val returnId = tripDao.finishAndPrepareReturn(tripId, endKm, endPlace, endTime)
        SessionPreferences.saveOngoingSessionId(context, returnId)
        return returnId
    }

    suspend fun createSkippedReturn(tripId: Long): Long {
        val skippedId = tripDao.createSkippedReturn(tripId)
        SessionPreferences.clearOngoingSessionId(context)
        return skippedId
    }

    suspend fun startReturn(returnTripId: Long, actualStartKm: Int?, startTime: Long) {
        tripDao.startReturnTrip(returnTripId, actualStartKm, startTime)
        SessionPreferences.saveOngoingSessionId(context, returnTripId)
    }

    suspend fun cancelReturn(returnTripId: Long) {
        tripDao.cancelTrip(returnTripId)
        SessionPreferences.clearOngoingSessionId(context)
    }

    suspend fun saveOngoingSessionId(sessionId: Long) {
        SessionPreferences.saveOngoingSessionId(context, sessionId)
    }

    suspend fun clearOngoingSessionId() {
        SessionPreferences.clearOngoingSessionId(context)
    }

    suspend fun updateEndTime(tripId: Long, newEndTime: Long) {
        tripDao.updateEndTime(tripId, newEndTime)
    }

    suspend fun updateStartKm(tripId: Long, newStartKm: Int) {
        tripDao.updateStartKm(tripId, newStartKm)
    }

    suspend fun updateEndKm(tripId: Long, newEndKm: Int) {
        tripDao.updateEndKm(tripId, newEndKm)
    }

    suspend fun updateStartTime(tripId: Long, newStartTime: Long) {
        tripDao.updateStartTime(tripId, newStartTime)
    }
}