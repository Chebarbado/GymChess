package com.rnr.gymchess.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val historyDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"))

fun formatHistoryDate(epochMs: Long): String = historyDateFormat.format(Date(epochMs))
