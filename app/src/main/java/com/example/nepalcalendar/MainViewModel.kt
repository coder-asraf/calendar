package com.example.nepalcalendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalcalendar.calendar.engine.BsCalendarEngine
import com.example.nepalcalendar.calendar.engine.ConversionHistoryItem
import com.example.nepalcalendar.calendar.engine.DateConversionResult
import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.calendar.model.Festival
import com.example.nepalcalendar.calendar.model.FestivalCategory
import com.example.nepalcalendar.data.local.AppDatabase
import com.example.nepalcalendar.data.local.EventEntity
import com.example.nepalcalendar.data.repository.EventRepository
import com.example.nepalcalendar.data.repository.FestivalRepository
import com.example.nepalcalendar.data.repository.PreferencesRepository
import com.example.nepalcalendar.data.repository.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class AppTab {
    CALENDAR,
    CONVERTER,
    FESTIVALS,
    EVENTS,
    CALCULATOR,
    SETTINGS
}

enum class ConversionDirection {
    BS_TO_AD,
    AD_TO_BS
}

enum class CalculatorMode {
    DAYS_BETWEEN,
    COUNTDOWN,
    AGE,
    ADD_SUBTRACT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val eventRepository = EventRepository(db.eventDao())
    private val preferencesRepository = PreferencesRepository(application)
    private val notificationPreferencesRepository = com.example.nepalcalendar.data.repository.NotificationPreferencesRepository(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.CALENDAR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Preferences
    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    // Dedicated Notification Preferences State Holder
    val notificationPreferences: StateFlow<com.example.nepalcalendar.data.repository.NotificationPreferences> =
        notificationPreferencesRepository.notificationPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = com.example.nepalcalendar.data.repository.NotificationPreferences()
            )

    // Today's dates (deterministic and calculated from system clock)
    val todayAd: LocalDate = LocalDate.now()
    val todayBs: NepaliDate = runCatching { BsCalendarEngine.adToBs(todayAd) }
        .getOrDefault(NepaliDate(2083, 7, 6))

    // Calendar state
    private val _displayedYear = MutableStateFlow(todayBs.year)
    val displayedYear: StateFlow<Int> = _displayedYear.asStateFlow()

    private val _displayedMonth = MutableStateFlow(todayBs.month)
    val displayedMonth: StateFlow<Int> = _displayedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(todayBs)
    val selectedDate: StateFlow<NepaliDate> = _selectedDate.asStateFlow()

    // Events flow
    val allEvents: StateFlow<List<EventEntity>> = eventRepository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search query & results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Converter state
    private val _conversionDirection = MutableStateFlow(ConversionDirection.BS_TO_AD)
    val conversionDirection: StateFlow<ConversionDirection> = _conversionDirection.asStateFlow()

    // BS inputs
    private val _bsInputYear = MutableStateFlow(todayBs.year)
    val bsInputYear: StateFlow<Int> = _bsInputYear.asStateFlow()

    private val _bsInputMonth = MutableStateFlow(todayBs.month)
    val bsInputMonth: StateFlow<Int> = _bsInputMonth.asStateFlow()

    private val _bsInputDay = MutableStateFlow(todayBs.day)
    val bsInputDay: StateFlow<Int> = _bsInputDay.asStateFlow()

    // AD inputs
    private val _adInputYear = MutableStateFlow(todayAd.year)
    val adInputYear: StateFlow<Int> = _adInputYear.asStateFlow()

    private val _adInputMonth = MutableStateFlow(todayAd.monthValue)
    val adInputMonth: StateFlow<Int> = _adInputMonth.asStateFlow()

    private val _adInputDay = MutableStateFlow(todayAd.dayOfMonth)
    val adInputDay: StateFlow<Int> = _adInputDay.asStateFlow()

    // Conversion result
    private val _conversionResult = MutableStateFlow<DateConversionResult?>(null)
    val conversionResult: StateFlow<DateConversionResult?> = _conversionResult.asStateFlow()

    // Recent conversion history
    private val _recentConversions = MutableStateFlow<List<ConversionHistoryItem>>(emptyList())
    val recentConversions: StateFlow<List<ConversionHistoryItem>> = _recentConversions.asStateFlow()

    // Calculator state
    private val _calculatorMode = MutableStateFlow(CalculatorMode.DAYS_BETWEEN)
    val calculatorMode: StateFlow<CalculatorMode> = _calculatorMode.asStateFlow()

    private val _calcResult = MutableStateFlow<String?>(null)
    val calcResult: StateFlow<String?> = _calcResult.asStateFlow()

    // Festival category filter
    private val _festivalCategoryFilter = MutableStateFlow(FestivalCategory.ALL)
    val festivalCategoryFilter: StateFlow<FestivalCategory> = _festivalCategoryFilter.asStateFlow()

    init {
        // Run initial conversion for converter screen
        performConversion()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun goToToday() {
        _displayedYear.value = todayBs.year
        _displayedMonth.value = todayBs.month
        _selectedDate.value = todayBs
    }

    fun selectDate(date: NepaliDate) {
        _selectedDate.value = date
        _displayedYear.value = date.year
        _displayedMonth.value = date.month
    }

    fun nextMonth() {
        val (nextY, nextM) = BsCalendarEngine.getNextMonth(_displayedYear.value, _displayedMonth.value)
        if (BsCalendarEngine.isBsYearValid(nextY)) {
            _displayedYear.value = nextY
            _displayedMonth.value = nextM
            // Adjust selected date if needed
            val maxDays = BsCalendarEngine.getDaysInMonth(nextY, nextM)
            val newDay = _selectedDate.value.day.coerceAtMost(maxDays)
            _selectedDate.value = NepaliDate(nextY, nextM, newDay)
        }
    }

    fun previousMonth() {
        val (prevY, prevM) = BsCalendarEngine.getPreviousMonth(_displayedYear.value, _displayedMonth.value)
        if (BsCalendarEngine.isBsYearValid(prevY)) {
            _displayedYear.value = prevY
            _displayedMonth.value = prevM
            val maxDays = BsCalendarEngine.getDaysInMonth(prevY, prevM)
            val newDay = _selectedDate.value.day.coerceAtMost(maxDays)
            _selectedDate.value = NepaliDate(prevY, prevM, newDay)
        }
    }

    fun setDisplayedMonthYear(year: Int, month: Int) {
        if (BsCalendarEngine.isBsYearValid(year) && BsCalendarEngine.isBsMonthValid(month)) {
            _displayedYear.value = year
            _displayedMonth.value = month
            val maxDays = BsCalendarEngine.getDaysInMonth(year, month)
            val newDay = _selectedDate.value.day.coerceAtMost(maxDays)
            _selectedDate.value = NepaliDate(year, month, newDay)
        }
    }

    // Converter actions
    fun setConversionDirection(direction: ConversionDirection) {
        _conversionDirection.value = direction
        performConversion()
    }

    fun swapConversionDirection() {
        _conversionDirection.value = if (_conversionDirection.value == ConversionDirection.BS_TO_AD) {
            ConversionDirection.AD_TO_BS
        } else {
            ConversionDirection.BS_TO_AD
        }
        performConversion()
    }

    fun setBsInput(year: Int, month: Int, day: Int) {
        _bsInputYear.value = year
        _bsInputMonth.value = month
        _bsInputDay.value = day
        performConversion()
    }

    fun setAdInput(year: Int, month: Int, day: Int) {
        _adInputYear.value = year
        _adInputMonth.value = month
        _adInputDay.value = day
        performConversion()
    }

    fun performConversion() {
        try {
            if (_conversionDirection.value == ConversionDirection.BS_TO_AD) {
                val y = _bsInputYear.value
                val m = _bsInputMonth.value
                val maxDays = BsCalendarEngine.getDaysInMonth(y, m)
                val d = _bsInputDay.value.coerceIn(1, maxDays)
                val bsDate = NepaliDate(y, m, d)
                val adDate = BsCalendarEngine.bsToAd(bsDate)

                val res = DateConversionResult(
                    bsDate = bsDate,
                    adDate = adDate,
                    isBsToAd = true,
                    dayOfWeekNameNp = bsDate.dayOfWeekNameNp,
                    dayOfWeekNameEn = bsDate.dayOfWeekNameEn,
                    isSaturday = bsDate.isSaturday,
                    formattedBsNp = bsDate.formatDisplay(useNepaliLanguage = true, useNepaliNumerals = true),
                    formattedBsEn = bsDate.formatDisplay(useNepaliLanguage = false, useNepaliNumerals = false),
                    formattedAd = adDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                )
                _conversionResult.value = res
                addToHistory(res)
            } else {
                val y = _adInputYear.value
                val m = _adInputMonth.value
                val d = _adInputDay.value
                val adDate = LocalDate.of(y, m, d)
                val bsDate = BsCalendarEngine.adToBs(adDate)

                val res = DateConversionResult(
                    bsDate = bsDate,
                    adDate = adDate,
                    isBsToAd = false,
                    dayOfWeekNameNp = bsDate.dayOfWeekNameNp,
                    dayOfWeekNameEn = bsDate.dayOfWeekNameEn,
                    isSaturday = bsDate.isSaturday,
                    formattedBsNp = bsDate.formatDisplay(useNepaliLanguage = true, useNepaliNumerals = true),
                    formattedBsEn = bsDate.formatDisplay(useNepaliLanguage = false, useNepaliNumerals = false),
                    formattedAd = adDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                )
                _conversionResult.value = res
                addToHistory(res)
            }
        } catch (e: Exception) {
            // Handle error gracefully
        }
    }

    private fun addToHistory(res: DateConversionResult) {
        val item = ConversionHistoryItem(
            isBsToAd = res.isBsToAd,
            sourceFormatted = if (res.isBsToAd) res.formattedBsNp else res.formattedAd,
            targetFormatted = if (res.isBsToAd) res.formattedAd else res.formattedBsNp,
            weekday = res.dayOfWeekNameNp
        )
        val current = _recentConversions.value.toMutableList()
        // Deduplicate and keep top 10
        current.removeAll { it.sourceFormatted == item.sourceFormatted }
        current.add(0, item)
        if (current.size > 10) {
            _recentConversions.value = current.take(10)
        } else {
            _recentConversions.value = current
        }
    }

    // Events management
    fun addEvent(
        title: String,
        description: String,
        bsDate: NepaliDate,
        category: String,
        colorHex: String,
        isAllDay: Boolean
    ) {
        viewModelScope.launch {
            val adDate = bsDate.toLocalDate().toString()
            val event = EventEntity(
                title = title.trim(),
                description = description.trim(),
                bsYear = bsDate.year,
                bsMonth = bsDate.month,
                bsDay = bsDate.day,
                adDateString = adDate,
                category = category,
                colorHex = colorHex,
                isAllDay = isAllDay
            )
            eventRepository.insertEvent(event)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
        }
    }

    fun deleteEventById(id: Long) {
        viewModelScope.launch {
            eventRepository.deleteEventById(id)
        }
    }

    fun clearAllEvents() {
        viewModelScope.launch {
            eventRepository.clearAll()
        }
    }

    // Preferences
    fun setLanguage(lang: String) {
        viewModelScope.launch { preferencesRepository.setLanguage(lang) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setNumeralSystem(numeral: String) {
        viewModelScope.launch { preferencesRepository.setNumeralSystem(numeral) }
    }

    fun setFirstDayOfWeek(firstDay: Int) {
        viewModelScope.launch { preferencesRepository.setFirstDayOfWeek(firstDay) }
    }

    fun setHighlightSaturday(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setHighlightSaturday(enabled) }
    }

    fun setShowFestivalsOnGrid(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowFestivalsOnGrid(enabled) }
    }

    fun setFestivalFilter(category: FestivalCategory) {
        _festivalCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCalculatorMode(mode: CalculatorMode) {
        _calculatorMode.value = mode
        _calcResult.value = null
    }

    fun calculateDaysBetween(d1: NepaliDate, d2: NepaliDate): Long {
        return BsCalendarEngine.daysBetween(d1, d2)
    }

    fun calculateAge(birthBs: NepaliDate): BsCalendarEngine.AgeResult {
        return BsCalendarEngine.calculateAge(birthBs, todayBs)
    }

    fun calculateAddDays(startBs: NepaliDate, days: Long): NepaliDate {
        return BsCalendarEngine.addDays(startBs, days)
    }

    // Customizable Date Notification Methods
    fun setDateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferencesRepository.setNotificationEnabled(enabled)
            preferencesRepository.setDateNotificationEnabled(enabled)
            val notifPrefs = notificationPreferences.value.copy(isEnabled = enabled)
            if (enabled) {
                com.example.nepalcalendar.notifications.CalendarNotificationManager.showOrUpdateDateNotification(
                    getApplication(), notifPrefs, userPreferences.value, todayBs, todayAd
                )
            } else {
                com.example.nepalcalendar.notifications.CalendarNotificationManager.cancelNotification(getApplication())
            }
        }
    }

    fun setNotificationOngoing(ongoing: Boolean) {
        viewModelScope.launch {
            notificationPreferencesRepository.setNotificationOngoing(ongoing)
            preferencesRepository.setNotificationOngoing(ongoing)
            val notifPrefs = notificationPreferences.value.copy(isOngoing = ongoing)
            if (notifPrefs.isEnabled) {
                com.example.nepalcalendar.notifications.CalendarNotificationManager.showOrUpdateDateNotification(
                    getApplication(), notifPrefs, userPreferences.value, todayBs, todayAd
                )
            }
        }
    }

    fun setNotificationIncludeGregorian(include: Boolean) {
        viewModelScope.launch {
            notificationPreferencesRepository.setIncludeGregorian(include)
            preferencesRepository.setNotificationIncludeGregorian(include)
            val notifPrefs = notificationPreferences.value.copy(includeGregorian = include)
            if (notifPrefs.isEnabled) {
                com.example.nepalcalendar.notifications.CalendarNotificationManager.showOrUpdateDateNotification(
                    getApplication(), notifPrefs, userPreferences.value, todayBs, todayAd
                )
            }
        }
    }

    fun setNotificationIncludeFestivals(include: Boolean) {
        viewModelScope.launch {
            notificationPreferencesRepository.setIncludeFestivals(include)
            preferencesRepository.setNotificationIncludeFestivals(include)
            val notifPrefs = notificationPreferences.value.copy(includeFestivals = include)
            if (notifPrefs.isEnabled) {
                com.example.nepalcalendar.notifications.CalendarNotificationManager.showOrUpdateDateNotification(
                    getApplication(), notifPrefs, userPreferences.value, todayBs, todayAd
                )
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            notificationPreferencesRepository.setNotificationTime(hour, minute)
            preferencesRepository.setNotificationTime(hour, minute)
        }
    }

    fun previewDateNotification() {
        val notifPrefs = notificationPreferences.value.copy(isEnabled = true)
        com.example.nepalcalendar.notifications.CalendarNotificationManager.showOrUpdateDateNotification(
            getApplication(), notifPrefs, userPreferences.value, todayBs, todayAd
        )
    }
}
