package com.example.nepalcalendar.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val language: String = "np", // "np" or "en"
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val numeralSystem: String = "NEPALI", // "NEPALI" or "ARABIC"
    val firstDayOfWeek: Int = 1, // 1 = Sunday (आइतबार), 2 = Monday (सोमबार)
    val highlightSaturday: Boolean = true,
    val showFestivalsOnGrid: Boolean = true,
    // Customizable Notification Settings
    val dateNotificationEnabled: Boolean = false,
    val notificationOngoing: Boolean = true, // Pinned in status bar for quick glance
    val notificationIncludeGregorian: Boolean = true,
    val notificationIncludeFestivals: Boolean = true,
    val notificationTimeHour: Int = 7,
    val notificationTimeMinute: Int = 0
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("pref_language")
        val THEME_MODE = stringPreferencesKey("pref_theme_mode")
        val NUMERAL_SYSTEM = stringPreferencesKey("pref_numeral_system")
        val FIRST_DAY_OF_WEEK = intPreferencesKey("pref_first_day_of_week")
        val HIGHLIGHT_SATURDAY = booleanPreferencesKey("pref_highlight_saturday")
        val SHOW_FESTIVALS_ON_GRID = booleanPreferencesKey("pref_show_festivals")
        // Notification keys
        val DATE_NOTIFICATION_ENABLED = booleanPreferencesKey("pref_date_notification_enabled")
        val NOTIFICATION_ONGOING = booleanPreferencesKey("pref_notification_ongoing")
        val NOTIFICATION_INCLUDE_GREGORIAN = booleanPreferencesKey("pref_notification_include_gregorian")
        val NOTIFICATION_INCLUDE_FESTIVALS = booleanPreferencesKey("pref_notification_include_festivals")
        val NOTIFICATION_TIME_HOUR = intPreferencesKey("pref_notification_time_hour")
        val NOTIFICATION_TIME_MINUTE = intPreferencesKey("pref_notification_time_minute")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            language = prefs[Keys.LANGUAGE] ?: "np",
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            numeralSystem = prefs[Keys.NUMERAL_SYSTEM] ?: "NEPALI",
            firstDayOfWeek = prefs[Keys.FIRST_DAY_OF_WEEK] ?: 1,
            highlightSaturday = prefs[Keys.HIGHLIGHT_SATURDAY] ?: true,
            showFestivalsOnGrid = prefs[Keys.SHOW_FESTIVALS_ON_GRID] ?: true,
            dateNotificationEnabled = prefs[Keys.DATE_NOTIFICATION_ENABLED] ?: false,
            notificationOngoing = prefs[Keys.NOTIFICATION_ONGOING] ?: true,
            notificationIncludeGregorian = prefs[Keys.NOTIFICATION_INCLUDE_GREGORIAN] ?: true,
            notificationIncludeFestivals = prefs[Keys.NOTIFICATION_INCLUDE_FESTIVALS] ?: true,
            notificationTimeHour = prefs[Keys.NOTIFICATION_TIME_HOUR] ?: 7,
            notificationTimeMinute = prefs[Keys.NOTIFICATION_TIME_MINUTE] ?: 0
        )
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setNumeralSystem(numeral: String) {
        context.dataStore.edit { it[Keys.NUMERAL_SYSTEM] = numeral }
    }

    suspend fun setFirstDayOfWeek(firstDay: Int) {
        context.dataStore.edit { it[Keys.FIRST_DAY_OF_WEEK] = firstDay }
    }

    suspend fun setHighlightSaturday(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGHLIGHT_SATURDAY] = enabled }
    }

    suspend fun setShowFestivalsOnGrid(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_FESTIVALS_ON_GRID] = enabled }
    }

    suspend fun setDateNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DATE_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setNotificationOngoing(ongoing: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_ONGOING] = ongoing }
    }

    suspend fun setNotificationIncludeGregorian(include: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_INCLUDE_GREGORIAN] = include }
    }

    suspend fun setNotificationIncludeFestivals(include: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_INCLUDE_FESTIVALS] = include }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.NOTIFICATION_TIME_HOUR] = hour
            it[Keys.NOTIFICATION_TIME_MINUTE] = minute
        }
    }
}
