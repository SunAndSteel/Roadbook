package com.florent.carnetconduite.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.florent.carnetconduite.domain.models.TripStatus

/**
 * Entity représentant un trajet (aller ou retour).
 * Migré pour utiliser TripStatus enum au lieu de String.
 */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val startKm: Int,
    val endKm: Int? = null,

    val startPlace: String,
    val endPlace: String? = null,

    val startTime: Long,       // epoch ms
    val endTime: Long? = null, // epoch ms

    val isReturn: Boolean = false,
    val pairedTripId: Long? = null,

    /**
     * Statut du trajet - maintenant un enum type-safe
     * Room utilise Converters pour stocker comme String
     */
    val status: TripStatus = TripStatus.ACTIVE,

    val conditions: String = "",
    val guide: String = "1",
    val date: String = "" // ISO-8601 format
) {
    // Propriétés calculées
    val nbKmsParcours: Int
        get() = if (endKm != null && endKm >= startKm) endKm - startKm else 0

    val kmsComptabilises: Int
        get() = nbKmsParcours

    /**
     * Vérifie si le trajet est actif
     */
    val isActive: Boolean
        get() = status == TripStatus.ACTIVE

    /**
     * Vérifie si le trajet est complété
     */
    val isCompleted: Boolean
        get() = status == TripStatus.COMPLETED

    /**
     * Vérifie si le trajet est dans un état terminal
     */
    val isTerminal: Boolean
        get() = status == TripStatus.COMPLETED ||
                status == TripStatus.SKIPPED ||
                status == TripStatus.CANCELLED

    /**
     * Vérifie si le statut est SKIPPED
     */
    val isSkipped: Boolean
        get() = status == TripStatus.SKIPPED
}