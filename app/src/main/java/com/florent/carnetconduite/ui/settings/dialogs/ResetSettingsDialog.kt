package com.florent.carnetconduite.ui.settings.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Dialogue de confirmation de réinitialisation.
 *
 * PATTERN : Dialogue de confirmation
 * Pour les actions destructives, toujours demander confirmation.
 */
@Composable
internal fun ResetSettingsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null) },
        title = { Text("Réinitialiser les paramètres ?") },
        text = { Text("Tous vos paramètres seront restaurés à leurs valeurs par défaut.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Réinitialiser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
