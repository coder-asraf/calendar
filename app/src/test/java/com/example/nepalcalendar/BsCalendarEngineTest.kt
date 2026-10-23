package com.example.nepalcalendar

import com.example.nepalcalendar.calendar.engine.BsCalendarData
import com.example.nepalcalendar.calendar.engine.BsCalendarEngine
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.data.repository.FestivalRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class BsCalendarEngineTest {

    // 1. Independent Reference Dates from authoritative Panchanga tables
    @Test
    fun testIndependentReferenceDates() {
        val referencePairs = listOf(
            // BS Date to AD Date
            Triple(NepaliDate(1975, 1, 1), LocalDate.of(1918, 4, 13), "Anchor Epoch: 1975 Baisakh 1"),
            Triple(NepaliDate(2013, 2, 8), LocalDate.of(1956, 5, 21), "Reference 1: 2013 Jestha 8"),
            Triple(NepaliDate(2051, 10, 1), LocalDate.of(1995, 1, 15), "Reference 2: 2051 Magh 1"),
            Triple(NepaliDate(2076, 6, 27), LocalDate.of(2019, 10, 14), "Reference 3: 2076 Ashwin 27"),
            Triple(NepaliDate(2077, 4, 4), LocalDate.of(2020, 7, 19), "Reference 4: 2077 Shrawan 4"),
            Triple(NepaliDate(2081, 3, 31), LocalDate.of(2024, 7, 15), "Reference 5: 2081 Ashadh 31"),
            Triple(NepaliDate(2083, 7, 6), LocalDate.of(2026, 10, 23), "Reference 6: Today 2083 Kartik 6 = 2026-10-23")
        )

        for ((bs, expectedAd, label) in referencePairs) {
            val convertedAd = BsCalendarEngine.bsToAd(bs)
            assertEquals("Failed bsToAd for $label", expectedAd, convertedAd)

            val convertedBs = BsCalendarEngine.adToBs(expectedAd)
            assertEquals("Failed adToBs for $label", bs, convertedBs)
        }
    }

    // 2. Round-Trip Property: BS -> AD -> BS and AD -> BS -> AD
    @Test
    fun testRoundTripConversionsAcrossRange() {
        // Sample years across historical, modern, and future ranges
        val sampleYears = listOf(1975, 1980, 1990, 2000, 2010, 2020, 2030, 2040, 2050, 2060, 2070, 2080, 2083, 2090, 2100)

        for (year in sampleYears) {
            for (month in 1..12) {
                val maxDay = BsCalendarEngine.getDaysInMonth(year, month)
                // Test first day, middle day, and last day of every month
                val testDays = listOf(1, maxDay / 2, maxDay)

                for (day in testDays) {
                    val originalBs = NepaliDate(year, month, day)
                    val ad = BsCalendarEngine.bsToAd(originalBs)
                    val roundtripBs = BsCalendarEngine.adToBs(ad)
                    assertEquals("Roundtrip mismatch for $originalBs", originalBs, roundtripBs)

                    val roundtripAd = BsCalendarEngine.bsToAd(roundtripBs)
                    assertEquals("AD roundtrip mismatch for $ad", ad, roundtripAd)
                }
            }
        }
    }

    // 3. Weekday Alignment & Day-of-Week
    @Test
    fun testDayOfWeekAlignment() {
        // Today 2026-10-23 is Friday
        val todayAd = LocalDate.of(2026, 10, 23)
        assertEquals(DayOfWeek.FRIDAY, todayAd.dayOfWeek)

        val todayBs = BsCalendarEngine.adToBs(todayAd)
        assertEquals(DayOfWeek.FRIDAY, todayBs.dayOfWeek)
        assertEquals("शुक्रबार", todayBs.dayOfWeekNameNp)
        assertEquals("Friday", todayBs.dayOfWeekNameEn)
        assertFalse(todayBs.isSaturday)

        // Tomorrow is Saturday (Nepal weekly holiday)
        val tomorrowBs = todayBs.plusDays(1)
        assertEquals(DayOfWeek.SATURDAY, tomorrowBs.dayOfWeek)
        assertEquals("शनिबार", tomorrowBs.dayOfWeekNameNp)
        assertTrue(tomorrowBs.isSaturday)
    }

    // 4. Month and Year Boundaries
    @Test
    fun testMonthAndYearBoundaries() {
        val y = 2083
        val m = 1 // Baisakh
        val maxDaysBaisakh = BsCalendarEngine.getDaysInMonth(y, m)

        val lastDayBaisakh = NepaliDate(y, m, maxDaysBaisakh)
        val firstDayJestha = lastDayBaisakh.plusDays(1)

        assertEquals(y, firstDayJestha.year)
        assertEquals(2, firstDayJestha.month)
        assertEquals(1, firstDayJestha.day)

        // Year boundary: Chaitra last day to Baisakh 1 next year
        val lastDayChaitra = NepaliDate(y, 12, BsCalendarEngine.getDaysInMonth(y, 12))
        val newYearNext = lastDayChaitra.plusDays(1)

        assertEquals(y + 1, newYearNext.year)
        assertEquals(1, newYearNext.month)
        assertEquals(1, newYearNext.day)
    }

    // 5. Date Arithmetic
    @Test
    fun testDateArithmetic() {
        val baseDate = NepaliDate(2083, 7, 6)
        val plus30 = baseDate.plusDays(30)
        val diff = BsCalendarEngine.daysBetween(baseDate, plus30)
        assertEquals(30L, diff)

        val minus30 = plus30.minusDays(30)
        assertEquals(baseDate, minus30)
    }

    // 6. Age Calculator
    @Test
    fun testAgeCalculator() {
        val birth = NepaliDate(2050, 1, 1)
        val current = NepaliDate(2075, 1, 1)

        val age = BsCalendarEngine.calculateAge(birth, current)
        assertEquals(25, age.years)
        assertEquals(0, age.months)
        assertEquals(0, age.days)
        assertTrue(age.totalDays > 0)
    }

    // 7. Invalid Date Detection
    @Test
    fun testInvalidDateDetection() {
        assertFalse(BsCalendarEngine.isValidBsDate(1974, 1, 1)) // Under minimum
        assertFalse(BsCalendarEngine.isValidBsDate(2101, 1, 1)) // Over maximum
        assertFalse(BsCalendarEngine.isValidBsDate(2083, 0, 1)) // Invalid month 0
        assertFalse(BsCalendarEngine.isValidBsDate(2083, 13, 1)) // Invalid month 13
        assertFalse(BsCalendarEngine.isValidBsDate(2083, 1, 0)) // Invalid day 0
        assertFalse(BsCalendarEngine.isValidBsDate(2083, 1, 35)) // Exceeds month days
    }

    // 8. Festival Records Verification
    @Test
    fun testFestivalsDatasetIntegrity() {
        val festivals2083 = FestivalRepository.getAllFestivalsForYear(2083)
        assertTrue("Festivals list for 2083 should not be empty", festivals2083.isNotEmpty())

        for (fest in festivals2083) {
            assertTrue("Festival month must be valid: ${fest.nameEn}", fest.bsMonth in 1..12)
            val maxDays = BsCalendarEngine.getDaysInMonth(2083, fest.bsMonth)
            assertTrue("Festival day must be valid: ${fest.nameEn} on ${fest.bsMonth}/${fest.bsDay}", fest.bsDay in 1..maxDays)
            assertTrue("Festival should have Nepali name", fest.nameNp.isNotBlank())
            assertTrue("Festival should have English name", fest.nameEn.isNotBlank())
        }
    }
}
