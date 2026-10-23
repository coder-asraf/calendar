# Nepal Calendar Pro 🇳🇵

**Nepal Calendar Pro** is an advanced, offline-first, native Android application built with Kotlin and Jetpack Compose (Material 3). It implements a high-precision, verified Bikram Sambat (BS) calendar engine with bidirectional date conversion (BS ↔ AD), comprehensive festival and public holiday tracking, personal event management with local Room persistence, and date calculation tools.

---

## 🌟 Key Features

1. **Accurate Bikram Sambat Calendar Engine**:
   - Covers 126 years of official Bikram Sambat data from **1975 BS to 2100 BS** (1918 AD to 2044 AD).
   - Anchor epoch: **1975 BS Baisakh 1 = 1918-04-13 AD (Ordinal 1)**.
   - Built on verified astronomical tables from Nepal Panchanga Nirnayak Samiti.
   - Does **not** assume fixed month lengths or constant offsets; dynamically tracks exact variations (29 to 32 days per BS month).
   - Instant O(1) day-of-year calculations and binary-search ordinal conversions.

2. **Intuitive Calendar Dashboard**:
   - Current Nepali date (२०८३ कार्तिक ६, शुक्रबार) and Gregorian date (23 October 2026, Friday).
   - Dynamic month navigation with swipe gestures and quick Jump-to-Month/Year selector dialog.
   - Saturday weekly holiday highlight per Nepal national convention.
   - Public holiday badges and festival indicators on calendar cells.
   - Selected date card displaying equivalent Gregorian date, festival descriptions, and personal events.
   - One-tap date sharing in standard Nepali and English formats.

3. **Bidirectional Date Converter (BS ↔ AD)**:
   - Convert BS to AD and AD to BS with dropdown pickers for Year, Month, and Day.
   - Quick direction swap button.
   - Rich conversion result card with Nepali, English, and weekday representations.
   - Copy to clipboard and system share integration.
   - Saved recent conversion history.

4. **Nepali Festivals & Public Holidays**:
   - Complete database of major Nepali festivals (Dashain, Tihar, Chhath, Teej, Holi, Shivaratri, Buddha Jayanti, Lhosar, Maghe Sankranti, Gai Jatra, etc.) and national holidays (New Year, Constitution Day, Republic Day, Democracy Day, Workers' Day, Women's Day, Martyrs' Day).
   - Filter chips: All, Public Holidays, National Days, Religious & Festivals, Cultural.
   - Live search in both Nepali and English with countdown ("In X days").

5. **Personal Event Management**:
   - Local persistence using Android Jetpack **Room** database with reactive `Flow` queries.
   - Add, view, and delete personal events, birthdays, anniversaries, and deadlines.
   - Category tagging and color indicators that appear directly on the calendar grid.
   - Private and 100% offline — user events never leave the device.

6. **Date Calculator & Tools**:
   - **Days Between Dates**: Exact day count between any two BS dates.
   - **Age Calculator**: Accurate age in years, months, days, total days lived, and countdown to next birthday.
   - **Countdown**: Days remaining until a target date.
   - **Add / Subtract Days**: Add or subtract N days to any BS date to find the resulting BS and AD date.

7. **Multilingual & Customization**:
   - Full Nepali (नेपाली) and English interface support.
   - Toggle between Devanagari numerals (०१२३४५६७८९) and Arabic numerals (0123456789).
   - Theme options: System default, Light, and Dark mode.
   - First day of the week preference: Sunday (आइतबार) or Monday (सोमबार).
   - Option to clear local data with confirmation dialog.

8. **Customizable Date Notification in Status Bar**:
   - Pinned ongoing status-bar notification for instant quick-glance date checks or scheduled daily alerts.
   - Fully customizable content: show/hide Gregorian date, show/hide today's festivals & public holidays.
   - Respects user language (Nepali/English) and numeral preference (Devanagari/Arabic).
   - Instant in-app notification preview and "Test Now" action.
   - Automatically synchronizes across daily changes and device reboots (`RECEIVE_BOOT_COMPLETED`).

9. **Custom Adaptive Icon**:
   - Material You adaptive icon featuring Nepal's national sun and crescent moon calendar motif.

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose with Material 3 (M3)
- **Architecture**: MVVM with Clean Repository Pattern
- **Local Database**: Android Jetpack Room with Kotlin Symbol Processing (KSP)
- **Preferences**: Jetpack DataStore Preferences
- **Coroutines & Flow**: Reactive, non-blocking asynchronous state handling
- **Testing**: JUnit 4 and Robolectric local JVM testing suite

---

## 🧪 Testing & Verification

The calendar engine includes automated unit tests verifying:
- Known independent reference dates across historical and modern eras.
- Round-trip conversions: BS → AD → BS and AD → BS → AD.
- Weekday alignment and Saturday holiday detection.
- Month and year boundary arithmetic.
- Leap year calculations.
- Input validation and invalid date rejection.
- Festival record integrity.

Run tests via Gradle:
```bash
gradle :app:testDebugUnitTest
```

Build debug APK:
```bash
gradle assembleDebug
```
