package com.florent.carnetconduite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String,
    val date: String = "",
    val guide: String = "1",
    val heureDebut: String = "",
    val heureFin: String = "",
    val kmDepart: Int = 0,     // Changé de String à Int
    val kmFin: Int = 0,        // Changé de String à Int
    val typeTrajet: String = "A",
    val depart: String = "",
    val arrivee: String = "",
    val conditions: String = "",
    val nbKmsParcours: Int = 0, // Changé de String à Int
    val status: String = "completed",
    val timestamp: Long = System.currentTimeMillis()
)