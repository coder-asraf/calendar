package com.example.nepalcalendar.calendar.engine

import java.time.LocalDate

data class DateConversionResult(
    val bsDate: NepaliDate,
    val adDate: LocalDate,
    val isBsToAd: Boolean,
    val dayOfWeekNameNp: String,
    val dayOfWeekNameEn: String,
    val isSaturday: Boolean,
    val isSunday: Boolean = false,
    val isWeekendHoliday: Boolean = isSaturday || isSunday,
    val formattedBsNp: String,
    val formattedBsEn: String,
    val formattedAd: String,
    val seasonNp: String = "",
    val seasonEn: String = "",
    val dayOfYearBs: Int = 0,
    val totalDaysInBsYear: Int = 365,
    val dayOfYearAd: Int = 0,
    val festivalNames: List<String> = emptyList(),
    val isPublicHoliday: Boolean = false
)

data class ConversionHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val isBsToAd: Boolean,
    val sourceFormatted: String,
    val targetFormatted: String,
    val weekday: String,
    val bsYear: Int = 0,
    val bsMonth: Int = 0,
    val bsDay: Int = 0,
    val adYear: Int = 0,
    val adMonth: Int = 0,
    val adDay: Int = 0
)

data class DateRangeItem(
    val bsDate: NepaliDate,
    val adDate: LocalDate,
    val dayOfWeekNameNp: String,
    val dayOfWeekNameEn: String,
    val isWeekendHoliday: Boolean,
    val festivalName: String,
    val isPublicHoliday: Boolean
)
