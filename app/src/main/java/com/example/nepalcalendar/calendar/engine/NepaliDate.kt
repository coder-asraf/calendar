package com.example.nepalcalendar.calendar.engine

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Strongly typed representation of a date in the Bikram Sambat (BS) calendar system.
 * Month is 1-indexed: 1 = Baisakh, 12 = Chaitra.
 * Day is 1-indexed: 1..maxDaysInMonth.
 */
data class NepaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<NepaliDate> {

    init {
        require(year in BsCalendarData.MIN_BS_YEAR..BsCalendarData.MAX_BS_YEAR) {
            "BS Year $year is outside supported range (${BsCalendarData.MIN_BS_YEAR}..${BsCalendarData.MAX_BS_YEAR})"
        }
        require(month in 1..12) { "BS Month must be between 1 and 12, got $month" }
        require(day >= 1) { "BS Day must be >= 1, got $day" }
    }

    /**
     * Converts to Gregorian LocalDate using the verified engine.
     */
    fun toLocalDate(): LocalDate = BsCalendarEngine.bsToAd(this)

    /**
     * Day of week (Monday through Sunday) derived from the corresponding Gregorian date.
     */
    val dayOfWeek: DayOfWeek
        get() = toLocalDate().dayOfWeek

    /**
     * English month name (e.g. "Baisakh", "Kartik").
     */
    val monthNameEn: String
        get() = MONTH_NAMES_EN.getOrElse(month - 1) { "" }

    /**
     * Nepali (Devanagari) month name (e.g. "बैशाख", "कार्तिक").
     */
    val monthNameNp: String
        get() = MONTH_NAMES_NP.getOrElse(month - 1) { "" }

    /**
     * English weekday name (e.g. "Friday").
     */
    val dayOfWeekNameEn: String
        get() = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> "Sunday"
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
        }

    /**
     * Nepali weekday name (e.g. "शुक्रबार").
     */
    val dayOfWeekNameNp: String
        get() = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> "आइतबार"
            DayOfWeek.MONDAY -> "सोमबार"
            DayOfWeek.TUESDAY -> "मंगलबार"
            DayOfWeek.WEDNESDAY -> "बुधबार"
            DayOfWeek.THURSDAY -> "बिहिबार"
            DayOfWeek.FRIDAY -> "शुक्रबार"
            DayOfWeek.SATURDAY -> "शनिबार"
        }

    /**
     * Returns true if this day falls on Saturday (weekly holiday in Nepal).
     */
    val isSaturday: Boolean
        get() = dayOfWeek == DayOfWeek.SATURDAY

    /**
     * Formats this BS date according to user language and numeral preferences.
     * Example: "२०८३ कार्तिक ६, शुक्रबार" or "Kartik 6, 2083 (Friday)"
     */
    fun formatDisplay(useNepaliLanguage: Boolean, useNepaliNumerals: Boolean): String {
        val yStr = if (useNepaliNumerals) toNepaliDigits(year) else year.toString()
        val dStr = if (useNepaliNumerals) toNepaliDigits(day) else day.toString()
        return if (useNepaliLanguage) {
            "$yStr $monthNameNp $dStr, $dayOfWeekNameNp"
        } else {
            "$monthNameEn $dStr, $yStr ($dayOfWeekNameEn)"
        }
    }

    fun formatShort(useNepaliNumerals: Boolean): String {
        val yStr = if (useNepaliNumerals) toNepaliDigits(year) else year.toString()
        val mStr = if (useNepaliNumerals) toNepaliDigits(month) else month.toString().padStart(2, '0')
        val dStr = if (useNepaliNumerals) toNepaliDigits(day) else day.toString().padStart(2, '0')
        return "$yStr-$mStr-$dStr"
    }

    fun plusDays(days: Long): NepaliDate = BsCalendarEngine.addDays(this, days)

    fun minusDays(days: Long): NepaliDate = BsCalendarEngine.subtractDays(this, days)

    override fun compareTo(other: NepaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        val MONTH_NAMES_EN = listOf(
            "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
            "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
        )

        val MONTH_NAMES_NP = listOf(
            "बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज",
            "कार्तिक", "मंसिर", "पुस", "माघ", "फागुन", "चैत"
        )

        val WEEKDAY_NAMES_EN_SHORT = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val WEEKDAY_NAMES_NP_SHORT = listOf("आइत", "सोम", "मंगल", "बुध", "बिही", "शुक्र", "शनि")

        fun toNepaliDigits(number: Any): String {
            val str = number.toString()
            val devanagari = "०१२३४५६७८९"
            val sb = StringBuilder()
            for (ch in str) {
                if (ch in '0'..'9') {
                    sb.append(devanagari[ch - '0'])
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }
    }
}
