package com.example.nepalcalendar.data.repository

import com.example.nepalcalendar.calendar.engine.NepaliDate
import com.example.nepalcalendar.calendar.model.Festival
import com.example.nepalcalendar.calendar.model.FestivalCategory

object FestivalRepository {

    // Annual fixed national holidays & observances (BS year = 0 implies every year)
    private val fixedAnnualFestivals = listOf(
        Festival(
            id = "fix_new_year",
            nameNp = "नयाँ वर्ष (नववर्ष)",
            nameEn = "Nepali New Year (Navabarsha)",
            bsYear = 0,
            bsMonth = 1,
            bsDay = 1,
            isPublicHoliday = true,
            category = FestivalCategory.NATIONAL,
            descriptionNp = "विक्रम संवत्‌को नयाँ वर्षको पहिलो दिन, राष्ट्रिय सार्वजनिक बिदा।",
            descriptionEn = "First day of Bikram Sambat calendar year. National public holiday across Nepal."
        ),
        Festival(
            id = "fix_labor_day",
            nameNp = "अन्तर्राष्ट्रिय श्रमिक दिवस",
            nameEn = "International Workers' Day",
            bsYear = 0,
            bsMonth = 1,
            bsDay = 18,
            isPublicHoliday = true,
            category = FestivalCategory.NATIONAL,
            descriptionNp = "मई १ तारिखको उपलक्ष्यमा मनाइने अन्तर्राष्ट्रिय मजदुर दिवस।",
            descriptionEn = "Observed worldwide on May 1 (Baisakh 18/19 BS). National public holiday honoring workers."
        ),
        Festival(
            id = "fix_republic_day",
            nameNp = "गणतन्त्र दिवस",
            nameEn = "Republic Day",
            bsYear = 0,
            bsMonth = 2,
            bsDay = 15,
            isPublicHoliday = true,
            category = FestivalCategory.NATIONAL,
            descriptionNp = "२०६५ जेठ १५ गते नेपालमा गणतन्त्र घोषणा भएको ऐतिहासिक दिन।",
            descriptionEn = "Celebrates the establishment of the Federal Democratic Republic of Nepal on Jestha 15, 2065 BS."
        ),
        Festival(
            id = "fix_constitution_day",
            nameNp = "संविधान दिवस (राष्ट्रिय दिवस)",
            nameEn = "Constitution Day (National Day)",
            bsYear = 0,
            bsMonth = 6,
            bsDay = 3,
            isPublicHoliday = true,
            category = FestivalCategory.NATIONAL,
            descriptionNp = "२०७२ असोज ३ गते नेपालको नयाँ संविधान जारी भएको ऐतिहासिक दिन।",
            descriptionEn = "Commemorates the promulgation of the Constitution of Nepal on Ashwin 3, 2072 BS."
        ),
        Festival(
            id = "fix_maghe_sankranti",
            nameNp = "माघे संक्रान्ति / मकर संक्रान्ति",
            nameEn = "Maghe Sankranti",
            bsYear = 0,
            bsMonth = 10,
            bsDay = 1,
            isPublicHoliday = true,
            category = FestivalCategory.CULTURAL,
            descriptionNp = "सूर्य मकर राशिमा प्रवेश गर्ने दिन। तिलको लड्डु, चाकु, घिउ र तरुल खाने पर्व।",
            descriptionEn = "Solstice festival celebrating harvest, sun's transition into Capricorn. Traditional feast of ghee, molasses, yams."
        ),
        Festival(
            id = "fix_martyrs_day",
            nameNp = "शहीद दिवस",
            nameEn = "Martyrs' Day (Shahid Diwas)",
            bsYear = 0,
            bsMonth = 10,
            bsDay = 16,
            isPublicHoliday = false,
            category = FestivalCategory.NATIONAL,
            descriptionNp = "प्रजातन्त्रका निम्ति बलिदान दिने अमर शहीदहरूको सम्झनामा मनाइने दिन।",
            descriptionEn = "Memorial day honoring the four great martyrs who sacrificed their lives for democracy."
        ),
        Festival(
            id = "fix_democracy_day",
            nameNp = "राष्ट्रिय प्रजातन्त्र दिवस",
            nameEn = "National Democracy Day",
            bsYear = 0,
            bsMonth = 11,
            bsDay = 7,
            isPublicHoliday = true,
            category = FestivalCategory.NATIONAL,
            descriptionNp = "२००७ साल फागुन ७ गते १०४ वर्षे जहानियाँ राणा शासन अन्त्य भई प्रजातन्त्र स्थापना भएको दिन।",
            descriptionEn = "Commemorates the 1951 revolution and end of the 104-year Rana autocracy on Falgun 7, 2007 BS."
        ),
        Festival(
            id = "fix_womens_day",
            nameNp = "अन्तर्राष्ट्रिय महिला दिवस",
            nameEn = "International Women's Day",
            bsYear = 0,
            bsMonth = 11,
            bsDay = 24,
            isPublicHoliday = true,
            category = FestivalCategory.NATIONAL,
            descriptionNp = "महिला अधिकार, सशक्तिकरण तथा लैंगिक समानताको विश्वव्यापी पर्व (मार्च ८)।",
            descriptionEn = "Global celebration of women's rights and equality on March 8 (Falgun 24 BS). Public holiday in Nepal."
        )
    )

    // Year-specific tithi-based festivals (Dashain, Tihar, Shivaratri, Holi, Teej, Buddha Jayanti, etc.)
    private val yearSpecificFestivals = listOf(
        // === 2083 BS ===
        Festival("2083_buddha_jayanti", "बुद्ध जयन्ती / उभौली पर्व", "Buddha Jayanti / Ubhauli", 2083, 1, 28, true, FestivalCategory.MAJOR_RELIGIOUS, "भगवान गौतम बुद्धको जन्म, ज्ञान प्राप्ति र महापरिनिर्वाण दिवस।", "Celebration of Lord Buddha's birth, enlightenment, and nirvana."),
        Festival("2083_janai_purnima", "जनै पूर्णिमा / रक्षा बन्धन", "Janai Purnima / Raksha Bandhan", 2083, 4, 31, true, FestivalCategory.MAJOR_RELIGIOUS, "तागाधारीहरूले जनै फेर्ने र रक्षा बन्धन बाँध्ने पवित्र श्रावणी पूर्णिमा।", "Sacred thread changing festival and celebration of sibling protection bonds."),
        Festival("2083_gai_jatra", "गाईजात्रा", "Gai Jatra", 2083, 5, 2, false, FestivalCategory.CULTURAL, "काठमाडौँ उपत्यकामा दिवंगत पितृहरूको सम्झनामा मनाइने परम्परागत हास्यव्यंग्य पर्व।", "Newar festival of cows commemorating deceased family members."),
        Festival("2083_krishna_janmashtami", "श्रीकृष्ण जन्माष्टमी", "Krishna Janmashtami", 2083, 5, 9, true, FestivalCategory.MAJOR_RELIGIOUS, "भगवान श्रीकृष्णको जन्मोत्सव।", "Lord Krishna's birth celebration."),
        Festival("2083_teej", "हरितालिका तीज", "Haritalika Teej", 2083, 5, 18, true, FestivalCategory.MAJOR_RELIGIOUS, "महिलाहरूको महान पर्व, माता पार्वतीको आराधना र व्रत।", "Major women's fasting festival honoring Lord Shiva and Parvati."),
        Festival("2083_rishi_panchami", "ऋषि पञ्चमी", "Rishi Panchami", 2083, 5, 20, false, FestivalCategory.MAJOR_RELIGIOUS, "सप्तऋषिहरूको पूजा आराधना गरिने पवित्र दिन।", "Purification and worship of the Saptarishi sages."),
        Festival("2083_indra_jatra", "इन्द्रजात्रा", "Indra Jatra", 2083, 6, 8, false, FestivalCategory.CULTURAL, "काठमाडौँ उपत्यकाको भव्य सांस्कृतिक पर्व, जीवित देवी कुमारीको रथयात्रा।", "Major Kathmandu valley festival celebrating the rain god Indra and living goddess Kumari."),
        Festival("2083_ghatasthapana", "घटस्थापना (दशैँ सुरु)", "Ghatasthapana (Dashain Starts)", 2083, 6, 25, true, FestivalCategory.MAJOR_RELIGIOUS, "बडादशैँको पहिलो दिन, जमरा राख्ने दिन।", "First day of Bada Dashain; sowing of barley seeds (Jamara)."),
        Festival("2083_fulpati", "फूलपाती", "Fulpati", 2083, 7, 1, true, FestivalCategory.MAJOR_RELIGIOUS, "दशैँको सातौँ दिन, गोरखाबाट काठमाडौँ फूलपाती भित्र्याइने दिन।", "Seventh day of Dashain with sacred foliage procession from Gorkha."),
        Festival("2083_maha_ashtami", "महाअष्टमी", "Maha Ashtami", 2083, 7, 2, true, FestivalCategory.MAJOR_RELIGIOUS, "कालरात्रि तथा महाकालीको पूजा गरिने दिन।", "Eighth day of Dashain; special prayers to Goddess Mahakali."),
        Festival("2083_maha_navami", "महानवमी", "Maha Navami", 2083, 7, 3, true, FestivalCategory.MAJOR_RELIGIOUS, "दशैँको नवौँ दिन, कलपुर्जा तथा सवारीसाधनको पूजा।", "Ninth day of Dashain; Vishwakarma and weapon worship."),
        Festival("2083_vijaya_dashami", "विजयादशमी (दशैँको टीका)", "Vijaya Dashami (Dashain Tika)", 2083, 7, 4, true, FestivalCategory.MAJOR_RELIGIOUS, "बडादशैँको मुख्य दिन, मान्यजनबाट रातो टीका र पहेँलो जमरा ग्रहण गर्ने दिन।", "Most auspicious day of Dashain; elders bestow Tika, Jamara, and blessings."),
        Festival("2083_kojagrat_purnima", "कोजाग्रत पूर्णिमा", "Kojagrat Purnima", 2083, 7, 8, false, FestivalCategory.MAJOR_RELIGIOUS, "दशैँ समापन हुने पूर्णिमा, महालक्ष्मीको पूजा।", "Conclusion of Dashain festival; night vigil honoring Goddess Lakshmi."),
        Festival("2083_kaag_tihar", "काग तिहार", "Kaag Tihar", 2083, 7, 23, false, FestivalCategory.MAJOR_RELIGIOUS, "यमदूत कागको पूजा गरिने दिन।", "First day of Tihar; crow worship as Yamaraj's messenger."),
        Festival("2083_kukur_tihar", "कुकुर तिहार", "Kukur Tihar", 2083, 7, 24, false, FestivalCategory.MAJOR_RELIGIOUS, "मानवको विश्वासी साथी कुकुरको पूजा गरिने दिन।", "Second day of Tihar; celebration of the loyalty and companionship of dogs."),
        Festival("2083_laxmi_puja", "गाई तिहार / लक्ष्मीपूजा", "Laxmi Puja / Deepawali", 2083, 7, 25, true, FestivalCategory.MAJOR_RELIGIOUS, "धनधान्यकी देवी लक्ष्मीको भव्य पूजा र दीप प्रज्वलन।", "Grand illumination and worship of Goddess Lakshmi."),
        Festival("2083_govardhan_puja", "गोवर्धन पूजा / म्ह पूजा", "Govardhan Puja / Mha Puja", 2083, 7, 26, true, FestivalCategory.MAJOR_RELIGIOUS, "गोवर्धन पर्वत तथा आत्मपूजा (नेवार समुदायमा म्ह: पूजा)।", "Fourth day of Tihar; Govardhan worship and Newari New Year Mha Puja."),
        Festival("2083_bhai_tika", "भाइटीका", "Bhai Tika", 2083, 7, 27, true, FestivalCategory.MAJOR_RELIGIOUS, "दिदीबहिनीले दाजुभाइलाई सप्तरङ्गी टीका लगाइदिने दिन।", "Final day of Tihar; sisters blessing brothers with seven-colored Tika for longevity."),
        Festival("2083_chhath_puja", "छठ पर्व", "Chhath Puja", 2083, 7, 30, true, FestivalCategory.MAJOR_RELIGIOUS, "सूर्यदेव तथा छठी मैयाको निष्ठापूर्वक उपासना गरिने महान पर्व।", "Ancient Vedic solar festival honoring Surya and Chhathi Maiya at riverbanks."),
        Festival("2083_tamu_lhosar", "तमु ल्होसार", "Tamu Lhosar", 2083, 9, 15, true, FestivalCategory.CULTURAL, "गुरुङ समुदायको महान नयाँ वर्ष पर्व।", "Gurung community New Year festival."),
        Festival("2083_sonam_lhosar", "सोनाम ल्होसार", "Sonam Lhosar", 2083, 10, 24, true, FestivalCategory.CULTURAL, "तामाङ समुदायको महान नयाँ वर्ष पर्व।", "Tamang community New Year festival."),
        Festival("2083_shivaratri", "महाशिवरात्रि", "Maha Shivaratri", 2083, 11, 21, true, FestivalCategory.MAJOR_RELIGIOUS, "भगवान शिवको आराधना, पशुपतिनाथ मन्दिरमा भव्य मेला।", "Great night of Lord Shiva; massive pilgrimage to Pashupatinath."),
        Festival("2083_gyalpo_lhosar", "ग्याल्पो ल्होसार", "Gyalpo Lhosar", 2083, 11, 25, true, FestivalCategory.CULTURAL, "शेर्पा तथा हिमाली समुदायको नयाँ वर्ष।", "Tibetan/Sherpa community New Year."),
        Festival("2083_holi_hilly", "फागु पूर्णिमा / होली (पहाडी)", "Holi (Hilly Region)", 2083, 11, 28, true, FestivalCategory.MAJOR_RELIGIOUS, "रङहरूको उमङ्गिलो पर्व, काठमाडौँ तथा पहाडी जिल्लामा सार्वजनिक बिदा।", "Festival of colors in Kathmandu valley and hilly districts."),
        Festival("2083_holi_terai", "होली (तराई)", "Holi (Terai Region)", 2083, 11, 29, true, FestivalCategory.MAJOR_RELIGIOUS, "तराई-मधेशमा मनाइने रङहरूको महान पर्व।", "Festival of colors celebrated across the southern Terai plains."),
        Festival("2083_ghode_jatra", "घोडेजात्रा", "Ghode Jatra", 2083, 12, 13, false, FestivalCategory.CULTURAL, "काठमाडौँ टुँडिखेलमा मनाइने घोडेजात्रा पर्व।", "Horse racing festival celebrated in Kathmandu."),
        Festival("2083_chaite_dashain", "चैते दशैँ", "Chaite Dashain", 2083, 12, 22, false, FestivalCategory.MAJOR_RELIGIOUS, "चैत्र शुक्ल अष्टमीको दिन मनाइने सानो दशैँ।", "Spring festival worshipping Goddess Durga."),
        Festival("2083_ram_navami", "रामनवमी", "Ram Navami", 2083, 12, 23, true, FestivalCategory.MAJOR_RELIGIOUS, "मर्यादा पुरुषोत्तम भगवान श्री रामको जन्मोत्सव।", "Lord Rama's auspicious birth anniversary."),

        // === 2082 BS ===
        Festival("2082_buddha_jayanti", "बुद्ध जयन्ती", "Buddha Jayanti", 2082, 1, 29, true, FestivalCategory.MAJOR_RELIGIOUS, "भगवान बुद्धको त्रिसंयोग दिवस।", "Buddha Jayanti celebration."),
        Festival("2082_janai_purnima", "जनै पूर्णिमा", "Janai Purnima", 2082, 4, 25, true, FestivalCategory.MAJOR_RELIGIOUS, "रक्षाबन्धन पर्व।", "Sacred thread changing."),
        Festival("2082_krishna_janmashtami", "श्रीकृष्ण जन्माष्टमी", "Krishna Janmashtami", 2082, 5, 1, true, FestivalCategory.MAJOR_RELIGIOUS, "भगवान श्रीकृष्ण जन्मोत्सव।", "Lord Krishna's birthday."),
        Festival("2082_teej", "हरितालिका तीज", "Haritalika Teej", 2082, 5, 10, true, FestivalCategory.MAJOR_RELIGIOUS, "तीज व्रत।", "Teej festival."),
        Festival("2082_ghatasthapana", "घटस्थापना", "Ghatasthapana", 2082, 6, 6, true, FestivalCategory.MAJOR_RELIGIOUS, "दशैँ सुरु।", "Dashain starts."),
        Festival("2082_vijaya_dashami", "विजयादशमी", "Vijaya Dashami", 2082, 6, 16, true, FestivalCategory.MAJOR_RELIGIOUS, "दशैँ टीका।", "Dashain main day."),
        Festival("2082_laxmi_puja", "लक्ष्मीपूजा", "Laxmi Puja", 2082, 7, 4, true, FestivalCategory.MAJOR_RELIGIOUS, "दीपावली।", "Diwali."),
        Festival("2082_bhai_tika", "भाइटीका", "Bhai Tika", 2082, 7, 7, true, FestivalCategory.MAJOR_RELIGIOUS, "भाइटीका पर्व।", "Bhai Tika."),
        Festival("2082_chhath", "छठ पर्व", "Chhath Puja", 2082, 7, 10, true, FestivalCategory.MAJOR_RELIGIOUS, "छठ पर्व।", "Chhath festival."),
        Festival("2082_shivaratri", "महाशिवरात्रि", "Maha Shivaratri", 2082, 11, 4, true, FestivalCategory.MAJOR_RELIGIOUS, "शिवरात्रि।", "Maha Shivaratri."),
        Festival("2082_holi", "फागु पूर्णिमा", "Holi", 2082, 11, 19, true, FestivalCategory.MAJOR_RELIGIOUS, "होली पर्व।", "Holi color festival."),

        // === 2081 BS ===
        Festival("2081_buddha_jayanti", "बुद्ध जयन्ती", "Buddha Jayanti", 2081, 2, 10, true, FestivalCategory.MAJOR_RELIGIOUS, "बुद्ध जयन्ती।", "Buddha Jayanti."),
        Festival("2081_janai_purnima", "जनै पूर्णिमा", "Janai Purnima", 2081, 5, 3, true, FestivalCategory.MAJOR_RELIGIOUS, "जनै पूर्णिमा।", "Janai Purnima."),
        Festival("2081_teej", "हरितालिका तीज", "Haritalika Teej", 2081, 5, 21, true, FestivalCategory.MAJOR_RELIGIOUS, "तीज।", "Teej."),
        Festival("2081_ghatasthapana", "घटस्थापना", "Ghatasthapana", 2081, 6, 17, true, FestivalCategory.MAJOR_RELIGIOUS, "दशैँ सुरु।", "Dashain begins."),
        Festival("2081_vijaya_dashami", "विजयादशमी", "Vijaya Dashami", 2081, 6, 26, true, FestivalCategory.MAJOR_RELIGIOUS, "दशैँ टीका।", "Dashain Tika."),
        Festival("2081_laxmi_puja", "लक्ष्मीपूजा", "Laxmi Puja", 2081, 7, 15, true, FestivalCategory.MAJOR_RELIGIOUS, "लक्ष्मीपूजा।", "Laxmi Puja."),
        Festival("2081_bhai_tika", "भाइटीका", "Bhai Tika", 2081, 7, 18, true, FestivalCategory.MAJOR_RELIGIOUS, "भाइटीका।", "Bhai Tika."),
        Festival("2081_chhath", "छठ पर्व", "Chhath Puja", 2081, 7, 22, true, FestivalCategory.MAJOR_RELIGIOUS, "छठ।", "Chhath."),
        Festival("2081_shivaratri", "महाशिवरात्रि", "Maha Shivaratri", 2081, 11, 15, true, FestivalCategory.MAJOR_RELIGIOUS, "शिवरात्रि।", "Shivaratri."),
        Festival("2081_holi", "फागु पूर्णिमा", "Holi", 2081, 11, 29, true, FestivalCategory.MAJOR_RELIGIOUS, "होली।", "Holi.")
    )

    /**
     * Returns all festivals and holidays for a given BS year and month.
     */
    fun getFestivalsForMonth(bsYear: Int, bsMonth: Int): List<Festival> {
        val result = mutableListOf<Festival>()
        // 1. Add fixed annual holidays for this month
        fixedAnnualFestivals.filter { it.bsMonth == bsMonth }.forEach {
            result.add(it.copy(bsYear = bsYear))
        }
        // 2. Add year-specific festivals
        yearSpecificFestivals.filter { it.bsYear == bsYear && it.bsMonth == bsMonth }.forEach {
            result.add(it)
        }
        return result.sortedBy { it.bsDay }
    }

    /**
     * Returns all festivals and holidays for a specific BS date.
     */
    fun getFestivalsForDate(date: NepaliDate): List<Festival> {
        return getFestivalsForMonth(date.year, date.month).filter { it.bsDay == date.day }
    }

    /**
     * Checks if a specific BS date is an official public holiday.
     */
    fun isPublicHoliday(date: NepaliDate): Boolean {
        return getFestivalsForDate(date).any { it.isPublicHoliday }
    }

    /**
     * Returns all festivals for a specific BS year.
     */
    fun getAllFestivalsForYear(bsYear: Int): List<Festival> {
        val list = mutableListOf<Festival>()
        fixedAnnualFestivals.forEach { list.add(it.copy(bsYear = bsYear)) }
        yearSpecificFestivals.filter { it.bsYear == bsYear }.forEach { list.add(it) }
        return list.sortedWith(compareBy({ it.bsMonth }, { it.bsDay }))
    }

    /**
     * Searches festivals by query in Nepali or English.
     */
    fun searchFestivals(query: String, bsYear: Int = 2083): List<Festival> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return getAllFestivalsForYear(bsYear).filter {
            it.nameNp.contains(q, ignoreCase = true) ||
            it.nameEn.contains(q, ignoreCase = true) ||
            it.descriptionNp.contains(q, ignoreCase = true) ||
            it.descriptionEn.contains(q, ignoreCase = true)
        }
    }
}
