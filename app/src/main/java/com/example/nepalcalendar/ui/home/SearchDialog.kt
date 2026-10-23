package com.example.nepalcalendar.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.calendar.model.Festival
import com.example.nepalcalendar.data.local.EventEntity
import com.example.nepalcalendar.data.repository.FestivalRepository
import com.example.ui.theme.HolidayRed

@Composable
fun SearchDialog(
    currentBsYear: Int,
    events: List<EventEntity>,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean,
    onDismiss: () -> Unit,
    onSelectDate: (NepaliDate) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val allFestivals = remember(currentBsYear) {
        FestivalRepository.getAllFestivalsForYear(currentBsYear)
    }

    val matchedFestivals = remember(query, allFestivals) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase()
            allFestivals.filter {
                it.nameNp.contains(q, ignoreCase = true) ||
                it.nameEn.contains(q, ignoreCase = true) ||
                it.descriptionNp.contains(q, ignoreCase = true) ||
                it.descriptionEn.contains(q, ignoreCase = true)
            }
        }
    }

    val matchedEvents = remember(query, events) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase()
            events.filter {
                it.title.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(if (useNepaliLanguage) "चाडपर्व वा कार्यक्रम खोज्नुहोस्..." else "Search festivals or events...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_dialog_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (query.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (useNepaliLanguage) "दशैँ, तिहार, शिवरात्रि वा व्यक्तिगत कार्यक्रमको नाम लेख्नुहोस्।" else "Search Dashain, Tihar, Shivaratri, or personal events.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (matchedFestivals.isEmpty() && matchedEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (useNepaliLanguage) "कुनै नतिजा फेला परेन" else "No matching results found",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (matchedFestivals.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (useNepaliLanguage) "चाडपर्व तथा बिदाहरू" else "Festivals & Holidays",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(matchedFestivals) { fest ->
                                val date = NepaliDate(
                                    year = if (fest.bsYear > 0) fest.bsYear else currentBsYear,
                                    month = fest.bsMonth,
                                    day = fest.bsDay
                                )
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectDate(date)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = null,
                                            tint = if (fest.isPublicHoliday) HolidayRed else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.size(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (useNepaliLanguage) fest.nameNp else fest.nameEn,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = date.formatDisplay(useNepaliLanguage, useNepaliNumerals),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (matchedEvents.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (useNepaliLanguage) "व्यक्तिगत कार्यक्रमहरू" else "Personal Events",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            items(matchedEvents) { ev ->
                                val date = NepaliDate(ev.bsYear, ev.bsMonth, ev.bsDay)
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectDate(date)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.size(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ev.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = date.formatDisplay(useNepaliLanguage, useNepaliNumerals),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
