package com.example.data.model

data class HijriDate(
    val day: Int,          // 1..30
    val month: Int,        // 1..12
    val year: Int,         // e.g. 1448
    val monthName: String, // "Muharram", "Ramadan", etc.
    val monthNameArabic: String,
    val dayName: String,   // "Senin", "Selasa", etc.
    val gregorianFormatted: String // "27 Oktober 2026"
) {
    fun toFormattedString(): String = "$day $monthName $year H"
}

data class IslamicEvent(
    val hijriDay: Int,
    val hijriMonth: Int,
    val title: String,
    val arabicTitle: String,
    val description: String,
    val isMajorHoliday: Boolean = false
)

val ISLAMIC_EVENTS_LIST = listOf(
    IslamicEvent(1, 1, "Tahun Baru Islam (1 Muharram)", "رأس السنة الهجرية", "Awal tahun baru dalam kalender Hijriah", true),
    IslamicEvent(9, 1, "Puasa Tasu'a", "تاسوعاء", "Disunnahkan berpuasa sebelum hari Asyura", false),
    IslamicEvent(10, 1, "Hari Asyura", "عاشوراء", "Hari ke-10 Muharram, memperingati selamatnya Nabi Musa AS", true),
    IslamicEvent(12, 3, "Maulid Nabi Muhammad SAW", "المولد النبوي", "Kelahiran Baginda Nabi Muhammad SAW", true),
    IslamicEvent(27, 7, "Isra' Mi'raj", "الإسراء والمعراج", "Perjalanan malam Nabi Muhammad SAW dan perintah sholat 5 waktu", true),
    IslamicEvent(15, 8, "Malam Nisfu Sya'ban", "ليلة نصف شعبان", "Pertengahan bulan Sya'ban, malam pengampunan dosa", false),
    IslamicEvent(1, 9, "Awal Ramadan", "أول رمضان", "Hari pertama ibadah puasa wajib bulan suci Ramadan", true),
    IslamicEvent(17, 9, "Nuzulul Qur'an", "نزول القرآن", "Turunnya Al-Qur'an pertama kali ke bumi", true),
    IslamicEvent(21, 9, "Mulai 10 Malam Terakhir (Lailatul Qadr)", "العشر الأواخر", "Pencarian malam yang lebih baik dari 1000 bulan", false),
    IslamicEvent(1, 10, "Hari Raya Idul Fitri (1 Syawal)", "عيد الفطر المبارك", "Hari kemenangan kembali ke fitrah setelah Ramadan", true),
    IslamicEvent(2, 10, "Hari Kedua Idul Fitri", "عيد الفطر", "Silaturahim dan hari raya umat Islam", true),
    IslamicEvent(8, 12, "Hari Tarwiyah", "يوم التروية", "Hari ke-8 Dzulhijjah bagi jamaah haji di Mina", false),
    IslamicEvent(9, 12, "Hari Arafah", "يوم عرفة", "Puncak ibadah haji wukuf di Arafah & puasa sunnah Arafah", true),
    IslamicEvent(10, 12, "Hari Raya Idul Adha", "عيد الأضحى المبارك", "Hari raya kurban dan sholat Idul Adha", true),
    IslamicEvent(11, 12, "Hari Tasyrik ke-1", "أيام التشريق", "Penyembelihan hewan kurban, diharamkan berpuasa", false),
    IslamicEvent(12, 12, "Hari Tasyrik ke-2", "أيام التشريق", "Hari Tasyrik kedua", false),
    IslamicEvent(13, 12, "Hari Tasyrik ke-3", "أيام التشريق", "Hari Tasyrik ketiga", false)
)

val HIJRI_MONTHS = listOf(
    Pair("Muharram", "محرم"),
    Pair("Safar", "صفر"),
    Pair("Rabi'ul Awal", "ربيع الأول"),
    Pair("Rabi'ul Akhir", "ربيع الآخر"),
    Pair("Jumadil Awal", "جمادى الأولى"),
    Pair("Jumadil Akhir", "جمادى الآخرة"),
    Pair("Rajab", "رجب"),
    Pair("Sya'ban", "شعبان"),
    Pair("Ramadan", "رمضان"),
    Pair("Syawal", "شوال"),
    Pair("Dzulqa'dah", "ذو القعدة"),
    Pair("Dzulhijjah", "ذو الحجة")
)

data class HijriCalendarDay(
    val gregorianDay: Int,
    val gregorianMonth: Int, // 1..12
    val gregorianYear: Int,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriYear: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val event: IslamicEvent? = null,
    val isAyyamulBidh: Boolean = false // 13, 14, 15 of Hijri month
)
