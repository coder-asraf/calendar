package com.example.nepalcalendar.ui.settings

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.nepalcalendar.MainViewModel
import com.example.nepalcalendar.data.repository.FestivalRepository
import com.example.nepalcalendar.data.repository.NotificationPreferences
import com.example.nepalcalendar.data.repository.UserPreferences
import com.example.ui.theme.HolidayRed
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    preferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val isNepali = preferences.language == "np"
    val useNepaliNumerals = preferences.numeralSystem == "NEPALI"

    // Dedicated NotificationPreferences state holder observed from DataStore via ViewModel
    val notificationPreferences by viewModel.notificationPreferences.collectAsState()

    // Runtime permission launcher for Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setDateNotificationEnabled(true)
            Toast.makeText(
                context,
                if (isNepali) "मिति सूचना सक्रिय गरियो" else "Date notification enabled",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            viewModel.setDateNotificationEnabled(false)
            Toast.makeText(
                context,
                if (isNepali) "सूचना अनुमति अस्वीकृत भयो" else "Notification permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. NotificationPreferences Section with Toggle Switch
        item {
            SettingSectionCard(
                title = if (isNepali) "दैनिक मिति सूचना (Daily Date Notification)" else "Daily Date Notification",
                icon = Icons.Default.NotificationsActive
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Primary Toggle Switch for Daily Date Notification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isNepali) "दैनिक मिति सूचना सक्रिय गर्नुहोस्" else "Enable Daily Date Notification",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isNepali) "मोबाइलको नोटिफिकेसन बारमा आजको वि.सं. मिति, ई.सं. र चाडपर्व देखाउनुहोस्।" else "Show current Bikram Sambat date, English date & festivals in notification bar.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notificationPreferences.isEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.setDateNotificationEnabled(true)
                                    }
                                } else {
                                    viewModel.setDateNotificationEnabled(false)
                                }
                            },
                            modifier = Modifier.testTag("notification_toggle_switch")
                        )
                    }

                    if (notificationPreferences.isEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Live Mock Preview of the Notification
                        Text(
                            text = if (isNepali) "सूचनाको पूर्वावलोकन (Preview):" else "Notification Preview:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        NotificationMockPreview(
                            viewModel = viewModel,
                            notificationPreferences = notificationPreferences,
                            userPreferences = preferences,
                            isNepali = isNepali,
                            useNepaliNumerals = useNepaliNumerals
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Notification Style: Ongoing Status Bar vs Daily Alert
                        Text(
                            text = if (isNepali) "सूचना शैली (Style):" else "Notification Style:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = notificationPreferences.isOngoing,
                                onClick = { viewModel.setNotificationOngoing(true) },
                                label = {
                                    Text(
                                        if (isNepali) "पिन गरिएको (Ongoing)" else "Ongoing (Pinned)",
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !notificationPreferences.isOngoing,
                                onClick = { viewModel.setNotificationOngoing(false) },
                                label = {
                                    Text(
                                        if (isNepali) "दैनिक सूचना (Daily Alert)" else "Daily Alert",
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Content Customization Options
                        Text(
                            text = if (isNepali) "सूचनामा समावेश गर्ने सामग्री (Content Options):" else "Content Customization:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Toggle: Include Gregorian Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isNepali) "अंग्रेजी मिति देखाउने (Include Gregorian)" else "Include Gregorian (AD) Date",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = notificationPreferences.includeGregorian,
                                onCheckedChange = { viewModel.setNotificationIncludeGregorian(it) }
                            )
                        }

                        // Toggle: Include Festivals & Holidays
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isNepali) "चाडपर्व तथा बिदा देखाउने (Include Festivals)" else "Include Festivals & Holidays",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = notificationPreferences.includeFestivals,
                                onCheckedChange = { viewModel.setNotificationIncludeFestivals(it) }
                            )
                        }

                        // Schedule Time Selector (if Daily Alert chosen)
                        if (!notificationPreferences.isOngoing) {
                            Text(
                                text = if (isNepali) "सूचना आउने समय (Schedule Time):" else "Alert Time:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val times = listOf(Pair(6, 0), Pair(7, 0), Pair(8, 0), Pair(9, 0))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                times.forEach { (h, m) ->
                                    val isSelected = notificationPreferences.notificationHour == h
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setNotificationTime(h, m) },
                                        label = { Text("${h}:00 AM", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Live Test / Preview Action Button
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.previewDateNotification()
                                Toast.makeText(
                                    context,
                                    if (isNepali) "नोटिफिकेसन बारमा मिति पठाइयो!" else "Notification posted to status bar!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("preview_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isNepali) "अहिले सूचना परीक्षण गर्नुहोस् (Test Now)" else "Test Notification Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. App Language Card
        item {
            SettingSectionCard(
                title = if (isNepali) "भाषा (Language)" else "Language",
                icon = Icons.Default.Language
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = preferences.language == "np",
                        onClick = { viewModel.setLanguage("np") },
                        label = { Text("नेपाली (Nepali)", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = preferences.language == "en",
                        onClick = { viewModel.setLanguage("en") },
                        label = { Text("English", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Theme Mode Card
        item {
            SettingSectionCard(
                title = if (isNepali) "विषयवस्तु (Theme)" else "Theme",
                icon = Icons.Default.Brightness4
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = preferences.themeMode == "SYSTEM",
                        onClick = { viewModel.setThemeMode("SYSTEM") },
                        label = { Text(if (isNepali) "प्रणाली" else "System", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = preferences.themeMode == "LIGHT",
                        onClick = { viewModel.setThemeMode("LIGHT") },
                        label = { Text(if (isNepali) "उज्यालो" else "Light", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = preferences.themeMode == "DARK",
                        onClick = { viewModel.setThemeMode("DARK") },
                        label = { Text(if (isNepali) "अँध्यारो" else "Dark", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Numerals System Card
        item {
            SettingSectionCard(
                title = if (isNepali) "अङ्क प्रणाली (Numerals)" else "Numeral System",
                icon = Icons.Default.FormatListNumbered
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = preferences.numeralSystem == "NEPALI",
                        onClick = { viewModel.setNumeralSystem("NEPALI") },
                        label = { Text("नेपाली (१, २, ३)", fontWeight = FontWeight.Medium) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = preferences.numeralSystem == "ARABIC",
                        onClick = { viewModel.setNumeralSystem("ARABIC") },
                        label = { Text("English (1, 2, 3)", fontWeight = FontWeight.Medium) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. First Day of Week
        item {
            SettingSectionCard(
                title = if (isNepali) "हप्ताको पहिलो दिन" else "First Day of Week",
                icon = Icons.Default.CalendarViewMonth
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = preferences.firstDayOfWeek == 1,
                        onClick = { viewModel.setFirstDayOfWeek(1) },
                        label = { Text(if (isNepali) "आइतबार (Sunday)" else "Sunday", fontWeight = FontWeight.Medium) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = preferences.firstDayOfWeek == 2,
                        onClick = { viewModel.setFirstDayOfWeek(2) },
                        label = { Text(if (isNepali) "सोमबार (Monday)" else "Monday", fontWeight = FontWeight.Medium) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. Saturday Holiday Highlight Toggle
        item {
            SettingSectionCard(
                title = if (isNepali) "शनिबार साप्ताहिक बिदा" else "Saturday Weekly Holiday",
                icon = Icons.Default.Palette
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isNepali) "नेपालको साप्ताहिक बिदा अनुसार शनिबार रातो रङमा देखाउने।" else "Highlight Saturdays in red per Nepal holiday convention.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = preferences.highlightSaturday,
                        onCheckedChange = { viewModel.setHighlightSaturday(it) }
                    )
                }
            }
        }

        // 7. Calendar Engine Information & Supported Range
        item {
            SettingSectionCard(
                title = if (isNepali) "क्यालेन्डर प्रणाली तथा शुद्धता" else "Calendar Accuracy & Range",
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isNepali) "समर्थित दायरा: १९७५ वि.सं. देखि २१०० वि.सं. (१२६ वर्ष)" else "Supported Range: 1975 BS to 2100 BS (126 years offline)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isNepali) "नेपाल पञ्चाङ्ग निर्णायक समितिद्वारा स्वीकृत वास्तविक महिनाका दिनहरूमा आधारित पूर्ण अफलाइन गणना।" else "Strictly calculated using verified annual month-day mappings from Nepal Panchanga Nirnayak Samiti tables without fixed Gregorian offsets.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Anchor Epoch: 1975-01-01 BS = 1918-04-13 AD",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // 8. Clear Personal Event Data
        item {
            SettingSectionCard(
                title = if (isNepali) "डाटा व्यवस्थापन" else "Data Management",
                icon = Icons.Default.DeleteSweep
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isNepali) "तपाईंको फोनमा सुरक्षित व्यक्तिगत कार्यक्रमहरू मेटाउनुहोस्।" else "Clear all user-created personal events saved in local Room database.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("clear_all_events_button")
                    ) {
                        Text(if (isNepali) "सबै व्यक्तिगत कार्यक्रम मेटाउनुहोस्" else "Clear All Personal Events")
                    }
                }
            }
        }

        // 9. About Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nepal Calendar Pro 🇳🇵",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Version 1.0.0 (Native Android · Jetpack Compose)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "100% Offline · Private · Fast · Accurate Bikram Sambat Engine",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(if (isNepali) "कार्यक्रम मेटाउने पुष्टि" else "Confirm Data Reset") },
            text = {
                Text(if (isNepali) "के तपाईं सबै व्यक्तिगत कार्यक्रमहरू मेटाउन चाहनुहुन्छ? यो प्रक्रिया पूर्ववत गर्न सकिँदैन।" else "Are you sure you want to delete all personal events? This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllEvents()
                        showClearConfirmDialog = false
                        Toast.makeText(context, if (isNepali) "सबै कार्यक्रमहरू मेटाइयो" else "All personal events cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isNepali) "मेटाउनुहोस्" else "Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotificationMockPreview(
    viewModel: MainViewModel,
    notificationPreferences: NotificationPreferences,
    userPreferences: UserPreferences,
    isNepali: Boolean,
    useNepaliNumerals: Boolean
) {
    val bsTitle = remember(viewModel.todayBs, isNepali, useNepaliNumerals) {
        viewModel.todayBs.formatDisplay(isNepali, useNepaliNumerals)
    }
    val adFormatted = remember(viewModel.todayAd) {
        viewModel.todayAd.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
    }
    val festivals = remember(viewModel.todayBs) {
        FestivalRepository.getFestivalsForDate(viewModel.todayBs)
    }
    val festivalText = if (festivals.isNotEmpty()) {
        festivals.joinToString(", ") { if (isNepali) it.nameNp else it.nameEn }
    } else if (viewModel.todayBs.isSaturday && userPreferences.highlightSaturday) {
        if (isNepali) "शनिबार (साप्ताहिक बिदा)" else "Saturday (Weekly Holiday)"
    } else ""

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🇳🇵",
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bsTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (notificationPreferences.includeGregorian) {
                    Text(
                        text = adFormatted,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (notificationPreferences.includeFestivals && festivalText.isNotEmpty()) {
                    Text(
                        text = festivalText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HolidayRed
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
