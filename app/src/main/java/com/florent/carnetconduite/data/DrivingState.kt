package com.florent.carnetconduite.data

enum class DrivingState {
    IDLE,            // Aucun Trip en cours
    OUTWARD_ACTIVE,  // Trajet aller en cours (endKm == null && isReturn == false)
    ARRIVED,         // Arrivé, décision à prendre (terminer ou préparer retour)
    RETURN_READY,    // Retour préparé mais pas encore démarré
    RETURN_ACTIVE,   // Trajet retour en cours (endKm == null && isReturn == true)
    COMPLETED        // Tout terminé
}