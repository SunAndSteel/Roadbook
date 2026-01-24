package com.florent.carnetconduite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val startKm: Int,
    val endKm: Int? = null,
    val startPlace: String,
    val endPlace: String? = null,
    val startTime: Long,       // epoch ms
    val endTime: Long? = null, // epoch ms
    val isReturn: Boolean = false,
    val pairedTripId: Long? = null,
    val status: String = "ACTIVE", // ACTIVE / READY / COMPLETED / CANCELLED
    val conditions: String = "",
    val guide: String = "1",
    val date: String = "" // ISO-8601 format
) {
    // Propriétés calculées
    val nbKmsParcours: Int
        get() = if (endKm != null && endKm >= startKm) endKm - startKm else 0

    val kmsComptabilises: Int
        get() = nbKmsParcours
}