package com.florent.carnetconduite.domain.utils

/**
 * Wrapper pour les résultats d'opérations qui peuvent échouer.
 * Permet une gestion d'erreurs robuste et explicite.
 */
sealed class Result<out T> {
    /**
     * Opération réussie avec une valeur
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Opération échouée avec une erreur
     */
    data class Error(
        val exception: Exception,
        val message: String? = null
    ) : Result<Nothing>()

    /**
     * Opération en cours (optionnel, pour les flows)
     */
    object Loading : Result<Nothing>()

    /**
     * Vérifie si le résultat est un succès
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Vérifie si le résultat est une erreur
     */
    fun isError(): Boolean = this is Error

    /**
     * Récupère la donnée si succès, null sinon
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Récupère la donnée si succès, lance l'exception sinon
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    companion object {
        /**
         * Crée un Result.Success
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Crée un Result.Error
         */
        fun error(exception: Exception, message: String? = null): Result<Nothing> =
            Error(exception, message)

        /**
         * Exécute un bloc et wrappe le résultat
         */
        inline fun <T> runCatching(block: () -> T): Result<T> = try {
            success(block())
        } catch (e: Exception) {
            error(e)
        }

        /**
         * Exécute un bloc suspend et wrappe le résultat
         */
        suspend inline fun <T> runCatchingSuspend(crossinline block: suspend () -> T): Result<T> = try {
            success(block())
        } catch (e: Exception) {
            error(e)
        }
    }
}

/**
 * Extensions pour faciliter l'usage
 */

/**
 * Map la valeur si succès
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * FlatMap pour chaîner les opérations
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(data)
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Exécute une action si succès
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Exécute une action si erreur
 */
inline fun <T> Result<T>.onError(action: (Exception) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}
