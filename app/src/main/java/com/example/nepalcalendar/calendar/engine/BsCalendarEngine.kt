package com.example.nepalcalendar.calendar.engine

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Arrays

/**
 * High-performance, offline Bikram Sambat calendar calculation engine.
 * Precomputes cumulative day sums for O(1) day-of-year calculations and binary-search ordinal conversion.
 * Supported range: 1975 BS to 2100 BS (inclusive).
 */
object BsCalendarEngine {

    val MIN_BS_YEAR: Int = BsCalendarData.MIN_BS_YEAR
    val MAX_BS_YEAR: Int = BsCalendarData.MAX_BS_YEAR

    // 1975 BS Baisakh 1 corresponds to 1918-04-13 AD (Ordinal 1)
    val ANCHOR_AD: LocalDate = LocalDate.of(
        BsCalendarData.ANCHOR_AD_YEAR,
        BsCalendarData.ANCHOR_AD_MONTH,
        BsCalendarData.ANCHOR_AD_DAY
    )

    // Precomputed cumulative days before each BS year (0-indexed relative to MIN_BS_YEAR)
    // daysBeforeYear[0] = 0 (before 1975)
    // daysBeforeYear[1] = days in 1975
    // ...
    private val daysBeforeYear: LongArray = LongArray(MAX_BS_YEAR - MIN_BS_YEAR + 2).apply {
        var cumulative = 0L
        this[0] = 0L
        for (y in MIN_BS_YEAR..MAX_BS_YEAR) {
            val yIdx = y - MIN_BS_YEAR
            val totalInYear = BsCalendarData.MONTH_DAYS[yIdx].sum()
            cumulative += totalInYear
            this[yIdx + 1] = cumulative
        }
    }

    val MAX_ORDINAL: Long = daysBeforeYear[MAX_BS_YEAR - MIN_BS_YEAR + 1]

    /**
     * Checks if year is within verified dataset.
     */
    fun isBsYearValid(year: Int): Boolean = year in MIN_BS_YEAR..MAX_BS_YEAR

    /**
     * Checks if month is valid (1..12).
     */
    fun isBsMonthValid(month: Int): Boolean = month in 1..12

    /**
     * Returns the exact number of days in the specified BS year and month.
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        require(isBsYearValid(year)) { "Year $year outside supported range ($MIN_BS_YEAR..$MAX_BS_YEAR)" }
        require(isBsMonthValid(month)) { "Month $month must be in 1..12" }
        return BsCalendarData.MONTH_DAYS[year - MIN_BS_YEAR][month - 1]
    }

    /**
     * Returns total days in a given BS year (typically 365 or 366).
     */
    fun getDaysInYear(year: Int): Int {
        require(isBsYearValid(year)) { "Year $year outside supported range" }
        return BsCalendarData.MONTH_DAYS[year - MIN_BS_YEAR].sum()
    }

    /**
     * Validates whether a BS date is mathematically valid within the calendar.
     */
    fun isValidBsDate(year: Int, month: Int, day: Int): Boolean {
        if (!isBsYearValid(year) || !isBsMonthValid(month)) return false
        val maxDays = getDaysInMonth(year, month)
        return day in 1..maxDays
    }

    /**
     * Converts a (year, month, day) in BS to 1-based ordinal days starting from 1975-01-01 BS.
     */
    fun bsToOrdinal(year: Int, month: Int, day: Int): Long {
        require(isValidBsDate(year, month, day)) { "Invalid BS date: $year-$month-$day" }
        val yearOffset = year - MIN_BS_YEAR
        var days = daysBeforeYear[yearOffset]
        val monthArray = BsCalendarData.MONTH_DAYS[yearOffset]
        for (m in 1 until month) {
            days += monthArray[m - 1]
        }
        days += day
        return days
    }

    /**
     * Converts 1-based ordinal days back to NepaliDate.
     */
    fun ordinalToBs(ordinal: Long): NepaliDate {
        require(ordinal in 1..MAX_ORDINAL) { "Ordinal $ordinal outside supported range (1..$MAX_ORDINAL)" }

        // Binary search to find the year index where daysBeforeYear[yearIdx] < ordinal <= daysBeforeYear[yearIdx + 1]
        var low = 0
        var high = MAX_BS_YEAR - MIN_BS_YEAR
        var yearIdx = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (daysBeforeYear[mid] < ordinal) {
                yearIdx = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        val year = MIN_BS_YEAR + yearIdx
        var remainingDays = (ordinal - daysBeforeYear[yearIdx]).toInt()
        val monthArray = BsCalendarData.MONTH_DAYS[yearIdx]

        var month = 1
        for (m in 1..12) {
            val dim = monthArray[m - 1]
            if (remainingDays <= dim) {
                month = m
                break
            }
            remainingDays -= dim
        }

        return NepaliDate(year = year, month = month, day = remainingDays)
    }

    /**
     * Converts Bikram Sambat date to Gregorian LocalDate.
     */
    fun bsToAd(bsDate: NepaliDate): LocalDate {
        val ordinal = bsToOrdinal(bsDate.year, bsDate.month, bsDate.day)
        return ANCHOR_AD.plusDays(ordinal - 1)
    }

    /**
     * Converts Gregorian LocalDate to Bikram Sambat date.
     */
    fun adToBs(adDate: LocalDate): NepaliDate {
        val diffDays = ChronoUnit.DAYS.between(ANCHOR_AD, adDate)
        val ordinal = diffDays + 1
        require(ordinal in 1..MAX_ORDINAL) {
            "AD date $adDate is outside supported conversion range (${ANCHOR_AD} to ${bsToAd(NepaliDate(MAX_BS_YEAR, 12, getDaysInMonth(MAX_BS_YEAR, 12)))})"
        }
        return ordinalToBs(ordinal)
    }

    /**
     * Returns today's Bikram Sambat date based on the system local date.
     */
    fun todayBs(): NepaliDate {
        return adToBs(LocalDate.now())
    }

    /**
     * Days between two BS dates (d2 - d1).
     */
    fun daysBetween(d1: NepaliDate, d2: NepaliDate): Long {
        val o1 = bsToOrdinal(d1.year, d1.month, d1.day)
        val o2 = bsToOrdinal(d2.year, d2.month, d2.day)
        return o2 - o1
    }

    /**
     * Add days to a BS date.
     */
    fun addDays(date: NepaliDate, days: Long): NepaliDate {
        val ordinal = bsToOrdinal(date.year, date.month, date.day) + days
        return ordinalToBs(ordinal)
    }

    /**
     * Subtract days from a BS date.
     */
    fun subtractDays(date: NepaliDate, days: Long): NepaliDate {
        return addDays(date, -days)
    }

    /**
     * Navigation: previous month.
     */
    fun getPreviousMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 1) {
            Pair(year - 1, 12)
        } else {
            Pair(year, month - 1)
        }
    }

    /**
     * Navigation: next month.
     */
    fun getNextMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 12) {
            Pair(year + 1, 1)
        } else {
            Pair(year, month + 1)
        }
    }

    /**
     * Age calculation result.
     */
    data class AgeResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val nextBirthdayInDays: Long
    )

    /**
     * Calculates age between birth BS date and target BS date.
     */
    fun calculateAge(birthDate: NepaliDate, asOfDate: NepaliDate = todayBs()): AgeResult {
        require(birthDate <= asOfDate) { "Birth date must be before or equal to target date" }

        var yDiff = asOfDate.year - birthDate.year
        var mDiff = asOfDate.month - birthDate.month
        var dDiff = asOfDate.day - birthDate.day

        if (dDiff < 0) {
            // Borrow days from previous month of asOfDate
            val (prevY, prevM) = getPreviousMonth(asOfDate.year, asOfDate.month)
            val daysInPrevMonth = getDaysInMonth(prevY, prevM)
            dDiff += daysInPrevMonth
            mDiff -= 1
        }

        if (mDiff < 0) {
            mDiff += 12
            yDiff -= 1
        }

        val totalDays = daysBetween(birthDate, asOfDate)

        // Calculate next birthday
        var nextBdayYear = asOfDate.year
        val maxDaysThisYear = getDaysInMonth(nextBdayYear, birthDate.month)
        val validDayThisYear = birthDate.day.coerceAtMost(maxDaysThisYear)
        val thisYearBday = NepaliDate(nextBdayYear, birthDate.month, validDayThisYear)

        val nextBday = if (thisYearBday >= asOfDate) {
            thisYearBday
        } else {
            val maxDaysNextYear = getDaysInMonth(nextBdayYear + 1, birthDate.month)
            NepaliDate(nextBdayYear + 1, birthDate.month, birthDate.day.coerceAtMost(maxDaysNextYear))
        }
        val daysUntilNextBday = daysBetween(asOfDate, nextBday)

        return AgeResult(
            years = yDiff,
            months = mDiff,
            days = dDiff,
            totalDays = totalDays,
            nextBirthdayInDays = daysUntilNextBday
        )
    }
}
