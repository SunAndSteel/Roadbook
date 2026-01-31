package com.florent.carnetconduite.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import com.florent.carnetconduite.util.formatTime

enum class TripSummaryVariant { Minimal, Sticky }

/**
 * Un seul champ éditable à la fois (selon l’état).
 */
data class TripSummaryEdit(
    val label: String,          // ex: "Heure d’arrivée"
    val value: String,          // ex: "14:25"
    val onEdit: () -> Unit
)

/**
 * Version sobre (proche du design “résumé” classique), mais avec polish Material 3 expressive.
 * - Hiérarchie typographique claire
 * - Statut visible
 * - Un seul champ éditable (pencil)
 */
@Composable
fun TripSummaryHeader(
    trip: Trip,
    variant: TripSummaryVariant,
    statusLabel: String,
    statusColor: Color,
    showDistance: Boolean,
    modifier: Modifier = Modifier,
    edit: TripSummaryEdit? = null
) {
    val startTimeText = if (trip.startTime > 0L) formatTime(trip.startTime) else "—"
    val endTimeText = if ((trip.endTime ?: 0L) > 0L) formatTime(trip.endTime!!) else "—"

    val distanceText = run {
        val endKm = trip.endKm ?: 0
        val dist = endKm - trip.startKm
        if (showDistance && endKm > 0 && dist >= 0) "$dist km" else "—"
    }

    val container = when (variant) {
        TripSummaryVariant.Minimal -> MaterialTheme.colorScheme.surface
        TripSummaryVariant.Sticky -> MaterialTheme.colorScheme.surface // reste clean en bottom bar
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = container),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Ligne 1 : "Résumé" + status badge (expressive mais discret)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Résumé",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = statusColor.copy(alpha = 0.14f),
                    contentColor = statusColor,
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Ligne 2 : infos de base (non éditables)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoPill(
                    icon = Icons.Rounded.Schedule,
                    label = "Départ",
                    value = startTimeText,
                    modifier = Modifier.weight(1f)
                )
                InfoPill(
                    icon = Icons.Rounded.Schedule,
                    label = "Arrivée",
                    value = endTimeText,
                    modifier = Modifier.weight(1f)
                )
                if (showDistance) {
                    InfoPill(
                        icon = Icons.Rounded.Route,
                        label = "Distance",
                        value = distanceText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Ligne 3 : le seul champ éditable (si fourni)
            edit?.let { e ->
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = e.label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.alpha(0.85f)
                            )
                            Text(
                                text = e.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(onClick = e.onEdit) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Modifier")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.alpha(0.8f))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.alpha(0.8f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
