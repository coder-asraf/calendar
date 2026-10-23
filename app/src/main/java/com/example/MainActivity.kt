package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.AppTab
import com.example.nepalcalendar.MainViewModel
import com.example.nepalcalendar.ui.calculator.CalculatorScreen
import com.example.nepalcalendar.ui.converter.ConverterScreen
import com.example.nepalcalendar.ui.events.EventsScreen
import com.example.nepalcalendar.ui.festivals.FestivalsScreen
import com.example.nepalcalendar.ui.home.HomeScreen
import com.example.nepalcalendar.ui.home.SearchDialog
import com.example.nepalcalendar.ui.settings.SettingsScreen
import com.example.ui.theme.NepalCalendarTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.userPreferences.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val displayedYear by viewModel.displayedYear.collectAsState()
            val displayedMonth by viewModel.displayedMonth.collectAsState()
            val selectedDate by viewModel.selectedDate.collectAsState()
            val events by viewModel.allEvents.collectAsState()

            val useNepaliLanguage = preferences.language == "np"
            val useNepaliNumerals = preferences.numeralSystem == "NEPALI"

            var showSearchDialog by remember { mutableStateOf(false) }

            // Handle back navigation to always return to Calendar home tab
            BackHandler(enabled = currentTab != AppTab.CALENDAR) {
                viewModel.setTab(AppTab.CALENDAR)
            }

            // Update status bar date notification if enabled
            androidx.compose.runtime.LaunchedEffect(
                preferences.dateNotificationEnabled,
                preferences.notificationOngoing,
                preferences.notificationIncludeGregorian,
                preferences.notificationIncludeFestivals
            ) {
                if (preferences.dateNotificationEnabled) {
                    com.example.nepalcalendar.notifications.CalendarNotificationManager.showOrUpdateDateNotification(
                        this@MainActivity,
                        preferences,
                        viewModel.todayBs,
                        viewModel.todayAd
                    )
                }
            }

            NepalCalendarTheme(themeMode = preferences.themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentTab) {
                                        AppTab.CALENDAR -> if (useNepaliLanguage) "नेपाल क्यालेन्डर प्रो" else "Nepal Calendar Pro"
                                        AppTab.CONVERTER -> if (useNepaliLanguage) "मिति रूपान्तरण (BS ↔ AD)" else "Date Converter"
                                        AppTab.FESTIVALS -> if (useNepaliLanguage) "चाडपर्व तथा बिदाहरू" else "Festivals & Holidays"
                                        AppTab.EVENTS -> if (useNepaliLanguage) "व्यक्तिगत कार्यक्रमहरू" else "Personal Events"
                                        AppTab.CALCULATOR -> if (useNepaliLanguage) "मिति क्यालकुलेटर" else "Date Calculator"
                                        AppTab.SETTINGS -> if (useNepaliLanguage) "सेटिङहरू" else "Settings"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { showSearchDialog = true },
                                    modifier = Modifier.testTag("top_bar_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.setTab(AppTab.EVENTS) },
                                    modifier = Modifier.testTag("top_bar_events_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Events"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                actionIconContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.CALENDAR,
                                onClick = { viewModel.setTab(AppTab.CALENDAR) },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                                label = { Text(if (useNepaliLanguage) "क्यालेन्डर" else "Calendar", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_item_calendar")
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.CONVERTER,
                                onClick = { viewModel.setTab(AppTab.CONVERTER) },
                                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Converter") },
                                label = { Text(if (useNepaliLanguage) "रूपान्तरण" else "Converter", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_item_converter")
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.FESTIVALS,
                                onClick = { viewModel.setTab(AppTab.FESTIVALS) },
                                icon = { Icon(Icons.Default.Celebration, contentDescription = "Festivals") },
                                label = { Text(if (useNepaliLanguage) "चाडपर्व" else "Festivals", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_item_festivals")
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.CALCULATOR,
                                onClick = { viewModel.setTab(AppTab.CALCULATOR) },
                                icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                                label = { Text(if (useNepaliLanguage) "औजार" else "Tools", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_item_calculator")
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.SETTINGS,
                                onClick = { viewModel.setTab(AppTab.SETTINGS) },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text(if (useNepaliLanguage) "सेटिङ" else "Settings", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_item_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.CALENDAR -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    displayedYear = displayedYear,
                                    displayedMonth = displayedMonth,
                                    selectedDate = selectedDate,
                                    todayBs = viewModel.todayBs,
                                    events = events,
                                    useNepaliLanguage = useNepaliLanguage,
                                    useNepaliNumerals = useNepaliNumerals,
                                    firstDayOfWeek = preferences.firstDayOfWeek,
                                    highlightSaturday = preferences.highlightSaturday,
                                    onSearchClick = { showSearchDialog = true }
                                )
                            }
                            AppTab.CONVERTER -> {
                                val conversionDirection by viewModel.conversionDirection.collectAsState()
                                val conversionResult by viewModel.conversionResult.collectAsState()
                                val bsInputYear by viewModel.bsInputYear.collectAsState()
                                val bsInputMonth by viewModel.bsInputMonth.collectAsState()
                                val bsInputDay by viewModel.bsInputDay.collectAsState()
                                val adInputYear by viewModel.adInputYear.collectAsState()
                                val adInputMonth by viewModel.adInputMonth.collectAsState()
                                val adInputDay by viewModel.adInputDay.collectAsState()

                                ConverterScreen(
                                    viewModel = viewModel,
                                    conversionDirection = conversionDirection,
                                    conversionResult = conversionResult,
                                    bsYear = bsInputYear,
                                    bsMonth = bsInputMonth,
                                    bsDay = bsInputDay,
                                    adYear = adInputYear,
                                    adMonth = adInputMonth,
                                    adDay = adInputDay,
                                    useNepaliLanguage = useNepaliLanguage,
                                    useNepaliNumerals = useNepaliNumerals
                                )
                            }
                            AppTab.FESTIVALS -> {
                                FestivalsScreen(
                                    currentBsYear = displayedYear,
                                    todayBs = viewModel.todayBs,
                                    useNepaliLanguage = useNepaliLanguage,
                                    useNepaliNumerals = useNepaliNumerals
                                )
                            }
                            AppTab.EVENTS -> {
                                EventsScreen(
                                    viewModel = viewModel,
                                    events = events,
                                    todayBs = viewModel.todayBs,
                                    useNepaliLanguage = useNepaliLanguage,
                                    useNepaliNumerals = useNepaliNumerals
                                )
                            }
                            AppTab.CALCULATOR -> {
                                val calcMode by viewModel.calculatorMode.collectAsState()
                                CalculatorScreen(
                                    viewModel = viewModel,
                                    calculatorMode = calcMode,
                                    todayBs = viewModel.todayBs,
                                    useNepaliLanguage = useNepaliLanguage,
                                    useNepaliNumerals = useNepaliNumerals
                                )
                            }
                            AppTab.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    preferences = preferences
                                )
                            }
                        }
                    }

                    if (showSearchDialog) {
                        SearchDialog(
                            currentBsYear = displayedYear,
                            events = events,
                            useNepaliLanguage = useNepaliLanguage,
                            useNepaliNumerals = useNepaliNumerals,
                            onDismiss = { showSearchDialog = false },
                            onSelectDate = { date ->
                                viewModel.selectDate(date)
                                viewModel.setTab(AppTab.CALENDAR)
                            }
                        )
                    }
                }
            }
        }
    }
}
