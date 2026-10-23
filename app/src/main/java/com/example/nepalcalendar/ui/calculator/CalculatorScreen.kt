package com.example.nepalcalendar.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.CalculatorMode
import com.example.nepalcalendar.MainViewModel
import com.example.nepalcalendar.calendar.engine.BsCalendarData
import com.example.nepalcalendar.calendar.engine.BsCalendarEngine
import com.example.nepalcalendar.calendar.engine.NepaliDate
import java.time.format.DateTimeFormatter

@Composable
fun CalculatorScreen(
    viewModel: MainViewModel,
    calculatorMode: CalculatorMode,
    todayBs: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector Tabs
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            ) {
                TabRow(
                    selectedTabIndex = calculatorMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = calculatorMode == CalculatorMode.DAYS_BETWEEN,
                        onClick = { viewModel.setCalculatorMode(CalculatorMode.DAYS_BETWEEN) },
                        text = { Text(if (useNepaliLanguage) "दिन अन्तर" else "Diff", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = calculatorMode == CalculatorMode.AGE,
                        onClick = { viewModel.setCalculatorMode(CalculatorMode.AGE) },
                        text = { Text(if (useNepaliLanguage) "उमेर" else "Age", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = calculatorMode == CalculatorMode.COUNTDOWN,
                        onClick = { viewModel.setCalculatorMode(CalculatorMode.COUNTDOWN) },
                        text = { Text(if (useNepaliLanguage) "काउन्टडाउन" else "Countdown", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = calculatorMode == CalculatorMode.ADD_SUBTRACT,
                        onClick = { viewModel.setCalculatorMode(CalculatorMode.ADD_SUBTRACT) },
                        text = { Text(if (useNepaliLanguage) "जोड/घटाउ" else "+ / - Days", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Active Tool Section
        item {
            when (calculatorMode) {
                CalculatorMode.DAYS_BETWEEN -> DaysBetweenTool(todayBs, useNepaliLanguage, useNepaliNumerals)
                CalculatorMode.AGE -> AgeCalculatorTool(todayBs, useNepaliLanguage, useNepaliNumerals)
                CalculatorMode.COUNTDOWN -> CountdownTool(todayBs, useNepaliLanguage, useNepaliNumerals)
                CalculatorMode.ADD_SUBTRACT -> AddSubtractDaysTool(todayBs, useNepaliLanguage, useNepaliNumerals)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DaysBetweenTool(
    todayBs: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean
) {
    var y1 by remember { mutableIntStateOf(todayBs.year) }
    var m1 by remember { mutableIntStateOf(todayBs.month) }
    var d1 by remember { mutableIntStateOf(todayBs.day) }

    var y2 by remember { mutableIntStateOf(todayBs.year) }
    var m2 by remember { mutableIntStateOf((todayBs.month % 12) + 1) }
    var d2 by remember { mutableIntStateOf(1) }

    var resultDays by remember { mutableStateOf<Long?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (useNepaliLanguage) "दुई वि.सं. मितिहरू बीचको दिन अन्तर" else "Days Between Two BS Dates",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(if (useNepaliLanguage) "पहिलो मिति (Date 1):" else "Date 1 (BS):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            DateInputRow(
                year = y1, month = m1, day = d1,
                onDateChanged = { y, m, d -> y1 = y; m1 = m; d1 = d },
                useNepaliNumerals = useNepaliNumerals
            )

            Text(if (useNepaliLanguage) "दोस्रो मिति (Date 2):" else "Date 2 (BS):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            DateInputRow(
                year = y2, month = m2, day = d2,
                onDateChanged = { y, m, d -> y2 = y; m2 = m; d2 = d },
                useNepaliNumerals = useNepaliNumerals
            )

            Button(
                onClick = {
                    val dt1 = NepaliDate(y1, m1, d1)
                    val dt2 = NepaliDate(y2, m2, d2)
                    resultDays = kotlin.math.abs(BsCalendarEngine.daysBetween(dt1, dt2))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("calc_diff_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (useNepaliLanguage) "अन्तर गणना गर्नुहोस्" else "Calculate Difference")
            }

            if (resultDays != null) {
                val formattedResult = if (useNepaliNumerals) NepaliDate.toNepaliDigits(resultDays!!) else resultDays!!.toString()
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (useNepaliLanguage) "जम्मा अन्तर:" else "Total Difference:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$formattedResult ${if (useNepaliLanguage) "दिन" else "days"}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgeCalculatorTool(
    todayBs: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean
) {
    var birthYear by remember { mutableIntStateOf(2055) }
    var birthMonth by remember { mutableIntStateOf(5) }
    var birthDay by remember { mutableIntStateOf(15) }

    var ageResult by remember { mutableStateOf<BsCalendarEngine.AgeResult?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (useNepaliLanguage) "वि.सं. उमेर गणना (Age Calculator)" else "Bikram Sambat Age Calculator",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(if (useNepaliLanguage) "जन्म मिति (Date of Birth - BS):" else "Date of Birth (BS):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            DateInputRow(
                year = birthYear, month = birthMonth, day = birthDay,
                onDateChanged = { y, m, d -> birthYear = y; birthMonth = m; birthDay = d },
                useNepaliNumerals = useNepaliNumerals
            )

            Button(
                onClick = {
                    val bDate = NepaliDate(birthYear, birthMonth, birthDay)
                    if (bDate <= todayBs) {
                        ageResult = BsCalendarEngine.calculateAge(bDate, todayBs)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("calc_age_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (useNepaliLanguage) "उमेर गणना गर्नुहोस्" else "Calculate Age")
            }

            if (ageResult != null) {
                val res = ageResult!!
                val yStr = if (useNepaliNumerals) NepaliDate.toNepaliDigits(res.years) else res.years.toString()
                val mStr = if (useNepaliNumerals) NepaliDate.toNepaliDigits(res.months) else res.months.toString()
                val dStr = if (useNepaliNumerals) NepaliDate.toNepaliDigits(res.days) else res.days.toString()
                val totStr = if (useNepaliNumerals) NepaliDate.toNepaliDigits(res.totalDays) else res.totalDays.toString()
                val nextBStr = if (useNepaliNumerals) NepaliDate.toNepaliDigits(res.nextBirthdayInDays) else res.nextBirthdayInDays.toString()

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (useNepaliLanguage) "वर्तमान उमेर (Current Age):" else "Your Current Age:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (useNepaliLanguage) "$yStr वर्ष, $mStr महिना, $dStr दिन" else "$yStr years, $mStr months, $dStr days",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (useNepaliLanguage) "कुल दिनहरू:" else "Total Days Lived:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totStr ${if (useNepaliLanguage) "दिन" else "days"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (useNepaliLanguage) "अर्को जन्मदिन बाँकी:" else "Next Birthday in:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$nextBStr ${if (useNepaliLanguage) "दिन" else "days"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownTool(
    todayBs: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean
) {
    var targetYear by remember { mutableIntStateOf(todayBs.year) }
    var targetMonth by remember { mutableIntStateOf(todayBs.month) }
    var targetDay by remember { mutableIntStateOf(todayBs.day) }
    var countdownResult by remember { mutableStateOf<Long?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (useNepaliLanguage) "मिति काउन्टडाउन (Countdown)" else "Target Date Countdown",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(if (useNepaliLanguage) "लक्ष्य मिति (Target Date - BS):" else "Target Date (BS):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            DateInputRow(
                year = targetYear, month = targetMonth, day = targetDay,
                onDateChanged = { y, m, d -> targetYear = y; targetMonth = m; targetDay = d },
                useNepaliNumerals = useNepaliNumerals
            )

            Button(
                onClick = {
                    val tDate = NepaliDate(targetYear, targetMonth, targetDay)
                    countdownResult = BsCalendarEngine.daysBetween(todayBs, tDate)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (useNepaliLanguage) "बाँकी दिन गणना गर्नुहोस्" else "Calculate Countdown")
            }

            if (countdownResult != null) {
                val days = countdownResult!!
                val dStr = if (useNepaliNumerals) NepaliDate.toNepaliDigits(kotlin.math.abs(days)) else kotlin.math.abs(days).toString()
                val message = when {
                    days > 0 -> if (useNepaliLanguage) "$dStr दिन बाँकी छ" else "$dStr days remaining"
                    days < 0 -> if (useNepaliLanguage) "$dStr दिन बितिसक्यो" else "$dStr days ago"
                    else -> if (useNepaliLanguage) "आज नै हो!" else "It is today!"
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = message,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSubtractDaysTool(
    todayBs: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean
) {
    var startYear by remember { mutableIntStateOf(todayBs.year) }
    var startMonth by remember { mutableIntStateOf(todayBs.month) }
    var startDay by remember { mutableIntStateOf(todayBs.day) }
    var daysDeltaInput by remember { mutableStateOf("30") }
    var isAdd by remember { mutableStateOf(true) }
    var calculatedDate by remember { mutableStateOf<NepaliDate?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (useNepaliLanguage) "मितिमा दिन थप वा घटाउ (+ / - Days)" else "Add or Subtract Days",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(if (useNepaliLanguage) "सुरुवाती मिति (Start Date - BS):" else "Start Date (BS):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            DateInputRow(
                year = startYear, month = startMonth, day = startDay,
                onDateChanged = { y, m, d -> startYear = y; startMonth = m; startDay = d },
                useNepaliNumerals = useNepaliNumerals
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = daysDeltaInput,
                    onValueChange = { daysDeltaInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text(if (useNepaliLanguage) "दिन संख्या" else "Days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { isAdd = !isAdd },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isAdd) "+ (Add)" else "- (Subtract)")
                }
            }

            Button(
                onClick = {
                    val delta = daysDeltaInput.toLongOrNull() ?: 0L
                    val finalDelta = if (isAdd) delta else -delta
                    val sDate = NepaliDate(startYear, startMonth, startDay)
                    runCatching {
                        calculatedDate = BsCalendarEngine.addDays(sDate, finalDelta)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (useNepaliLanguage) "नयाँ मिति पत्ता लगाउनुहोस्" else "Calculate Result Date")
            }

            if (calculatedDate != null) {
                val res = calculatedDate!!
                val adRes = res.toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (useNepaliLanguage) "परिणामी मिति (Result Date):" else "Result Date:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = res.formatDisplay(useNepaliLanguage, useNepaliNumerals),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = adRes,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateInputRow(
    year: Int,
    month: Int,
    day: Int,
    onDateChanged: (year: Int, month: Int, day: Int) -> Unit,
    useNepaliNumerals: Boolean
) {
    var yText by remember(year) { mutableStateOf(year.toString()) }
    var mText by remember(month) { mutableStateOf(month.toString()) }
    var dText by remember(day) { mutableStateOf(day.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = yText,
            onValueChange = {
                yText = it.filter { ch -> ch.isDigit() }.take(4)
                val y = yText.toIntOrNull()
                if (y != null && BsCalendarEngine.isBsYearValid(y)) {
                    val maxD = BsCalendarEngine.getDaysInMonth(y, month)
                    onDateChanged(y, month, day.coerceAtMost(maxD))
                }
            },
            label = { Text("Year (वर्ष)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1.3f)
        )

        OutlinedTextField(
            value = mText,
            onValueChange = {
                mText = it.filter { ch -> ch.isDigit() }.take(2)
                val m = mText.toIntOrNull()
                if (m != null && m in 1..12) {
                    val maxD = BsCalendarEngine.getDaysInMonth(year, m)
                    onDateChanged(year, m, day.coerceAtMost(maxD))
                }
            },
            label = { Text("Month (महिना)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1.1f)
        )

        OutlinedTextField(
            value = dText,
            onValueChange = {
                dText = it.filter { ch -> ch.isDigit() }.take(2)
                val d = dText.toIntOrNull()
                val maxD = BsCalendarEngine.getDaysInMonth(year, month)
                if (d != null && d in 1..maxD) {
                    onDateChanged(year, month, d)
                }
            },
            label = { Text("Day (गते)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
}
