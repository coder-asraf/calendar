package com.example.nepalcalendar.ui.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.ConversionDirection
import com.example.nepalcalendar.MainViewModel
import com.example.nepalcalendar.calendar.engine.BsCalendarData
import com.example.nepalcalendar.calendar.engine.BsCalendarEngine
import com.example.nepalcalendar.calendar.engine.ConversionHistoryItem
import com.example.nepalcalendar.calendar.engine.DateConversionResult
import com.example.nepalcalendar.calendar.engine.DateRangeItem
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.data.repository.FestivalRepository
import com.example.ui.theme.HolidayRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ConverterMode {
    SINGLE_DATE,
    DATE_RANGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: MainViewModel,
    conversionDirection: ConversionDirection,
    conversionResult: DateConversionResult?,
    bsYear: Int,
    bsMonth: Int,
    bsDay: Int,
    adYear: Int,
    adMonth: Int,
    adDay: Int,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var converterMode by remember { mutableStateOf(ConverterMode.SINGLE_DATE) }
    var showDirectTextInput by remember { mutableStateOf(false) }
    var textInputQuery by remember { mutableStateOf("") }
    var textInputError by remember { mutableStateOf<String?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    val history by viewModel.recentConversions.collectAsState()

    // Date Range state
    var rangeDaysCount by remember { mutableIntStateOf(7) }

    val adMonthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val maxDaysForBs = remember(bsYear, bsMonth) {
        if (BsCalendarEngine.isBsYearValid(bsYear) && BsCalendarEngine.isBsMonthValid(bsMonth)) {
            BsCalendarEngine.getDaysInMonth(bsYear, bsMonth)
        } else 30
    }

    val maxDaysForAd = remember(adYear, adMonth) {
        runCatching { LocalDate.of(adYear, adMonth, 1).lengthOfMonth() }.getOrDefault(30)
    }

    // Quick presets
    val todayBs = remember { BsCalendarEngine.todayBs() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("converter_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector: Single Date vs Date Range
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            ) {
                TabRow(
                    selectedTabIndex = converterMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Tab(
                        selected = converterMode == ConverterMode.SINGLE_DATE,
                        onClick = { converterMode = ConverterMode.SINGLE_DATE },
                        text = {
                            Text(
                                text = if (useNepaliLanguage) "एकल मिति रूपान्तरण" else "Single Date Converter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_single_date")
                    )
                    Tab(
                        selected = converterMode == ConverterMode.DATE_RANGE,
                        onClick = { converterMode = ConverterMode.DATE_RANGE },
                        text = {
                            Text(
                                text = if (useNepaliLanguage) "अवधि रूपान्तरण (Range)" else "Date Range Schedule",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_date_range")
                    )
                }
            }
        }

        if (converterMode == ConverterMode.SINGLE_DATE) {
            // Direction Switcher: BS to AD or AD to BS
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabRow(
                            selectedTabIndex = if (conversionDirection == ConversionDirection.BS_TO_AD) 0 else 1,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        ) {
                            Tab(
                                selected = conversionDirection == ConversionDirection.BS_TO_AD,
                                onClick = { viewModel.setConversionDirection(ConversionDirection.BS_TO_AD) },
                                text = {
                                    Text(
                                        text = if (useNepaliLanguage) "वि.सं. → ई.सं. (BS to AD)" else "BS → AD",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.testTag("tab_bs_to_ad")
                            )
                            Tab(
                                selected = conversionDirection == ConversionDirection.AD_TO_BS,
                                onClick = { viewModel.setConversionDirection(ConversionDirection.AD_TO_BS) },
                                text = {
                                    Text(
                                        text = if (useNepaliLanguage) "ई.सं. → वि.सं. (AD to BS)" else "AD → BS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.testTag("tab_ad_to_bs")
                            )
                        }

                        IconButton(
                            onClick = { viewModel.swapConversionDirection() },
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .testTag("swap_direction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Direction",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Quick Date Presets Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (useNepaliLanguage) "द्रुत मिति छनोट (Quick Presets):" else "Quick Date Presets:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = if (useNepaliLanguage) "आज (Today)" else "Today",
                            onClick = {
                                viewModel.setDateFromPreset(todayBs)
                                viewModel.performConversion()
                            }
                        )
                        PresetChip(
                            label = if (useNepaliLanguage) "हिजो (Yesterday)" else "Yesterday",
                            onClick = {
                                val yest = todayBs.minusDays(1)
                                viewModel.setDateFromPreset(yest)
                                viewModel.performConversion()
                            }
                        )
                        PresetChip(
                            label = if (useNepaliLanguage) "भोलि (Tomorrow)" else "Tomorrow",
                            onClick = {
                                val tom = todayBs.plusDays(1)
                                viewModel.setDateFromPreset(tom)
                                viewModel.performConversion()
                            }
                        )
                        PresetChip(
                            label = if (useNepaliLanguage) "नयाँ वर्ष (१ बैशाख)" else "New Year (1 Baisakh)",
                            onClick = {
                                val newYear = NepaliDate(todayBs.year, 1, 1)
                                viewModel.setDateFromPreset(newYear)
                                viewModel.performConversion()
                            }
                        )
                        PresetChip(
                            label = if (useNepaliLanguage) "अङ्ग्रेजी नयाँ वर्ष (1 Jan)" else "Gregorian New Year (Jan 1)",
                            onClick = {
                                val currentAdYear = LocalDate.now().year
                                val jan1Ad = LocalDate.of(currentAdYear, 1, 1)
                                val jan1Bs = BsCalendarEngine.adToBs(jan1Ad)
                                viewModel.setDateFromPreset(jan1Bs)
                                viewModel.performConversion()
                            }
                        )
                    }
                }
            }

            // Input Selector Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (conversionDirection == ConversionDirection.BS_TO_AD) {
                                    if (useNepaliLanguage) "वि.सं. मिति छान्नुहोस् (Bikram Sambat)" else "Select Bikram Sambat (BS) Date"
                                } else {
                                    if (useNepaliLanguage) "ई.सं. मिति छान्नुहोस् (Gregorian)" else "Select Gregorian (AD) Date"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Toggle between Dropdowns and Direct Typing
                            TextButton(
                                onClick = {
                                    showDirectTextInput = !showDirectTextInput
                                    textInputError = null
                                }
                            ) {
                                Icon(
                                    imageVector = if (showDirectTextInput) Icons.Default.CalendarToday else Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showDirectTextInput) (if (useNepaliLanguage) "ड्रपडाउन" else "Pickers")
                                    else (if (useNepaliLanguage) "टाइप गर्नुहोस्" else "Type Date"),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (showDirectTextInput) {
                            // Direct Text Input Mode
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = textInputQuery,
                                    onValueChange = {
                                        textInputQuery = it
                                        textInputError = null
                                    },
                                    label = {
                                        Text(
                                            if (conversionDirection == ConversionDirection.BS_TO_AD)
                                                (if (useNepaliLanguage) "वि.सं. मिति (उदा: २०८३/०७/०६ वा 2083-07-06)" else "BS Date (e.g. 2083-07-06)")
                                            else
                                                (if (useNepaliLanguage) "ई.सं. मिति (उदा: 2026-10-23)" else "AD Date (e.g. 2026-10-23)")
                                        )
                                    },
                                    placeholder = {
                                        Text(if (conversionDirection == ConversionDirection.BS_TO_AD) "2083-07-06" else "2026-10-23")
                                    },
                                    isError = textInputError != null,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("direct_date_input")
                                )

                                if (textInputError != null) {
                                    Text(
                                        text = textInputError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (conversionDirection == ConversionDirection.BS_TO_AD) {
                                            val parsed = BsCalendarEngine.parseBsDate(textInputQuery)
                                            if (parsed != null) {
                                                viewModel.setBsInput(parsed.year, parsed.month, parsed.day)
                                                viewModel.performConversion()
                                                textInputError = null
                                            } else {
                                                textInputError = if (useNepaliLanguage)
                                                    "अमान्य वि.सं. मिति! ढाँचा: YYYY-MM-DD (१९७५ देखि २१०० सम्म)"
                                                else
                                                    "Invalid BS Date! Format: YYYY-MM-DD (1975 to 2100)"
                                            }
                                        } else {
                                            val parsed = BsCalendarEngine.parseAdDate(textInputQuery)
                                            if (parsed != null) {
                                                viewModel.setAdInput(parsed.year, parsed.monthValue, parsed.dayOfMonth)
                                                viewModel.performConversion()
                                                textInputError = null
                                            } else {
                                                textInputError = if (useNepaliLanguage)
                                                    "अमान्य ई.सं. मिति! ढाँचा: YYYY-MM-DD (1918 to 2043)"
                                                else
                                                    "Invalid AD Date! Format: YYYY-MM-DD"
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("direct_convert_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (useNepaliLanguage) "रूपान्तरण गर्नुहोस् (Convert)" else "Parse & Convert")
                                }
                            }
                        } else {
                            // Standard Picker Dropdowns
                            if (conversionDirection == ConversionDirection.BS_TO_AD) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Year Picker
                                    NumberDropdownField(
                                        label = if (useNepaliLanguage) "वर्ष" else "Year",
                                        selectedValue = bsYear,
                                        options = (BsCalendarData.MIN_BS_YEAR..BsCalendarData.MAX_BS_YEAR).toList(),
                                        displayFormatter = { if (useNepaliNumerals) NepaliDate.toNepaliDigits(it) else it.toString() },
                                        onSelected = { y ->
                                            val safeDay = bsDay.coerceAtMost(BsCalendarEngine.getDaysInMonth(y, bsMonth))
                                            viewModel.setBsInput(y, bsMonth, safeDay)
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        testTag = "bs_year_dropdown"
                                    )

                                    // Month Picker
                                    MonthDropdownField(
                                        label = if (useNepaliLanguage) "महिना" else "Month",
                                        selectedMonth = bsMonth,
                                        monthNames = if (useNepaliLanguage) NepaliDate.MONTH_NAMES_NP else NepaliDate.MONTH_NAMES_EN,
                                        onSelected = { m ->
                                            val safeDay = bsDay.coerceAtMost(BsCalendarEngine.getDaysInMonth(bsYear, m))
                                            viewModel.setBsInput(bsYear, m, safeDay)
                                        },
                                        modifier = Modifier.weight(1.5f),
                                        testTag = "bs_month_dropdown"
                                    )

                                    // Day Picker
                                    NumberDropdownField(
                                        label = if (useNepaliLanguage) "गते" else "Day",
                                        selectedValue = bsDay.coerceAtMost(maxDaysForBs),
                                        options = (1..maxDaysForBs).toList(),
                                        displayFormatter = { if (useNepaliNumerals) NepaliDate.toNepaliDigits(it) else it.toString() },
                                        onSelected = { d -> viewModel.setBsInput(bsYear, bsMonth, d) },
                                        modifier = Modifier.weight(1f),
                                        testTag = "bs_day_dropdown"
                                    )
                                }

                                // Day & Month Quick Steppers
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val curr = NepaliDate(bsYear, bsMonth, bsDay)
                                                val prev = curr.minusDays(1)
                                                viewModel.setBsInput(prev.year, prev.month, prev.day)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(if (useNepaliLanguage) "१ दिन" else "1 Day", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val curr = NepaliDate(bsYear, bsMonth, bsDay)
                                                val next = curr.plusDays(1)
                                                viewModel.setBsInput(next.year, next.month, next.day)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(if (useNepaliLanguage) "१ दिन" else "1 Day", fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.performConversion() },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("convert_action_button")
                                    ) {
                                        Text(
                                            text = if (useNepaliLanguage) "रूपान्तरण (Convert)" else "Convert",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // AD Date Pickers: Year, Month, Day
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Year Picker
                                    NumberDropdownField(
                                        label = "Year",
                                        selectedValue = adYear,
                                        options = (1920..2040).toList(),
                                        displayFormatter = { it.toString() },
                                        onSelected = { y ->
                                            val maxD = runCatching { LocalDate.of(y, adMonth, 1).lengthOfMonth() }.getOrDefault(30)
                                            viewModel.setAdInput(y, adMonth, adDay.coerceAtMost(maxD))
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        testTag = "ad_year_dropdown"
                                    )

                                    // Month Picker
                                    MonthDropdownField(
                                        label = "Month",
                                        selectedMonth = adMonth,
                                        monthNames = adMonthNames,
                                        onSelected = { m ->
                                            val maxD = runCatching { LocalDate.of(adYear, m, 1).lengthOfMonth() }.getOrDefault(30)
                                            viewModel.setAdInput(adYear, m, adDay.coerceAtMost(maxD))
                                        },
                                        modifier = Modifier.weight(1.5f),
                                        testTag = "ad_month_dropdown"
                                    )

                                    // Day Picker
                                    NumberDropdownField(
                                        label = "Day",
                                        selectedValue = adDay.coerceAtMost(maxDaysForAd),
                                        options = (1..maxDaysForAd).toList(),
                                        displayFormatter = { it.toString() },
                                        onSelected = { d -> viewModel.setAdInput(adYear, adMonth, d) },
                                        modifier = Modifier.weight(1f),
                                        testTag = "ad_day_dropdown"
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val curr = LocalDate.of(adYear, adMonth, adDay).minusDays(1)
                                                viewModel.setAdInput(curr.year, curr.monthValue, curr.dayOfMonth)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("1 Day", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val curr = LocalDate.of(adYear, adMonth, adDay).plusDays(1)
                                                viewModel.setAdInput(curr.year, curr.monthValue, curr.dayOfMonth)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("1 Day", fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.performConversion() },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("convert_action_button")
                                    ) {
                                        Text(
                                            text = if (useNepaliLanguage) "रूपान्तरण (Convert)" else "Convert",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Enhanced Result Card
            if (conversionResult != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ),
                        modifier = Modifier.testTag("conversion_result_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Header: Status badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (useNepaliLanguage) "रूपान्तरण नतिजा (Conversion Result)" else "Conversion Result",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Holiday/Weekend Tag
                                if (conversionResult.isWeekendHoliday) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = HolidayRed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (conversionResult.isSaturday)
                                                (if (useNepaliLanguage) "शनिबार (साप्ताहिक बिदा)" else "Saturday Holiday")
                                            else
                                                (if (useNepaliLanguage) "आइतबार (साप्ताहिक बिदा)" else "Sunday Holiday"),
                                            color = HolidayRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else if (conversionResult.isPublicHoliday) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = HolidayRed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (useNepaliLanguage) "सार्वजनिक बिदा" else "Public Holiday",
                                            color = HolidayRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bikram Sambat Section
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Bikram Sambat (वि.सं.):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = conversionResult.formattedBsNp,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = conversionResult.formattedBsEn,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (conversionResult.seasonNp.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = if (useNepaliLanguage) conversionResult.seasonNp else conversionResult.seasonEn,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (conversionResult.dayOfYearBs > 0) {
                                                val daysLeft = conversionResult.totalDaysInBsYear - conversionResult.dayOfYearBs
                                                Text(
                                                    text = if (useNepaliLanguage)
                                                        "दिन ${NepaliDate.toNepaliDigits(conversionResult.dayOfYearBs)} / बाँकी ${NepaliDate.toNepaliDigits(daysLeft)}"
                                                    else
                                                        "Day ${conversionResult.dayOfYearBs} / ${daysLeft} days left",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Gregorian Section
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Gregorian (ई.सं.):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = conversionResult.formattedAd,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "ISO: ${conversionResult.adDate} · Day ${conversionResult.dayOfYearAd} of ${conversionResult.adDate.lengthOfYear()}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Festivals on this date if any
                            if (conversionResult.festivalNames.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "चाडपर्व: " + conversionResult.festivalNames.joinToString(", "),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (conversionResult.isPublicHoliday) HolidayRed else MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons Row: Copy, Share, View in Calendar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val text = "${conversionResult.formattedBsNp} = ${conversionResult.formattedAd}"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Nepali Date", text))
                                        Toast.makeText(context, if (useNepaliLanguage) "क्लिपबोर्डमा प्रतिलिपि गरियो" else "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).testTag("copy_result_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (useNepaliLanguage) "प्रतिलिपि" else "Copy", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val shareText = """
                                            🇳🇵 Nepal Calendar Pro Conversion
                                            वि.सं. (BS): ${conversionResult.formattedBsNp} (${conversionResult.formattedBsEn})
                                            ई.सं. (AD): ${conversionResult.formattedAd}
                                            ऋतु: ${conversionResult.seasonNp}
                                        """.trimIndent()
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Date"))
                                    },
                                    modifier = Modifier.weight(1f).testTag("share_result_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (useNepaliLanguage) "सेयर" else "Share", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.jumpToDateInCalendar(conversionResult.bsDate)
                                    },
                                    modifier = Modifier.weight(1.1f).testTag("view_in_cal_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (useNepaliLanguage) "क्यालेन्डर" else "Calendar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Recent Conversion History Section
            if (history.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (useNepaliLanguage) "हालैका रूपान्तरणहरू (${history.size})" else "Recent Conversions (${history.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        TextButton(
                            onClick = { showClearHistoryDialog = true }
                        ) {
                            Text(
                                text = if (useNepaliLanguage) "सबै मेटाउनुहोस्" else "Clear All",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                items(history) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.loadHistoryItem(item)
                                viewModel.performConversion()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.sourceFormatted,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.targetFormatted,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = if (item.isBsToAd) "BS → AD" else "AD → BS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeConversionHistoryItem(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // DATE RANGE CONVERTER MODE
            item {
                DateRangeConverterSection(
                    viewModel = viewModel,
                    bsYear = bsYear,
                    bsMonth = bsMonth,
                    bsDay = bsDay,
                    rangeDaysCount = rangeDaysCount,
                    onRangeDaysCountChange = { rangeDaysCount = it },
                    useNepaliLanguage = useNepaliLanguage,
                    useNepaliNumerals = useNepaliNumerals
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Confirmation dialog for clearing conversion history
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(if (useNepaliLanguage) "इतिहास मेटाउने?" else "Clear History?") },
            text = { Text(if (useNepaliLanguage) "के तपाईं सबै हालैका रूपान्तरणहरू मेटाउन चाहनुहुन्छ?" else "Are you sure you want to delete all saved conversion history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConversionHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(if (useNepaliLanguage) "मेटाउनुहोस्" else "Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(if (useNepaliLanguage) "रद्द" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DateRangeConverterSection(
    viewModel: MainViewModel,
    bsYear: Int,
    bsMonth: Int,
    bsDay: Int,
    rangeDaysCount: Int,
    onRangeDaysCountChange: (Int) -> Unit,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean
) {
    val context = LocalContext.current
    val startBsDate = remember(bsYear, bsMonth, bsDay) {
        val safeMax = BsCalendarEngine.getDaysInMonth(bsYear, bsMonth)
        NepaliDate(bsYear, bsMonth, bsDay.coerceIn(1, safeMax))
    }

    // Generate range items
    val rangeItems = remember(startBsDate, rangeDaysCount) {
        List(rangeDaysCount) { offset ->
            val currBs = startBsDate.plusDays(offset.toLong())
            val currAd = currBs.toLocalDate()
            val fests = FestivalRepository.getFestivalsForDate(currBs)
            val festName = if (fests.isNotEmpty()) fests.first().nameNp else ""
            val isHoliday = currBs.isWeekendHoliday || fests.any { it.isPublicHoliday }
            DateRangeItem(
                bsDate = currBs,
                adDate = currAd,
                dayOfWeekNameNp = currBs.dayOfWeekNameNp,
                dayOfWeekNameEn = currBs.dayOfWeekNameEn,
                isWeekendHoliday = currBs.isWeekendHoliday,
                festivalName = festName,
                isPublicHoliday = isHoliday
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (useNepaliLanguage) "अवधि रूपान्तरण तालिका (Multi-Day Schedule)" else "Date Range Converter Schedule",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (useNepaliLanguage) "सुरुवात मिति: ${startBsDate.formatDisplay(useNepaliLanguage, useNepaliNumerals)}"
                    else "Starting from: ${startBsDate.formatDisplay(useNepaliLanguage, useNepaliNumerals)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (useNepaliLanguage) "अवधि (दिनहरू):" else "Duration (Days):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7, 15, 30, 60).forEach { days ->
                        FilterChip(
                            selected = rangeDaysCount == days,
                            onClick = { onRangeDaysCountChange(days) },
                            label = {
                                Text(
                                    text = if (useNepaliLanguage) "${NepaliDate.toNepaliDigits(days)} दिन" else "$days Days",
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val sb = StringBuilder()
                        sb.append("🇳🇵 Nepal Calendar Date Range Schedule\n")
                        rangeItems.forEach { item ->
                            val holidayNotice = if (item.isWeekendHoliday) " [Holiday]" else if (item.festivalName.isNotEmpty()) " [${item.festivalName}]" else ""
                            sb.append("${item.bsDate.formatShort(useNepaliNumerals)} (${item.dayOfWeekNameNp}) = ${item.adDate}$holidayNotice\n")
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Date Range Schedule", sb.toString()))
                        Toast.makeText(context, if (useNepaliLanguage) "तालिका क्लिपबोर्डमा प्रतिलिपि गरियो" else "Range copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (useNepaliLanguage) "तालिका प्रतिलिपि गर्नुहोस् (Copy Schedule)" else "Copy Entire Schedule")
                }
            }
        }

        // List of days in range
        Text(
            text = if (useNepaliLanguage) "मितिहरूको सूची (${rangeItems.size} दिन):" else "Converted Dates (${rangeItems.size} days):",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        rangeItems.forEach { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isWeekendHoliday || item.isPublicHoliday) {
                        HolidayRed.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (item.isWeekendHoliday || item.isPublicHoliday) HolidayRed.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.bsDate.formatDisplay(useNepaliLanguage, useNepaliNumerals),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (item.isWeekendHoliday || item.isPublicHoliday) HolidayRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AD: ${item.adDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy"))}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (item.isWeekendHoliday) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HolidayRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (item.bsDate.isSaturday) (if (useNepaliLanguage) "शनिबार" else "Sat")
                                else (if (useNepaliLanguage) "आइतबार" else "Sun"),
                                color = HolidayRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (item.festivalName.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = item.festivalName,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberDropdownField(
    label: String,
    selectedValue: Int,
    options: List<Int>,
    displayFormatter: (Int) -> String,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.testTag(testTag)
    ) {
        OutlinedTextField(
            value = displayFormatter(selectedValue),
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.height(240.dp)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(displayFormatter(opt)) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDropdownField(
    label: String,
    selectedMonth: Int,
    monthNames: List<String>,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    val currentMonthName = monthNames.getOrElse(selectedMonth - 1) { "" }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.testTag(testTag)
    ) {
        OutlinedTextField(
            value = currentMonthName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.height(240.dp)
        ) {
            monthNames.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(index + 1)
                        expanded = false
                    }
                )
            }
        }
    }
}
