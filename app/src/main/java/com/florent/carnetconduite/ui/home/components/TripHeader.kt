package com.florent.carnetconduite.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal data class TripHeaderData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val statusLabel: String
)

/**
 * Header compact “expressive” : gros statut visuel + texte serré, parfait en sticky bar.
 */
@Composable
internal fun TripHeaderCompact(
    header: TripHeaderData,
    statusColor: Color,
    containerColor: Color,
    onContainerColor: Color,
    showActiveIndicator: Boolean,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        color = containerColor,
        contentColor = onContainerColor,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = statusColor,
                modifier = Modifier
                    .height(44.dp)
                    .width(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = header.icon,
                        contentDescription = null,
                        tint = contentColorFor(statusColor),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = header.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = header.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.75f),
                    maxLines = 1
                )
            }

            AssistChip(
                onClick = {},
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showActiveIndicator) {
                            PulsingDot(
                                color = statusColor,
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(8.dp)
                            )
                        }
                        Text(
                            text = header.statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = statusColor.copy(alpha = 0.15f),
                    labelColor = statusColor
                )
            )

            trailing?.let { slot ->
                Box(contentAlignment = Alignment.Center) { slot() }
            }
        }
    }
}
