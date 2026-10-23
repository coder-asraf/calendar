package com.example.nepalcalendar.calendar.model

enum class FestivalCategory(val titleNp: String, val titleEn: String) {
    ALL("सबै", "All"),
    PUBLIC_HOLIDAY("सार्वजनिक बिदा", "Public Holidays"),
    NATIONAL("राष्ट्रिय दिवस", "National Days"),
    MAJOR_RELIGIOUS("धार्मिक तथा चाडपर्व", "Religious & Festivals"),
    CULTURAL("सांस्कृतिक पर्व", "Cultural Events")
}

data class Festival(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val bsYear: Int, // 0 if annual recurring on fixed BS month/day
    val bsMonth: Int,
    val bsDay: Int,
    val isPublicHoliday: Boolean,
    val category: FestivalCategory,
    val descriptionNp: String,
    val descriptionEn: String,
    val source: String = "Nepal Government Gazette / Panchanga Nirnayak Samiti"
)
