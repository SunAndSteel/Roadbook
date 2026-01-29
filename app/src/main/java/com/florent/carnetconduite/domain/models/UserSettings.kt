package com.florent.carnetconduite.domain.models

import com.florent.carnetconduite.ui.theme.ThemeMode

/**
 * Modèle de domaine pour les préférences utilisateur
 */
data class UserSettings(
    /**
     * Mode du thème : Dynamic, Light, ou Dark
     */
    val themeMode: ThemeMode = ThemeMode.DYNAMIC,

    /**
     * Guide par défaut
     */
    val defaultGuide: String = "1",

    /**
     * Afficher les confirmations de suppression
     */
    val showDeleteConfirmations: Boolean = true,

    /**
     * Format de date
     */
    val dateFormat: String = "dd/MM/yyyy"
) {
    companion object {
        val DEFAULT = UserSettings()
    }
}