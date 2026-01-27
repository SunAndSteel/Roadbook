package com.florent.carnetconduite.domain.validators

import com.florent.carnetconduite.domain.models.ValidationResult

/**
 * Validator centralisé pour toutes les règles métier de validation des trajets.
 * Remplace la validation disséminée dans le ViewModel.
 */
object TripValidator {

    // Constantes de validation
    private const val MIN_KM = 0
    private const val MAX_KM = 999_999
    private const val MIN_PLACE_LENGTH = 2
    private const val MAX_PLACE_LENGTH = 100

    /**
     * Valide un kilométrage de départ
     */
    fun validateStartKm(km: Int): ValidationResult {
        return when {
            km < MIN_KM -> ValidationResult.invalid(
                "Le kilométrage doit être positif (minimum $MIN_KM km)"
            )
            km > MAX_KM -> ValidationResult.invalid(
                "Le kilométrage semble incorrect (maximum $MAX_KM km)"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Valide un kilométrage d'arrivée par rapport au départ
     */
    fun validateEndKm(startKm: Int, endKm: Int): ValidationResult {
        return when {
            endKm < MIN_KM -> ValidationResult.invalid(
                "Le kilométrage doit être positif"
            )
            endKm > MAX_KM -> ValidationResult.invalid(
                "Le kilométrage semble incorrect"
            )
            endKm < startKm -> ValidationResult.invalid(
                "Le kilométrage d'arrivée ($endKm km) ne peut pas être inférieur au départ ($startKm km)"
            )
            endKm == startKm -> ValidationResult.invalid(
                "Le kilométrage d'arrivée doit être différent du départ"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Valide un nom de lieu
     */
    fun validatePlace(place: String): ValidationResult {
        val trimmed = place.trim()
        return when {
            trimmed.isBlank() -> ValidationResult.invalid(
                "Le lieu ne peut pas être vide"
            )
            trimmed.length < MIN_PLACE_LENGTH -> ValidationResult.invalid(
                "Le lieu doit contenir au moins $MIN_PLACE_LENGTH caractères"
            )
            trimmed.length > MAX_PLACE_LENGTH -> ValidationResult.invalid(
                "Le lieu est trop long (maximum $MAX_PLACE_LENGTH caractères)"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Valide un guide (numéro 1-9)
     */
    fun validateGuide(guide: String): ValidationResult {
        return when {
            guide.isBlank() -> ValidationResult.invalid(
                "Le guide doit être spécifié"
            )
            guide.toIntOrNull() == null -> ValidationResult.invalid(
                "Le guide doit être un numéro valide"
            )
            guide.toInt() !in 1..9 -> ValidationResult.invalid(
                "Le guide doit être entre 1 et 9"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Valide les conditions météo (optionnel)
     */
    fun validateConditions(conditions: String): ValidationResult {
        // Les conditions sont optionnelles, donc toujours valide
        // On pourrait ajouter une limite de longueur si nécessaire
        return if (conditions.length > 200) {
            ValidationResult.invalid("Les conditions sont trop longues (maximum 200 caractères)")
        } else {
            ValidationResult.valid()
        }
    }

    /**
     * Valide un timestamp (doit être dans le passé ou présent, pas dans le futur)
     */
    fun validateTimestamp(timestamp: Long): ValidationResult {
        val now = System.currentTimeMillis()
        return when {
            timestamp > now -> ValidationResult.invalid(
                "L'heure ne peut pas être dans le futur"
            )
            timestamp < 0 -> ValidationResult.invalid(
                "Timestamp invalide"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Valide une plage de temps (start < end)
     */
    fun validateTimeRange(startTime: Long, endTime: Long): ValidationResult {
        return when {
            endTime <= startTime -> ValidationResult.invalid(
                "L'heure d'arrivée doit être après l'heure de départ"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Valide toutes les données de démarrage d'un trajet aller
     */
    fun validateOutwardStart(
        startKm: Int,
        startPlace: String,
        conditions: String,
        guide: String
    ): ValidationResult {
        return listOf(
            validateStartKm(startKm),
            validatePlace(startPlace),
            validateConditions(conditions),
            validateGuide(guide)
        ).combine()
    }

    /**
     * Valide toutes les données de fin d'un trajet
     */
    fun validateTripEnd(
        startKm: Int,
        endKm: Int,
        endPlace: String
    ): ValidationResult {
        return listOf(
            validateEndKm(startKm, endKm),
            validatePlace(endPlace)
        ).combine()
    }
}

/**
 * Extension pour combiner plusieurs ValidationResult
 */
private fun List<ValidationResult>.combine(): ValidationResult {
    return firstOrNull { it.isInvalid() } ?: ValidationResult.valid()
}