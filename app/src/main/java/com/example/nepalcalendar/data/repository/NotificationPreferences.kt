package com.example.nepalcalendar.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * State holder for daily date notification preferences backed by Jetpack DataStore.
 */
data class NotificationPreferences(
    val isEnabled: Boolean = false,
    val isOngoing: Boolean = true, // Pinned in status bar for quick glance
    val includeGregorian: Boolean = true,
    val includeFestivals: Boolean = true,
    val notificationHour: Int = 7,
    val notificationMinute: Int = 0
) {
    fun formattedTime(): String {
        val period = if (notificationHour < 12) "AM" else "PM"
        val displayHour = when {
            notificationHour == 0 -> 12
            notificationHour > 12 -> notificationHour - 12
            else -> notificationHour
        }
        return String.format(java.util.Locale.US, "%02d:%02d %s", displayHour, notificationMinute, period)
    }
}


private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

class NotificationPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("notif_is_enabled")
        val IS_ONGOING = booleanPreferencesKey("notif_is_ongoing")
        val INCLUDE_GREGORIAN = booleanPreferencesKey("notif_include_gregorian")
        val INCLUDE_FESTIVALS = booleanPreferencesKey("notif_include_festivals")
        val NOTIFICATION_HOUR = intPreferencesKey("notif_notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notif_notification_minute")
    }

    val notificationPreferencesFlow: Flow<NotificationPreferences> = context.notificationDataStore.data.map { prefs ->
        NotificationPreferences(
            isEnabled = prefs[Keys.IS_ENABLED] ?: false,
            isOngoing = prefs[Keys.IS_ONGOING] ?: true,
            includeGregorian = prefs[Keys.INCLUDE_GREGORIAN] ?: true,
            includeFestivals = prefs[Keys.INCLUDE_FESTIVALS] ?: true,
            notificationHour = prefs[Keys.NOTIFICATION_HOUR] ?: 7,
            notificationMinute = prefs[Keys.NOTIFICATION_MINUTE] ?: 0
        )
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[Keys.IS_ENABLED] = enabled }
    }

    suspend fun setNotificationOngoing(ongoing: Boolean) {
        context.notificationDataStore.edit { it[Keys.IS_ONGOING] = ongoing }
    }

    suspend fun setIncludeGregorian(include: Boolean) {
        context.notificationDataStore.edit { it[Keys.INCLUDE_GREGORIAN] = include }
    }

    suspend fun setIncludeFestivals(include: Boolean) {
        context.notificationDataStore.edit { it[Keys.INCLUDE_FESTIVALS] = include }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.notificationDataStore.edit {
            it[Keys.NOTIFICATION_HOUR] = hour
            it[Keys.NOTIFICATION_MINUTE] = minute
        }
    }
}
