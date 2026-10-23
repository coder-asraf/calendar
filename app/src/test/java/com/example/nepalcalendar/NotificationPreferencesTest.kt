package com.example.nepalcalendar

import com.example.nepalcalendar.data.repository.NotificationPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPreferencesTest {

    @Test
    fun defaultPreferences_haveExpectedValues() {
        val prefs = NotificationPreferences()
        assertFalse("Default should not be enabled until user turns it on", prefs.isEnabled)
        assertTrue("Default notification style should be ongoing sticky", prefs.isOngoing)
        assertTrue("Default notification should include Gregorian date", prefs.includeGregorian)
        assertTrue("Default notification should include festivals", prefs.includeFestivals)
        assertEquals(7, prefs.notificationHour)
        assertEquals(0, prefs.notificationMinute)
    }

    @Test
    fun formattedTime_returnsFormattedString() {
        val morning = NotificationPreferences(notificationHour = 7, notificationMinute = 5)
        assertEquals("07:05 AM", morning.formattedTime())

        val afternoon = NotificationPreferences(notificationHour = 14, notificationMinute = 30)
        assertEquals("02:30 PM", afternoon.formattedTime())

        val midnight = NotificationPreferences(notificationHour = 0, notificationMinute = 0)
        assertEquals("12:00 AM", midnight.formattedTime())

        val noon = NotificationPreferences(notificationHour = 12, notificationMinute = 0)
        assertEquals("12:00 PM", noon.formattedTime())
    }

    @Test
    fun copy_updatesFieldsCorrectly() {
        val initial = NotificationPreferences()
        val updated = initial.copy(
            isEnabled = true,
            isOngoing = false,
            includeGregorian = false,
            includeFestivals = false,
            notificationHour = 8,
            notificationMinute = 15
        )

        assertTrue(updated.isEnabled)
        assertFalse(updated.isOngoing)
        assertFalse(updated.includeGregorian)
        assertFalse(updated.includeFestivals)
        assertEquals(8, updated.notificationHour)
        assertEquals(15, updated.notificationMinute)
    }
}
