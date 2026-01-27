package com.florent.carnetconduite.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.util.formatTime

@Composable
fun TripDetails(trip: Trip, label: String?) {
    Column {
        label?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        Text(
            "${trip.startPlace} → ${trip.endPlace}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "${formatTime(trip.startTime)} - ${trip.endTime?.let { formatTime(it) } ?: ""}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            "Km: ${trip.startKm} → ${trip.endKm} (${trip.nbKmsParcours} km)",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}