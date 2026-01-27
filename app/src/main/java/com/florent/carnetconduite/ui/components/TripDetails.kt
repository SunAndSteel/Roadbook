package com.florent.carnetconduite.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.data.Trip
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TripDetails(
    trip: Trip,
    isReturn: Boolean = false,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isReturn)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête du trajet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isReturn)
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isReturn) Icons.Rounded.KeyboardReturn else Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = if (isReturn)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isReturn) "Trajet retour" else "Trajet aller",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isReturn)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = "${trip.startPlace} → ${trip.endPlace ?: "En cours..."}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isReturn)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.alpha(0.8f)
                    )
                }
            }

            Divider(
                color = if (isReturn)
                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )

            // Détails via ListItem
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Kilométrage
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Kilométrage",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "${trip.startKm} → ${trip.endKm ?: "..."}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = trip.endKm?.let {
                        {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${it - trip.startKm} km",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = if (isReturn)
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
                )

                // Horaire
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Horaire",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = {
                        Text(
                            text = formatTimeRange(trip.startTime, trip.endTime),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    trailingContent = trip.endTime?.let { endTime ->
                        {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = formatDuration(endTime - trip.startTime),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )

                // Conditions météo si présentes
                if (trip.conditions.isNotBlank()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isReturn)
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Conditions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = trip.conditions,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isReturn)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.CloudQueue,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isReturn)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }

                // Guide si renseigné et différent de "1"
                if (trip.guide.isNotBlank() && trip.guide != "1") {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isReturn)
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Accompagnateur",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Guide n°${trip.guide}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isReturn)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isReturn)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

private fun formatTimeRange(startTime: Long, endTime: Long?): String {
    val start = formatTime(startTime)
    val end = endTime?.let { formatTime(it) } ?: "En cours..."
    return "$start → $end"
}

private fun formatTime(timestamp: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "N/A"
    }
}

private fun formatDuration(milliseconds: Long): String {
    val hours = milliseconds / 3600000
    val minutes = (milliseconds % 3600000) / 60000

    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}min"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}min"
        else -> "< 1min"
    }
}