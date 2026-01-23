package com.florent.carnetconduite.data

import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    suspend fun insert(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun update(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun delete(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    suspend fun getTripById(id: String): Trip? {
        return tripDao.getTripById(id)
    }
}