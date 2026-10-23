package com.example.nepalcalendar.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.calendar.model.Festival
import com.example.nepalcalendar.data.local.EventEntity
import com.example.ui.theme.HolidayRed
import java.time.format.DateTimeFormatter

@Composable
fun DateDetailCard(
    date: NepaliDate,
    festivals: List<Festival>,
    events: List<EventEntity>,
    useNepaliNumerals: Boolean,
    useNepaliLanguage: Boolean,
    onAddEventClick: () -> Unit,
    onDeleteEventClick: (EventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adDate = remember(date) { date.toLocalDate() }
    val adFormatted = remember(adDate) {
        adDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
    }

    val bsDisplay = remember(date, useNepaliLanguage, useNepaliNumerals) {
        date.formatDisplay(useNepaliLanguage, useNepaliNumerals)
    }

    val isWeekendHoliday = remember(date) { date.isWeekendHoliday }
    val hasHoliday = remember(festivals, isWeekendHoliday) {
        isWeekendHoliday || festivals.any { it.isPublicHoliday }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_detail_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Formatted BS date + Share Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bsDisplay,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = adFormatted,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        shareDate(context, date, adFormatted, festivals)
                    },
                    modifier = Modifier.testTag("share_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Holiday badge if applicable
            if (hasHoliday) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isWeekendHoliday) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HolidayRed.copy(alpha = 0.12f)
                        ) {
                            val holidayLabel = if (date.isSaturday) {
                                if (useNepaliLanguage) "शनिबार (साप्ताहिक बिदा)" else "Saturday (Weekly Holiday)"
                            } else {
                                if (useNepaliLanguage) "आइतबार (साप्ताहिक बिदा)" else "Sunday (Weekly Holiday)"
                            }
                            Text(
                                text = holidayLabel,
                                color = HolidayRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (festivals.any { it.isPublicHoliday }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HolidayRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (useNepaliLanguage) "सार्वजनिक बिदा" else "Public Holiday",
                                color = HolidayRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Festivals / Observances Section
            if (festivals.isNotEmpty()) {
                Text(
                    text = if (useNepaliLanguage) "चाडपर्व तथा विशेष दिवस" else "Festivals & Observances",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                festivals.forEach { fest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = if (fest.isPublicHoliday) HolidayRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (useNepaliLanguage) fest.nameNp else fest.nameEn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val desc = if (useNepaliLanguage) fest.descriptionNp else fest.descriptionEn
                            if (desc.isNotEmpty()) {
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Personal Events Section
            if (events.isNotEmpty()) {
                Text(
                    text = if (useNepaliLanguage) "व्यक्तिगत कार्यक्रमहरू" else "Personal Events",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                events.forEach { ev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = ev.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (ev.description.isNotEmpty()) {
                                    Text(
                                        text = ev.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { onDeleteEventClick(ev) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Event",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Quick Action: Add Event Button
            OutlinedButton(
                onClick = onAddEventClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_event_quick_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (useNepaliLanguage) "यस मितिमा कार्यक्रम थप्नुहोस्" else "Add Event for this Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun shareDate(
    context: Context,
    bsDate: NepaliDate,
    adFormatted: String,
    festivals: List<Festival>
) {
    val bsNepali = bsDate.formatDisplay(useNepaliLanguage = true, useNepaliNumerals = true)
    val bsEnglish = bsDate.formatDisplay(useNepaliLanguage = false, useNepaliNumerals = false)
    val festString = if (festivals.isNotEmpty()) {
        "\nFestivals: " + festivals.joinToString(", ") { "${it.nameNp} (${it.nameEn})" }
    } else ""

    val textToShare = """
        🇳🇵 Nepal Calendar Pro
        वि.सं.: $bsNepali
        BS: $bsEnglish
        AD: $adFormatted$festString
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Nepali Date: $bsNepali")
        putExtra(Intent.EXTRA_TEXT, textToShare)
    }
    context.startActivity(Intent.createChooser(intent, "Share Date"))
}
