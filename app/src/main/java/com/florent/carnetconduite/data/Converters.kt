package com.florent.carnetconduite.data

import androidx.room.TypeConverter
import com.florent.carnetconduite.domain.models.TripStatus

/**
 * Type converter pour Room afin de stocker TripStatus enum en base de données.
 * Room stocke les enums comme des strings.
 */
class Converters {

    /**
     * Convertit TripStatus en String pour stockage Room
     */
    @TypeConverter
    fun fromTripStatus(status: TripStatus): String {
        return status.name
    }

    /**
     * Convertit String en TripStatus lors de la lecture Room
     */
    @TypeConverter
    fun toTripStatus(value: String): TripStatus {
        return try {
            TripStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Fallback si valeur inconnue
            TripStatus.ACTIVE
        }
    }
}