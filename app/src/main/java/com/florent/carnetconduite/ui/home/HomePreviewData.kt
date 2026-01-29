package com.florent.carnetconduite.ui.home

import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripGroup
import com.florent.carnetconduite.domain.models.TripStatus

internal const val PreviewStartTime = 1704445200000L
internal const val PreviewEndTime = PreviewStartTime + 45 * 60 * 1000L

internal val previewOutwardActiveTrip = Trip(
    id = 1L,
    startKm = 12500,
    startPlace = "Bordeaux - Centre",
    startTime = PreviewStartTime,
    isReturn = false,
    status = TripStatus.ACTIVE,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

internal val previewArrivedTrip = Trip(
    id = 2L,
    startKm = 12500,
    endKm = 12620,
    startPlace = "Bordeaux - Centre",
    endPlace = "Pessac",
    startTime = PreviewStartTime,
    endTime = PreviewEndTime,
    isReturn = false,
    pairedTripId = 2L,
    status = TripStatus.COMPLETED,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

internal val previewReturnReadyTrip = Trip(
    id = 3L,
    startKm = 12620,
    startPlace = "Pessac",
    startTime = PreviewEndTime + 10 * 60 * 1000L,
    isReturn = true,
    pairedTripId = 2L,
    status = TripStatus.READY,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

internal val previewReturnActiveTrip = Trip(
    id = 4L,
    startKm = 12620,
    startPlace = "Pessac",
    startTime = PreviewEndTime + 10 * 60 * 1000L,
    isReturn = true,
    pairedTripId = 2L,
    status = TripStatus.ACTIVE,
    conditions = "Soleil",
    guide = "1",
    date = "2024-01-05"
)

internal val previewTripGroups = listOf(
    TripGroup(
        outward = previewArrivedTrip,
        returnTrip = previewReturnActiveTrip.copy(
            id = 5L,
            endKm = 12810,
            endPlace = "Bordeaux - Centre",
            endTime = PreviewEndTime + 70 * 60 * 1000L,
            status = TripStatus.COMPLETED
        ),
        seanceNumber = 12
    ),
    TripGroup(
        outward = previewArrivedTrip.copy(
            id = 6L,
            startTime = PreviewStartTime - 3 * 24 * 60 * 60 * 1000L,
            endTime = PreviewStartTime - 3 * 24 * 60 * 60 * 1000L + 35 * 60 * 1000L,
            startKm = 12000,
            endKm = 12110,
            startPlace = "Mérignac",
            endPlace = "Talence",
            status = TripStatus.COMPLETED
        ),
        returnTrip = null,
        seanceNumber = 11
    )
)
