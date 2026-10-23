package com.example.nepalcalendar.ui.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.ConversionDirection
import com.example.nepalcalendar.MainViewModel
import com.example.nepalcalendar.calendar.engine.BsCalendarData
import com.example.nepalcalendar.calendar.engine.BsCalendarEngine
import com.example.nepalcalendar.calendar.engine.DateConversionResult
import com.example.nepalcalendar.calendar.engine.NepaliDate
import java.time.LocalDate

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("converter_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Direction Tabs
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            ) {
                TabRow(
                    selectedTabIndex = if (conversionDirection == ConversionDirection.BS_TO_AD) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Tab(
                        selected = conversionDirection == ConversionDirection.BS_TO_AD,
                        onClick = { viewModel.setConversionDirection(ConversionDirection.BS_TO_AD) },
                        text = {
                            Text(
                                text = if (useNepaliLanguage) "वि.सं. बाट ई.सं. (BS → AD)" else "BS to AD",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_bs_to_ad")
                    )
                    Tab(
                        selected = conversionDirection == ConversionDirection.AD_TO_BS,
                        onClick = { viewModel.setConversionDirection(ConversionDirection.AD_TO_BS) },
                        text = {
                            Text(
                                text = if (useNepaliLanguage) "ई.सं. बाट वि.सं. (AD → BS)" else "AD to BS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_ad_to_bs")
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
                                if (useNepaliLanguage) "विक्रम संवत् (वि.सं.) मिति छान्नुहोस्" else "Select Bikram Sambat (BS) Date"
                            } else {
                                if (useNepaliLanguage) "ग्रेगोरियन (ई.सं.) मिति छान्नुहोस्" else "Select Gregorian (AD) Date"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = { viewModel.swapConversionDirection() },
                            modifier = Modifier.testTag("swap_direction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Direction",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (conversionDirection == ConversionDirection.BS_TO_AD) {
                        // BS Date Pickers: Year, Month, Day
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Year Picker
                            NumberDropdownField(
                                label = if (useNepaliLanguage) "वर्ष (Year)" else "Year",
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
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.performConversion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("convert_action_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (useNepaliLanguage) "रूपान्तरण गर्नुहोस् (Convert)" else "Convert Date",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Result Card
        if (conversionResult != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ),
                    modifier = Modifier.testTag("conversion_result_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (useNepaliLanguage) "रूपान्तरण नतिजा (Result)" else "Conversion Result",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bikram Sambat Section
                        Column {
                            Text(
                                text = "Bikram Sambat (वि.सं.):",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = conversionResult.formattedBsNp,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = conversionResult.formattedBsEn,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gregorian Section
                        Column {
                            Text(
                                text = "Gregorian (ई.सं.):",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = conversionResult.formattedAd,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Copy and Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val text = "${conversionResult.formattedBsNp} = ${conversionResult.formattedAd}"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Nepali Date", text))
                                    Toast.makeText(context, if (useNepaliLanguage) "क्लिपबोर्डमा प्रतिलिपि गरियो" else "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_result_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (useNepaliLanguage) "प्रतिलिपि" else "Copy")
                            }

                            Button(
                                onClick = {
                                    val text = """
                                        🇳🇵 Nepal Calendar Pro Date Conversion
                                        Bikram Sambat: ${conversionResult.formattedBsNp} (${conversionResult.formattedBsEn})
                                        Gregorian: ${conversionResult.formattedAd}
                                    """.trimIndent()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Conversion Result"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_result_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (useNepaliLanguage) "सेयर" else "Share")
                            }
                        }
                    }
                }
            }
        }

        // Recent Conversion History
        val history = viewModel.recentConversions.value
        if (history.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (useNepaliLanguage) "हालैका रूपान्तरणहरू" else "Recent Conversions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            items(history) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                        Text(
                            text = if (item.isBsToAd) "BS → AD" else "AD → BS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
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
