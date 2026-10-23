package com.example.nepalcalendar.calendar.engine

import java.time.LocalDate

data class DateConversionResult(
    val bsDate: NepaliDate,
    val adDate: LocalDate,
    val isBsToAd: Boolean,
    val dayOfWeekNameNp: String,
    val dayOfWeekNameEn: String,
    val isSaturday: Boolean,
    val formattedBsNp: String,
    val formattedBsEn: String,
    val formattedAd: String
)

data class ConversionHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val isBsToAd: Boolean,
    val sourceFormatted: String,
    val targetFormatted: String,
    val weekday: String
)
