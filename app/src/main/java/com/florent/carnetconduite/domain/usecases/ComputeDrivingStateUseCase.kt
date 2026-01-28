package com.florent.carnetconduite.domain.usecases

import com.florent.carnetconduite.data.DrivingState
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.domain.models.TripStatus

/**
 * Use Case responsable du calcul de l'état de conduite
 * basé sur la liste des trajets.
 *
 * Cette logique était auparavant dans DrivingViewModel.computeDrivingState()
 */
class ComputeDrivingStateUseCase {

    /**
     * Calcule l'état de conduite actuel basé sur les trajets
     */
    operator fun invoke(trips: List<Trip>): DrivingState {
        // Priorité 1 : Retour actif
        if (trips.any { it.endKm == null && it.isReturn }) {
            return DrivingState.RETURN_ACTIVE
        }

        // Priorité 2 : Aller actif
        if (trips.any { it.endKm == null && !it.isReturn }) {
            return DrivingState.OUTWARD_ACTIVE
        }

        // Priorité 3 : Retour prêt
        if (trips.any { it.isReturn && it.status == TripStatus.READY }) {
            return DrivingState.RETURN_READY
        }

        // Priorité 4 : Arrivé (dernier aller complété sans retour)
        val arrivedTrip = findArrivedTrip(trips)
        if (arrivedTrip != null) {
            return DrivingState.ARRIVED
        }

        // Priorité 5 : IDLE ou COMPLETED
        return if (trips.isEmpty() || trips.all {
                it.status == TripStatus.COMPLETED ||
                        it.status == TripStatus.CANCELLED ||
                        it.status == TripStatus.SKIPPED
            }) {
            DrivingState.IDLE
        } else {
            DrivingState.COMPLETED
        }
    }

    /**
     * Trouve le trajet "arrivé" (aller terminé sans retour associé)
     */
    private fun findArrivedTrip(trips: List<Trip>): Trip? {
        // Trouver le dernier trajet aller terminé qui n'a pas de retour associé
        val latestOutward = trips
            .filter { !it.isReturn && it.status == TripStatus.COMPLETED }
            .maxByOrNull { it.id }

        if (latestOutward != null) {
            // Vérifier si un retour existe
            val hasReturn = trips.any {
                it.pairedTripId == latestOutward.id && it.isReturn
            }

            // Si pas de retour, c'est un trajet "arrivé" en attente de décision
            if (!hasReturn) {
                return latestOutward
            }
        }

        return null
    }
}