package com.florent.carnetconduite.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatTimeOrEmpty(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val instant = Instant.ofEpochMilli(epochMillis)
    val time = LocalTime.ofInstant(instant, ZoneId.systemDefault())
    return time.format(DateTimeFormatter.ofPattern("HH:mm"))
}
