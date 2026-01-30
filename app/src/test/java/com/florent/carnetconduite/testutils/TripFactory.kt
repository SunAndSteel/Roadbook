package com.florent.carnetconduite.testutils

import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus

object TripFactory {
    fun create(
        id: Long = 1L,
        startKm: Int = 100,
        endKm: Int? = null,
        startPlace: String = "Paris",
        endPlace: String? = null,
        startTime: Long = 1_000L,
        endTime: Long? = null,
        isReturn: Boolean = false,
        pairedTripId: Long? = null,
        status: TripStatus = TripStatus.ACTIVE,
        conditions: String = "",
        guide: String = "1",
        date: String = "2024-01-01"
    ): Trip = Trip(
        id = id,
        startKm = startKm,
        endKm = endKm,
        startPlace = startPlace,
        endPlace = endPlace,
        startTime = startTime,
        endTime = endTime,
        isReturn = isReturn,
        pairedTripId = pairedTripId,
        status = status,
        conditions = conditions,
        guide = guide,
        date = date
    )
}
