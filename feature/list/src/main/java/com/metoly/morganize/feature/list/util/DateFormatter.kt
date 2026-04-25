package com.metoly.morganize.feature.list.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val dateFormat by lazy { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    private val dateTimeFormat by lazy {
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
    }

    fun formatWithTime(millis: Long): String = dateTimeFormat.format(Date(millis))
}
