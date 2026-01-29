package com.florent.carnetconduite.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatTime(timestamp: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "N/A"
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (e: Exception) {
        dateString
    }
}

fun formatTimeRange(startTime: Long, endTime: Long?, ongoingLabel: String = "En cours..."): String {
    val start = formatTime(startTime)
    val end = endTime?.let { formatTime(it) } ?: ongoingLabel
    return "$start → $end"
}
