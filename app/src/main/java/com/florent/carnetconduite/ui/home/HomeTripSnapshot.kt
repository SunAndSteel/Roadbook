package com.florent.carnetconduite.ui.home

import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.ui.home.mapper.findTripForDrivingState

data class HomeTripSnapshot(
    val outward: Trip?,
    val arrived: Trip?,
    val returnReady: Trip?,
    val returnActive: Trip?
) {
    fun headerTrip(state: DrivingState): Trip? = when (state) {
        DrivingState.OUTWARD_ACTIVE -> outward
        DrivingState.ARRIVED -> arrived
        DrivingState.RETURN_READY -> returnReady
        DrivingState.RETURN_ACTIVE -> returnActive
        else -> null
    }

    companion object {
        fun fromTrips(trips: List<Trip>): HomeTripSnapshot = HomeTripSnapshot(
            outward = findTripForDrivingState(DrivingState.OUTWARD_ACTIVE, trips),
            arrived = findTripForDrivingState(DrivingState.ARRIVED, trips),
            returnReady = findTripForDrivingState(DrivingState.RETURN_READY, trips),
            returnActive = findTripForDrivingState(DrivingState.RETURN_ACTIVE, trips)
        )
    }
}
