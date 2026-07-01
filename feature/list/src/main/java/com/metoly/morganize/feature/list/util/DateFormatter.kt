package com.metoly.morganize.feature.list.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Thread-safe date formatting utility using java.time.DateTimeFormatter.
 * All formatters are immutable and thread-safe, unlike SimpleDateFormat.
 */
object DateFormatter {

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault())
            .withZone(ZoneId.systemDefault())

    fun formatWithTime(millis: Long): String =
        dateTimeFormatter.format(Instant.ofEpochMilli(millis))
}
