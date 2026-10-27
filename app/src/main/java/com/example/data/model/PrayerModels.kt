package com.example.data.model

import java.util.Date

enum class PrayerName(val displayName: String, val arabicName: String) {
    IMSAK("Imsak", "الإمساك"),
    SUBUH("Subuh", "الفجر"),
    TERBIT("Terbit", "الشروق"),
    DHUHA("Dhuha", "الضحى"),
    DZUHUR("Dzuhur", "الظهر"),
    ASHAR("Ashar", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISYA("Isya", "العشاء")
}

data class SinglePrayerTime(
    val prayer: PrayerName,
    val timeFormatted: String, // "04:32"
    val timestamp: Long,       // Epoch millis
    val isNext: Boolean = false
)

data class DailyPrayerSchedule(
    val dateFormatted: String,  // "Senin, 27 Oktober 2026"
    val cityName: String,
    val calculationMethod: String,
    val imsak: SinglePrayerTime,
    val subuh: SinglePrayerTime,
    val terbit: SinglePrayerTime,
    val dhuha: SinglePrayerTime,
    val dzuhur: SinglePrayerTime,
    val ashar: SinglePrayerTime,
    val maghrib: SinglePrayerTime,
    val isya: SinglePrayerTime,
    val nextPrayer: SinglePrayerTime?,
    val countdownMillis: Long = 0L
) {
    fun getList(): List<SinglePrayerTime> = listOf(
        imsak, subuh, terbit, dhuha, dzuhur, ashar, maghrib, isya
    )
}

data class CalculationMethod(
    val id: String,
    val name: String,
    val description: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val isIshaFixedMinutes: Boolean = false,
    val ishaFixedMinutes: Int = 90
)

val AVAILABLE_CALCULATION_METHODS = listOf(
    CalculationMethod(
        id = "kemenag",
        name = "Kemenag RI (Standar Indonesia)",
        description = "Kementerian Agama RI (Subuh 20°, Isya 18°)",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    ),
    CalculationMethod(
        id = "mwl",
        name = "Muslim World League (MWL)",
        description = "Liga Muslim Dunia (Subuh 18°, Isya 17°)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    CalculationMethod(
        id = "egypt",
        name = "Egyptian General Authority",
        description = "Otoritas Umum Mesir (Subuh 19.5°, Isya 17.5°)",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    CalculationMethod(
        id = "makkah",
        name = "Umm Al-Qura (Makkah)",
        description = "Universitas Umm Al-Qura (Subuh 18.5°, Isya 90 menit setelah Maghrib)",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        isIshaFixedMinutes = true,
        ishaFixedMinutes = 90
    ),
    CalculationMethod(
        id = "isna",
        name = "ISNA (Amerika Utara)",
        description = "Islamic Society of North America (Subuh 15°, Isya 15°)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    )
)

data class PrayerNotificationSettings(
    val subuhEnabled: Boolean = true,
    val dzuhurEnabled: Boolean = true,
    val asharEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val isyaEnabled: Boolean = true,
    val playAdhanSound: Boolean = true
)
