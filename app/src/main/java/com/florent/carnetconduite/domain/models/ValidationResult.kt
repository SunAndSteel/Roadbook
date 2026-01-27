package com.florent.carnetconduite.domain.models

/**
 * Résultat d'une validation de données métier
 */
sealed class ValidationResult {
    /**
     * Validation réussie
     */
    object Valid : ValidationResult()

    /**
     * Validation échouée avec un message d'erreur
     */
    data class Invalid(val message: String) : ValidationResult()

    /**
     * Vérifie si la validation est réussie
     */
    fun isValid(): Boolean = this is Valid

    /**
     * Vérifie si la validation a échoué
     */
    fun isInvalid(): Boolean = this is Invalid

    /**
     * Récupère le message d'erreur si invalide, null sinon
     */
    fun getErrorMessage(): String? = when (this) {
        is Invalid -> message
        is Valid -> null
    }

    companion object {
        /**
         * Crée un résultat valide
         */
        fun valid(): ValidationResult = Valid

        /**
         * Crée un résultat invalide
         */
        fun invalid(message: String): ValidationResult = Invalid(message)
    }
}

/**
 * Extensions pour faciliter l'usage
 */

/**
 * Exécute une action si la validation est valide
 */
inline fun ValidationResult.onValid(action: () -> Unit): ValidationResult {
    if (this is ValidationResult.Valid) action()
    return this
}

/**
 * Exécute une action si la validation est invalide
 */
inline fun ValidationResult.onInvalid(action: (String) -> Unit): ValidationResult {
    if (this is ValidationResult.Invalid) action(message)
    return this
}

/**
 * Combine plusieurs validations
 * Retourne Invalid dès la première erreur, Valid si toutes sont valides
 */
fun List<ValidationResult>.combine(): ValidationResult {
    return firstOrNull { it.isInvalid() } ?: ValidationResult.valid()
}