package com.florent.carnetconduite.ui.settings.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.carnetconduite.ui.theme.ThemeMode

/**
 * Dialogue de sélection du thème.
 *
 * PATTERN : Dialogue avec options radio
 * Chaque option est cliquable entièrement (pas juste le bouton radio).
 */
@Composable
internal fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir le thème") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentTheme,
                            onClick = { onThemeSelected(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Clair"
                                    ThemeMode.DARK -> "Sombre"
                                    ThemeMode.DYNAMIC -> "Système"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Toujours en mode clair"
                                    ThemeMode.DARK -> "Toujours en mode sombre"
                                    ThemeMode.DYNAMIC -> "Suit les paramètres du système"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

/**
 * Dialogue de sélection du guide par défaut.
 */
@Composable
internal fun GuideSelectionDialog(
    currentGuide: String,
    onGuideSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guide par défaut") },
        text = {
            Column {
                listOf("1", "2").forEach { guide ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGuideSelected(guide) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = guide == currentGuide,
                            onClick = { onGuideSelected(guide) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guide $guide",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
