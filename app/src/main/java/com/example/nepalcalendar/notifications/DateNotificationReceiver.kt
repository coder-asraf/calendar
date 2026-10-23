package com.example.nepalcalendar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nepalcalendar.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DateNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = PreferencesRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = repo.userPreferencesFlow.first()
            if (prefs.dateNotificationEnabled) {
                CalendarNotificationManager.showOrUpdateDateNotification(context, prefs)
            }
        }
    }
}
