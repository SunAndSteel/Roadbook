package com.florent.carnetconduite.domain.models

/**
 * Statut d'un trajet dans le système.
 * Remplace les magic strings "ACTIVE", "COMPLETED", etc.
 */
enum class TripStatus {
    /**
     * Trajet en cours d'exécution (endKm == null)
     */
    ACTIVE,

    /**
     * Trajet terminé avec succès (endKm != null)
     */
    COMPLETED,

    /**
     * Trajet retour préparé mais pas encore démarré
     */
    READY,

    /**
     * Trajet retour volontairement ignoré (trajet simple)
     */
    SKIPPED,

    /**
     * Trajet annulé
     */
    CANCELLED;

    companion object {
        /**
         * Convertit une string en TripStatus de manière sécurisée
         * @throws IllegalArgumentException si la valeur est invalide
         */
        fun fromString(value: String): TripStatus = valueOf(value.uppercase())

        /**
         * Convertit une string en TripStatus avec fallback
         */
        fun fromStringOrNull(value: String): TripStatus? = try {
            valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Vérifie si le trajet est dans un état terminal (complété ou ignoré)
     */
    fun isTerminal(): Boolean = this == COMPLETED || this == SKIPPED || this == CANCELLED

    /**
     * Vérifie si le trajet est actif (en cours)
     */
    fun isActive(): Boolean = this == ACTIVE
}