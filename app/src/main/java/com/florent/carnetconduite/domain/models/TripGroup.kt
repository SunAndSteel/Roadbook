package com.florent.carnetconduite.domain.models

import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus

/**
 * Domain model qui groupe un trajet aller et son retour éventuel
 */
data class TripGroup(
    val outward: Trip,
    val returnTrip: Trip? = null,
    val seanceNumber: Int
) {
    val totalKms: Int = outward.kmsComptabilises + (returnTrip?.kmsComptabilises ?: 0)

    val hasReturn: Boolean = returnTrip != null && returnTrip.status != TripStatus.SKIPPED

    val isComplete: Boolean = outward.status == TripStatus.COMPLETED &&
            (returnTrip == null || returnTrip.status == TripStatus.COMPLETED || returnTrip.status == TripStatus.SKIPPED)
}

/**
 * Groupe les trajets par paire aller-retour et les numérote correctement
 */
fun groupTrips(trips: List<Trip>): List<TripGroup> {
    // Filtrer seulement les trajets terminés ou skipped
    val completedTrips = trips.filter {
        it.status == TripStatus.COMPLETED || it.status == TripStatus.SKIPPED
    }

    // Trier par date de début
    val sorted = completedTrips.sortedBy { it.startTime }

    val grouped = mutableListOf<TripGroup>()
    val processed = mutableSetOf<Long>()

    sorted.forEach { trip ->
        // Traiter seulement les trajets aller non encore traités
        if (trip.id !in processed && !trip.isReturn) {
            // Chercher le retour associé
            val returnTrip = sorted.find {
                it.pairedTripId == trip.id && it.isReturn
            }

            grouped.add(
                TripGroup(
                    outward = trip,
                    returnTrip = returnTrip,
                    seanceNumber = grouped.size + 1
                )
            )

            // Marquer comme traités
            processed.add(trip.id)
            returnTrip?.let { processed.add(it.id) }
        }
    }

    // Retourner en ordre inverse (plus récent en premier)
    return grouped.reversed()
}