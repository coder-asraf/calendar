package com.example.nepalcalendar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.calendar.engine.BsCalendarEngine
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.calendar.model.Festival
import com.example.nepalcalendar.data.local.EventEntity
import com.example.ui.theme.HolidayRed
import java.time.DayOfWeek
import java.time.LocalDate

data class CalendarDayModel(
    val dayNumber: Int,
    val bsDate: NepaliDate?,
    val adDate: LocalDate?,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isSaturday: Boolean,
    val isSunday: Boolean = false,
    val isWeekendHoliday: Boolean = isSaturday || isSunday,
    val isPublicHoliday: Boolean,
    val hasPersonalEvent: Boolean,
    val festivalNames: List<String>
)

@Composable
fun CalendarGrid(
    year: Int,
    month: Int,
    selectedDate: NepaliDate,
    todayBs: NepaliDate,
    festivalsForMonth: List<Festival>,
    eventsForMonth: List<EventEntity>,
    useNepaliNumerals: Boolean,
    useNepaliLanguage: Boolean,
    firstDayOfWeek: Int, // 1 = Sunday, 2 = Monday
    highlightSaturday: Boolean,
    onDateSelected: (NepaliDate) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = remember(year, month) {
        BsCalendarEngine.getDaysInMonth(year, month)
    }

    // Determine the day of week of the first day of this BS month
    val firstDayBs = remember(year, month) {
        NepaliDate(year, month, 1)
    }
    val firstDayAd = remember(firstDayBs) {
        firstDayBs.toLocalDate()
    }

    // Weekday headers based on firstDayOfWeek (Sunday=1 or Monday=2)
    val weekdayHeaders = remember(useNepaliLanguage, firstDayOfWeek) {
        if (firstDayOfWeek == 1) { // Sunday first
            if (useNepaliLanguage) listOf("आइत", "सोम", "मंगल", "बुध", "बिही", "शुक्र", "शनि")
            else listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        } else { // Monday first
            if (useNepaliLanguage) listOf("सोम", "मंगल", "बुध", "बिही", "शुक्र", "शनि", "आइत")
            else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        }
    }

    // Offset in the first week
    val startDayOffset = remember(firstDayAd.dayOfWeek, firstDayOfWeek) {
        val dow = firstDayAd.dayOfWeek
        if (firstDayOfWeek == 1) { // Sunday = 0, Monday = 1, ..., Saturday = 6
            when (dow) {
                DayOfWeek.SUNDAY -> 0
                DayOfWeek.MONDAY -> 1
                DayOfWeek.TUESDAY -> 2
                DayOfWeek.WEDNESDAY -> 3
                DayOfWeek.THURSDAY -> 4
                DayOfWeek.FRIDAY -> 5
                DayOfWeek.SATURDAY -> 6
            }
        } else { // Monday = 0, ..., Sunday = 6
            dow.value - 1
        }
    }

    // Prepare grid items (total 35 or 42 cells)
    val totalCells = if (startDayOffset + daysInMonth > 35) 42 else 35
    val daysList = remember(year, month, selectedDate, todayBs, festivalsForMonth, eventsForMonth) {
        List(totalCells) { index ->
            val dayNum = index - startDayOffset + 1
            if (dayNum in 1..daysInMonth) {
                val bsDate = NepaliDate(year, month, dayNum)
                val adDate = bsDate.toLocalDate()
                val isSaturday = bsDate.isSaturday
                val isSunday = bsDate.isSunday
                val isWeekendHoliday = bsDate.isWeekendHoliday
                val isToday = (bsDate == todayBs)
                val isSelected = (bsDate == selectedDate)
                val fests = festivalsForMonth.filter { it.bsDay == dayNum }
                val hasHoliday = fests.any { it.isPublicHoliday }
                val hasEvent = eventsForMonth.any { it.bsDay == dayNum }
                CalendarDayModel(
                    dayNumber = dayNum,
                    bsDate = bsDate,
                    adDate = adDate,
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSelected = isSelected,
                    isSaturday = isSaturday,
                    isSunday = isSunday,
                    isWeekendHoliday = isWeekendHoliday,
                    isPublicHoliday = hasHoliday,
                    hasPersonalEvent = hasEvent,
                    festivalNames = fests.map { if (useNepaliLanguage) it.nameNp else it.nameEn }
                )
            } else {
                CalendarDayModel(
                    dayNumber = 0,
                    bsDate = null,
                    adDate = null,
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false,
                    isSaturday = false,
                    isSunday = false,
                    isWeekendHoliday = false,
                    isPublicHoliday = false,
                    hasPersonalEvent = false,
                    festivalNames = emptyList()
                )
            }
        }
    }

    // Swipe gesture tracking
    var dragAmount = remember { 0f }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(year, month) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAmount = 0f },
                    onHorizontalDrag = { _, drag -> dragAmount += drag },
                    onDragEnd = {
                        if (dragAmount < -60f) {
                            onSwipeLeft() // Next month
                        } else if (dragAmount > 60f) {
                            onSwipeRight() // Previous month
                        }
                    }
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("calendar_grid")
    ) {
        // Weekday header row: highlight both Saturday and Sunday when weekend holiday highlight is on
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdayHeaders.forEachIndexed { idx, header ->
                val isSatHeader = if (firstDayOfWeek == 1) idx == 6 else idx == 5
                val isSunHeader = if (firstDayOfWeek == 1) idx == 0 else idx == 6
                val isWeekendHeader = isSatHeader || isSunHeader
                Text(
                    text = header,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isWeekendHeader && highlightSaturday) HolidayRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weeks rows (5 or 6 rows of 7 days)
        val rows = totalCells / 7
        for (r in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val cellData = daysList[cellIndex]
                    CalendarDayCell(
                        cellData = cellData,
                        useNepaliNumerals = useNepaliNumerals,
                        highlightSaturday = highlightSaturday,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    cellData: CalendarDayModel,
    useNepaliNumerals: Boolean,
    highlightSaturday: Boolean,
    onDateSelected: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!cellData.isCurrentMonth || cellData.bsDate == null) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .padding(2.dp)
        )
        return
    }

    val bsDayText = remember(cellData.dayNumber, useNepaliNumerals) {
        if (useNepaliNumerals) NepaliDate.toNepaliDigits(cellData.dayNumber)
        else cellData.dayNumber.toString()
    }

    val adDayText = remember(cellData.adDate) {
        cellData.adDate?.dayOfMonth?.toString() ?: ""
    }

    val isHighlightRed = (cellData.isWeekendHoliday && highlightSaturday) || cellData.isPublicHoliday

    val cellBackground = when {
        cellData.isSelected -> MaterialTheme.colorScheme.primary
        cellData.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val contentColor = when {
        cellData.isSelected -> MaterialTheme.colorScheme.onPrimary
        isHighlightRed -> HolidayRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    val secondaryTextColor = when {
        cellData.isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
        isHighlightRed -> HolidayRed.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cellBackground)
            .then(
                if (cellData.isToday && !cellData.isSelected) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                } else Modifier
            )
            .clickable { onDateSelected(cellData.bsDate) }
            .testTag("day_cell_${cellData.dayNumber}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bikram Sambat day number
            Text(
                text = bsDayText,
                fontSize = 15.sp,
                fontWeight = if (cellData.isSelected || cellData.isToday) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )

            // Gregorian day number below
            Text(
                text = adDayText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryTextColor
            )

            // Event / Festival dots row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                if (cellData.isPublicHoliday || cellData.festivalNames.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (cellData.isSelected) MaterialTheme.colorScheme.onPrimary else HolidayRed)
                    )
                }
                if (cellData.hasPersonalEvent) {
                    Spacer(modifier = Modifier.size(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (cellData.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
        }
    }
}
