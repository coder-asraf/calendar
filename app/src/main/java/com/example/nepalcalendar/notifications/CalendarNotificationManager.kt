package com.example.nepalcalendar.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.nepalcalendar.calendar.engine.BsCalendarEngine
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.data.repository.FestivalRepository
import com.example.nepalcalendar.data.repository.NotificationPreferences
import com.example.nepalcalendar.data.repository.UserPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CalendarNotificationManager {

    const val CHANNEL_ID = "nepal_calendar_date_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nepali Date Notification"
            val descriptionText = "Displays the current Bikram Sambat date and festivals"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and displays the customized date notification based on NotificationPreferences state holder.
     */
    fun showOrUpdateDateNotification(
        context: Context,
        notificationPreferences: NotificationPreferences,
        userPreferences: UserPreferences,
        targetBsDate: NepaliDate? = null,
        targetAdDate: LocalDate? = null
    ) {
        if (!notificationPreferences.isEnabled) {
            cancelNotification(context)
            return
        }

        createNotificationChannel(context)

        val todayAd = targetAdDate ?: LocalDate.now()
        val todayBs = targetBsDate ?: runCatching { BsCalendarEngine.adToBs(todayAd) }
            .getOrDefault(NepaliDate(2083, 7, 6))

        val useNepaliLang = userPreferences.language == "np"
        val useNepaliNumerals = userPreferences.numeralSystem == "NEPALI"

        // Title: e.g. "२०८३ कार्तिक ६, शुक्रबार" or "Kartik 6, 2083 (Friday)"
        val titleText = todayBs.formatDisplay(useNepaliLang, useNepaliNumerals)

        // Subtitle / Content lines: Gregorian date & Today's festivals
        val adFormatted = todayAd.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
        val festivals = FestivalRepository.getFestivalsForDate(todayBs)
        val holidayText = if (festivals.isNotEmpty()) {
            festivals.joinToString(", ") { if (useNepaliLang) it.nameNp else it.nameEn }
        } else if (todayBs.isSaturday && userPreferences.highlightSaturday) {
            if (useNepaliLang) "शनिबार (साप्ताहिक बिदा)" else "Saturday (Weekly Holiday)"
        } else ""

        val contentTextBuilder = StringBuilder()
        if (notificationPreferences.includeGregorian) {
            contentTextBuilder.append(adFormatted)
        }
        if (notificationPreferences.includeFestivals && holidayText.isNotEmpty()) {
            if (contentTextBuilder.isNotEmpty()) contentTextBuilder.append(" · ")
            contentTextBuilder.append(holidayText)
        }

        val contentText = if (contentTextBuilder.isNotEmpty()) {
            contentTextBuilder.toString()
        } else {
            if (useNepaliLang) "नेपाल क्यालेन्डर प्रो" else "Nepal Calendar Pro"
        }

        // Tap action opens MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$titleText\n$contentText"))
            .setOngoing(notificationPreferences.isOngoing)
            .setAutoCancel(!notificationPreferences.isOngoing)
            .setPriority(if (notificationPreferences.isOngoing) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Backward-compatible overload accepting UserPreferences.
     */
    fun showOrUpdateDateNotification(
        context: Context,
        preferences: UserPreferences,
        targetBsDate: NepaliDate? = null,
        targetAdDate: LocalDate? = null
    ) {
        val notifPrefs = NotificationPreferences(
            isEnabled = preferences.dateNotificationEnabled,
            isOngoing = preferences.notificationOngoing,
            includeGregorian = preferences.notificationIncludeGregorian,
            includeFestivals = preferences.notificationIncludeFestivals,
            notificationHour = preferences.notificationTimeHour,
            notificationMinute = preferences.notificationTimeMinute
        )
        showOrUpdateDateNotification(context, notifPrefs, preferences, targetBsDate, targetAdDate)
    }

    /**
     * Cancels the active date notification.
     */
    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
