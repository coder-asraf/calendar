package com.example.nepalcalendar.ui.festivals

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.calendar.model.Festival
import com.example.nepalcalendar.calendar.model.FestivalCategory
import com.example.nepalcalendar.data.repository.FestivalRepository
import com.example.ui.theme.HolidayRed
import java.time.format.DateTimeFormatter

@Composable
fun FestivalsScreen(
    currentBsYear: Int,
    todayBs: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FestivalCategory.ALL) }

    val allFestivalsForYear = remember(currentBsYear) {
        FestivalRepository.getAllFestivalsForYear(currentBsYear)
    }

    val filteredFestivals = remember(allFestivalsForYear, selectedCategory, searchQuery) {
        allFestivalsForYear.filter { fest ->
            val matchesCategory = when (selectedCategory) {
                FestivalCategory.ALL -> true
                FestivalCategory.PUBLIC_HOLIDAY -> fest.isPublicHoliday
                else -> fest.category == selectedCategory
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                fest.nameNp.contains(searchQuery, ignoreCase = true) ||
                fest.nameEn.contains(searchQuery, ignoreCase = true) ||
                fest.descriptionNp.contains(searchQuery, ignoreCase = true) ||
                fest.descriptionEn.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("festivals_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (useNepaliLanguage) "चाडपर्व वा बिदा खोज्नुहोस्..." else "Search festivals or holidays...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("festival_search_bar")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FestivalCategory.values().forEach { cat ->
                val title = if (useNepaliLanguage) cat.titleNp else cat.titleEn
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(title, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Festivals List
        if (filteredFestivals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (useNepaliLanguage) "कुनै चाडपर्व फेला परेन" else "No festivals found",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFestivals, key = { it.id }) { fest ->
                    FestivalCard(
                        festival = fest,
                        todayBs = todayBs,
                        useNepaliLanguage = useNepaliLanguage,
                        useNepaliNumerals = useNepaliNumerals
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun FestivalCard(
    festival: Festival,
    todayBs: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean
) {
    val festDate = remember(festival) {
        val y = if (festival.bsYear > 0) festival.bsYear else todayBs.year
        NepaliDate(y, festival.bsMonth, festival.bsDay)
    }

    val adDate = remember(festDate) { festDate.toLocalDate() }
    val adFormatted = remember(adDate) {
        adDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
    }

    val daysDiff = remember(todayBs, festDate) {
        festDate.toLocalDate().toEpochDay() - todayBs.toLocalDate().toEpochDay()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (useNepaliLanguage) festival.nameNp else festival.nameEn,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (useNepaliLanguage) festival.nameEn else festival.nameNp,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (festival.isPublicHoliday) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HolidayRed.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (useNepaliLanguage) "सार्वजनिक बिदा" else "Public Holiday",
                            color = HolidayRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = festDate.formatDisplay(useNepaliLanguage, useNepaliNumerals),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Days countdown chip
                if (daysDiff >= 0) {
                    val countdownText = if (daysDiff == 0L) {
                        if (useNepaliLanguage) "आज!" else "Today!"
                    } else {
                        if (useNepaliLanguage) {
                            "${if (useNepaliNumerals) NepaliDate.toNepaliDigits(daysDiff) else daysDiff} दिन बाँकी"
                        } else {
                            "In $daysDiff days"
                        }
                    }
                    Text(
                        text = countdownText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (daysDiff == 0L) HolidayRed else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = adFormatted,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val desc = if (useNepaliLanguage) festival.descriptionNp else festival.descriptionEn
            if (desc.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
