package com.florent.carnetconduite.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.florent.carnetconduite.ui.preview.DevicePreview
import com.florent.carnetconduite.ui.preview.RoadbookTheme

@DevicePreview
@Composable
private fun SettingsScreenPreview() {
    RoadbookTheme {
        SettingsScreen(
            settings = UserSettings(
                themeMode = ThemeMode.DARK,
                defaultGuide = "2",
                showDeleteConfirmations = true
            ),
            onThemeModeChange = {},
            onDefaultGuideChange = {},
            onShowDeleteConfirmationsChange = {},
            onResetToDefaults = {},
            onNavigateBack = {}
        )
    }
}

@DevicePreview
@Composable
private fun SettingsItemPreview() {
    RoadbookTheme {
        SettingsItem(
            icon = Icons.Default.Palette,
            title = "Thème",
            subtitle = "Sombre",
            onClick = {}
        )
    }
}

@DevicePreview
@Composable
private fun SettingsSwitchItemPreview() {
    RoadbookTheme {
        SettingsSwitchItem(
            icon = Icons.Default.Delete,
            title = "Confirmer les suppressions",
            subtitle = "Demander confirmation avant de supprimer un trajet",
            checked = true,
            onCheckedChange = {}
        )
    }
}

@DevicePreview
@Composable
private fun SettingsSectionHeaderPreview() {
    RoadbookTheme {
        SettingsSectionHeader("Apparence")
    }
}

@DevicePreview
@Composable
private fun ThemeSelectionDialogPreview() {
    RoadbookTheme {
        ThemeSelectionDialog(
            currentTheme = ThemeMode.DARK,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}

@DevicePreview
@Composable
private fun GuideSelectionDialogPreview() {
    RoadbookTheme {
        GuideSelectionDialog(
            currentGuide = "2",
            onGuideSelected = {},
            onDismiss = {}
        )
    }
}

@DevicePreview
@Composable
private fun ResetSettingsDialogPreview() {
    RoadbookTheme {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Réinitialiser les paramètres ?") },
            text = { Text("Tous vos paramètres seront restaurés à leurs valeurs par défaut.") },
            confirmButton = {
                TextButton(onClick = {}) {
                    Text("Réinitialiser")
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text("Annuler")
                }
            }
        )
    }
}
