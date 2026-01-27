package com.florent.carnetconduite.domain.utils
/**
 * Interface de logging centralisée.
 * Permet de logger les événements sans dépendre d'une implémentation spécifique.
 */
interface AppLogger {
    /**
     * Log d'information
     */
    fun log(message: String, tag: String = "RoadbookApp")

    /**
     * Log d'erreur
     */
    fun logError(message: String, throwable: Throwable? = null, tag: String = "RoadbookApp")

    /**
     * Log de debug (seulement en dev)
     */
    fun logDebug(message: String, tag: String = "RoadbookApp")

    /**
     * Log une transition d'état
     */
    fun logStateTransition(from: String, to: String, tag: String = "RoadbookApp") {
        log("State transition: $from → $to", tag)
    }

    /**
     * Log le démarrage d'une opération
     */
    fun logOperationStart(operation: String, tag: String = "RoadbookApp") {
        log("Starting operation: $operation", tag)
    }

    /**
     * Log la fin d'une opération
     */
    fun logOperationEnd(operation: String, success: Boolean, tag: String = "RoadbookApp") {
        val status = if (success) "✓" else "✗"
        log("Operation $status: $operation", tag)
    }
}

/**
 * Implémentation par défaut utilisant Android Log
 */
class AndroidAppLogger : AppLogger {
    override fun log(message: String, tag: String) {
        android.util.Log.i(tag, message)
    }

    override fun logError(message: String, throwable: Throwable?, tag: String) {
        if (throwable != null) {
            android.util.Log.e(tag, message, throwable)
        } else {
            android.util.Log.e(tag, message)
        }
    }

    override fun logDebug(message: String, tag: String) {
        android.util.Log.d(tag, message)
    }
}

/**
 * Implémentation pour les tests (ne fait rien)
 */
class NoOpLogger : AppLogger {
    override fun log(message: String, tag: String) {}
    override fun logError(message: String, throwable: Throwable?, tag: String) {}
    override fun logDebug(message: String, tag: String) {}
}

/**
 * Implémentation qui collecte les logs (pour les tests)
 */
class TestLogger : AppLogger {
    private val _logs = mutableListOf<LogEntry>()
    val logs: List<LogEntry> get() = _logs.toList()

    data class LogEntry(
        val message: String,
        val tag: String,
        val level: LogLevel,
        val throwable: Throwable? = null
    )

    enum class LogLevel { INFO, ERROR, DEBUG }

    override fun log(message: String, tag: String) {
        _logs.add(LogEntry(message, tag, LogLevel.INFO))
    }

    override fun logError(message: String, throwable: Throwable?, tag: String) {
        _logs.add(LogEntry(message, tag, LogLevel.ERROR, throwable))
    }

    override fun logDebug(message: String, tag: String) {
        _logs.add(LogEntry(message, tag, LogLevel.DEBUG))
    }

    fun clear() {
        _logs.clear()
    }

    fun getLogsByLevel(level: LogLevel): List<LogEntry> {
        return _logs.filter { it.level == level }
    }
}