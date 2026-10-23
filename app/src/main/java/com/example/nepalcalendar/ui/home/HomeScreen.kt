package com.example.nepalcalendar.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.MainViewModel
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.data.local.EventEntity
import com.example.nepalcalendar.data.repository.FestivalRepository
import com.example.nepalcalendar.ui.events.AddEditEventDialog
import com.example.ui.theme.HolidayRed
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    displayedYear: Int,
    displayedMonth: Int,
    selectedDate: NepaliDate,
    todayBs: NepaliDate,
    events: List<EventEntity>,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean,
    firstDayOfWeek: Int,
    highlightSaturday: Boolean,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    val festivalsForMonth = remember(displayedYear, displayedMonth) {
        FestivalRepository.getFestivalsForMonth(displayedYear, displayedMonth)
    }

    val festivalsForSelectedDate = remember(festivalsForMonth, selectedDate) {
        festivalsForMonth.filter { it.bsDay == selectedDate.day }
    }

    val eventsForSelectedDate = remember(events, selectedDate) {
        events.filter {
            it.bsYear == selectedDate.year &&
            it.bsMonth == selectedDate.month &&
            it.bsDay == selectedDate.day
        }
    }

    val todayFestivals = remember(todayBs) {
        FestivalRepository.getFestivalsForDate(todayBs)
    }

    val monthName = remember(displayedMonth, useNepaliLanguage) {
        if (useNepaliLanguage) NepaliDate.MONTH_NAMES_NP[displayedMonth - 1]
        else NepaliDate.MONTH_NAMES_EN[displayedMonth - 1]
    }

    val yearText = remember(displayedYear, useNepaliNumerals) {
        if (useNepaliNumerals) NepaliDate.toNepaliDigits(displayedYear)
        else displayedYear.toString()
    }

    val todayAdFormatted = remember(viewModel.todayAd) {
        viewModel.todayAd.format(DateTimeFormatter.ofPattern("d MMMM yyyy · EEEE"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("today_hero_banner"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (useNepaliLanguage) "आजको मिति (Today)" else "Today's Date",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = todayBs.formatDisplay(useNepaliLanguage, useNepaliNumerals),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Quick Jump to Today
                    Button(
                        onClick = { viewModel.goToToday() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("today_quick_jump_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (useNepaliLanguage) "आज" else "Today",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = todayAdFormatted,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )

                if (todayFestivals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = HolidayRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = todayFestivals.joinToString(", ") {
                                if (useNepaliLanguage) it.nameNp else it.nameEn
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HolidayRed
                        )
                    }
                }

                val userPrefs by viewModel.userPreferences.collectAsState()
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (userPrefs.dateNotificationEnabled) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    },
                    modifier = Modifier
                        .clickable { viewModel.setTab(com.example.nepalcalendar.AppTab.SETTINGS) }
                        .testTag("today_banner_notification_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (userPrefs.dateNotificationEnabled) {
                                Icons.Default.NotificationsActive
                            } else {
                                Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = if (userPrefs.dateNotificationEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userPrefs.dateNotificationEnabled) {
                                if (useNepaliLanguage) "दैनिक मिति सूचना सक्रिय (पिन गरिएको)" else "Status Bar Date Notification Active"
                            } else {
                                if (useNepaliLanguage) "नोटिफिकेसन बारमा मिति राख्नुहोस्" else "Show Date in Notification Bar"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (userPrefs.dateNotificationEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        // Calendar Month Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.previousMonth() },
                modifier = Modifier.testTag("prev_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month"
                )
            }

            // Clickable Month & Year title to open Month/Year Picker
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showMonthPicker = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("month_year_header_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$monthName $yearText",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { viewModel.nextMonth() },
                modifier = Modifier.testTag("next_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month"
                )
            }
        }

        // Calendar Grid
        CalendarGrid(
            year = displayedYear,
            month = displayedMonth,
            selectedDate = selectedDate,
            todayBs = todayBs,
            festivalsForMonth = festivalsForMonth,
            eventsForMonth = events,
            useNepaliNumerals = useNepaliNumerals,
            useNepaliLanguage = useNepaliLanguage,
            firstDayOfWeek = firstDayOfWeek,
            highlightSaturday = highlightSaturday,
            onDateSelected = { date -> viewModel.selectDate(date) },
            onSwipeLeft = { viewModel.nextMonth() },
            onSwipeRight = { viewModel.previousMonth() }
        )

        // Detail card for the currently selected date
        DateDetailCard(
            date = selectedDate,
            festivals = festivalsForSelectedDate,
            events = eventsForSelectedDate,
            useNepaliNumerals = useNepaliNumerals,
            useNepaliLanguage = useNepaliLanguage,
            onAddEventClick = { showAddEventDialog = true },
            onDeleteEventClick = { ev -> viewModel.deleteEvent(ev) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentYear = displayedYear,
            currentMonth = displayedMonth,
            useNepaliNumerals = useNepaliNumerals,
            useNepaliLanguage = useNepaliLanguage,
            onDismiss = { showMonthPicker = false },
            onConfirm = { y, m -> viewModel.setDisplayedMonthYear(y, m) }
        )
    }

    if (showAddEventDialog) {
        AddEditEventDialog(
            initialDate = selectedDate,
            useNepaliLanguage = useNepaliLanguage,
            useNepaliNumerals = useNepaliNumerals,
            onDismiss = { showAddEventDialog = false },
            onSave = { title, desc, cat, color, allDay ->
                viewModel.addEvent(title, desc, selectedDate, cat, color, allDay)
            }
        )
    }
}
