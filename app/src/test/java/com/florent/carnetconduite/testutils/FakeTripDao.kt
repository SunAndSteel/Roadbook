package com.florent.carnetconduite.testutils

import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.data.TripDao
import com.florent.carnetconduite.domain.models.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTripDao(initialTrips: List<Trip> = emptyList()) : TripDao {
    private val trips = mutableListOf<Trip>().apply { addAll(initialTrips) }
    private val tripsFlow = MutableStateFlow(trips.toList())

    override fun getAllTrips(): Flow<List<Trip>> = tripsFlow

    override suspend fun getTripById(tripId: Long): Trip? = trips.find { it.id == tripId }

    override fun getActiveTrip(): Flow<Trip?> = tripsFlow.map { list ->
        list.firstOrNull { it.endKm == null }
    }

    override suspend fun getReadyReturnTrip(): Trip? =
        trips.findLast { it.status == TripStatus.READY && it.isReturn }

    override suspend fun insertTrip(trip: Trip): Long {
        val nextId = if (trip.id == 0L) (trips.maxOfOrNull { it.id } ?: 0L) + 1 else trip.id
        val stored = trip.copy(id = nextId)
        trips.add(stored)
        emitTrips()
        return nextId
    }

    override suspend fun updateTrip(trip: Trip) {
        val index = trips.indexOfFirst { it.id == trip.id }
        if (index >= 0) {
            trips[index] = trip
            emitTrips()
        }
    }

    override suspend fun deleteTrip(trip: Trip) {
        trips.removeIf { it.id == trip.id }
        emitTrips()
    }

    override suspend fun deleteAllTrips() {
        trips.clear()
        emitTrips()
    }

    override suspend fun finishTrip(tripId: Long, endKm: Int, endPlace: String, endTime: Long) {
        val trip = getTripById(tripId) ?: return
        updateTrip(
            trip.copy(
                endKm = endKm,
                endPlace = endPlace,
                endTime = endTime,
                status = TripStatus.COMPLETED
            )
        )
    }

    override suspend fun startTrip(tripId: Long, startTime: Long, startKm: Int) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(startTime = startTime, startKm = startKm, status = TripStatus.ACTIVE))
    }

    override suspend fun cancelTrip(tripId: Long) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(status = TripStatus.CANCELLED))
    }

    override suspend fun updateEndTime(tripId: Long, newEndTime: Long) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(endTime = newEndTime))
    }

    override suspend fun updateStartKm(tripId: Long, newStartKm: Int) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(startKm = newStartKm))
    }

    override suspend fun updateEndKm(tripId: Long, newEndKm: Int) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(endKm = newEndKm))
    }

    override suspend fun updateStartTime(tripId: Long, newStartTime: Long) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(startTime = newStartTime))
    }

    override suspend fun updateDate(tripId: Long, newDate: String) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(date = newDate))
    }

    override suspend fun updateConditions(tripId: Long, newConditions: String) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(conditions = newConditions))
    }

    override suspend fun markOutwardAsSimple(tripId: Long) {
        val trip = getTripById(tripId) ?: return
        updateTrip(trip.copy(pairedTripId = tripId))
    }

    override suspend fun finishAndPrepareReturn(
        tripId: Long,
        endKm: Int,
        endPlace: String,
        endTime: Long
    ): Long {
        val trip = getTripById(tripId) ?: throw IllegalStateException("Trip not found")
        finishTrip(tripId, endKm, endPlace, endTime)
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

    override suspend fun createSkippedReturn(outwardTripId: Long): Long {
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

    override suspend fun startReturn(returnTripId: Long, actualStartKm: Int?, startTime: Long) {
        val trip = getTripById(returnTripId) ?: throw IllegalStateException("Return trip not found")
        val startKm = actualStartKm ?: trip.startKm
        startTrip(returnTripId, startTime, startKm)
    }

    private fun emitTrips() {
        tripsFlow.value = trips.toList()
    }
}
