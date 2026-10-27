package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.config.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserSettings(
    val arabicFontSize: Float = AppConfig.DEFAULT_ARABIC_FONT_SIZE_SP,
    val translationFontSize: Float = AppConfig.DEFAULT_TRANSLATION_FONT_SIZE_SP,
    val showLatin: Boolean = AppConfig.DEFAULT_SHOW_LATIN,
    val showTranslation: Boolean = AppConfig.DEFAULT_SHOW_TRANSLATION,
    val autoScrollSpeed: Int = AppConfig.DEFAULT_AUTO_SCROLL_SPEED,
    val selectedQariId: String = AppConfig.DEFAULT_QARI_ID,
    val dailyAyatTarget: Int = AppConfig.DEFAULT_DAILY_AYAT_TARGET,
    val dailyAyatReadCount: Int = 0,
    val themeMode: String = "system", // system, light, dark

    // Location & Qibla / Prayer Times
    val cityName: String = AppConfig.DEFAULT_CITY_NAME,
    val latitude: Double = AppConfig.DEFAULT_LATITUDE,
    val longitude: Double = AppConfig.DEFAULT_LONGITUDE,
    val timeZoneId: String = AppConfig.DEFAULT_TIMEZONE_ID,
    val calculationMethodId: String = "kemenag",
    val hijriAdjustmentDays: Int = 0,

    // Prayer Notification Settings
    val subuhNotification: Boolean = true,
    val dzuhurNotification: Boolean = true,
    val asharNotification: Boolean = true,
    val maghribNotification: Boolean = true,
    val isyaNotification: Boolean = true,
    val playAdhanSound: Boolean = true
)

data class LastReadPosition(
    val surahNumber: Int = 1,
    val surahName: String = "Al-Fatihah",
    val verseNumber: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dd_quran_user_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    private val _lastReadFlow = MutableStateFlow(loadLastRead())
    val lastReadFlow: StateFlow<LastReadPosition> = _lastReadFlow.asStateFlow()

    init {
        checkDailyReset()
    }

    private fun checkDailyReset() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("key_last_goal_date", "")
        if (savedDate != todayStr) {
            prefs.edit()
                .putString("key_last_goal_date", todayStr)
                .putInt("key_daily_ayat_read_count", 0)
                .apply()
            _settingsFlow.value = loadSettings()
        }
    }

    private fun loadSettings(): UserSettings {
        return UserSettings(
            arabicFontSize = prefs.getFloat("key_arabic_font_size", AppConfig.DEFAULT_ARABIC_FONT_SIZE_SP),
            translationFontSize = prefs.getFloat("key_translation_font_size", AppConfig.DEFAULT_TRANSLATION_FONT_SIZE_SP),
            showLatin = prefs.getBoolean("key_show_latin", AppConfig.DEFAULT_SHOW_LATIN),
            showTranslation = prefs.getBoolean("key_show_translation", AppConfig.DEFAULT_SHOW_TRANSLATION),
            autoScrollSpeed = prefs.getInt("key_auto_scroll_speed", AppConfig.DEFAULT_AUTO_SCROLL_SPEED),
            selectedQariId = prefs.getString("key_selected_qari_id", AppConfig.DEFAULT_QARI_ID) ?: AppConfig.DEFAULT_QARI_ID,
            dailyAyatTarget = prefs.getInt("key_daily_ayat_target", AppConfig.DEFAULT_DAILY_AYAT_TARGET),
            dailyAyatReadCount = prefs.getInt("key_daily_ayat_read_count", 0),
            themeMode = prefs.getString("key_theme_mode", "system") ?: "system",

            cityName = prefs.getString("key_city_name", AppConfig.DEFAULT_CITY_NAME) ?: AppConfig.DEFAULT_CITY_NAME,
            latitude = prefs.getString("key_latitude", AppConfig.DEFAULT_LATITUDE.toString())?.toDoubleOrNull() ?: AppConfig.DEFAULT_LATITUDE,
            longitude = prefs.getString("key_longitude", AppConfig.DEFAULT_LONGITUDE.toString())?.toDoubleOrNull() ?: AppConfig.DEFAULT_LONGITUDE,
            timeZoneId = prefs.getString("key_timezone_id", AppConfig.DEFAULT_TIMEZONE_ID) ?: AppConfig.DEFAULT_TIMEZONE_ID,
            calculationMethodId = prefs.getString("key_calc_method_id", "kemenag") ?: "kemenag",
            hijriAdjustmentDays = prefs.getInt("key_hijri_adjustment", 0),

            subuhNotification = prefs.getBoolean("key_notif_subuh", true),
            dzuhurNotification = prefs.getBoolean("key_notif_dzuhur", true),
            asharNotification = prefs.getBoolean("key_notif_ashar", true),
            maghribNotification = prefs.getBoolean("key_notif_maghrib", true),
            isyaNotification = prefs.getBoolean("key_notif_isya", true),
            playAdhanSound = prefs.getBoolean("key_play_adhan_sound", true)
        )
    }

    private fun loadLastRead(): LastReadPosition {
        val surahNo = prefs.getInt("key_last_surah_no", 1)
        val surahName = prefs.getString("key_last_surah_name", "Al-Fatihah") ?: "Al-Fatihah"
        val verseNo = prefs.getInt("key_last_verse_no", 1)
        val time = prefs.getLong("key_last_read_time", System.currentTimeMillis())
        return LastReadPosition(surahNo, surahName, verseNo, time)
    }

    fun saveLastRead(surahNumber: Int, surahName: String, verseNumber: Int) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putInt("key_last_surah_no", surahNumber)
            .putString("key_last_surah_name", surahName)
            .putInt("key_last_verse_no", verseNumber)
            .putLong("key_last_read_time", now)
            .apply()

        // Increment today's reading counter if applicable
        val newCount = _settingsFlow.value.dailyAyatReadCount + 1
        prefs.edit().putInt("key_daily_ayat_read_count", newCount).apply()

        _lastReadFlow.value = LastReadPosition(surahNumber, surahName, verseNumber, now)
        _settingsFlow.value = _settingsFlow.value.copy(dailyAyatReadCount = newCount)
    }

    fun updateArabicFontSize(size: Float) {
        prefs.edit().putFloat("key_arabic_font_size", size).apply()
        _settingsFlow.value = _settingsFlow.value.copy(arabicFontSize = size)
    }

    fun updateTranslationFontSize(size: Float) {
        prefs.edit().putFloat("key_translation_font_size", size).apply()
        _settingsFlow.value = _settingsFlow.value.copy(translationFontSize = size)
    }

    fun updateShowLatin(show: Boolean) {
        prefs.edit().putBoolean("key_show_latin", show).apply()
        _settingsFlow.value = _settingsFlow.value.copy(showLatin = show)
    }

    fun updateShowTranslation(show: Boolean) {
        prefs.edit().putBoolean("key_show_translation", show).apply()
        _settingsFlow.value = _settingsFlow.value.copy(showTranslation = show)
    }

    fun updateAutoScrollSpeed(speed: Int) {
        prefs.edit().putInt("key_auto_scroll_speed", speed).apply()
        _settingsFlow.value = _settingsFlow.value.copy(autoScrollSpeed = speed)
    }

    fun updateSelectedQari(qariId: String) {
        prefs.edit().putString("key_selected_qari_id", qariId).apply()
        _settingsFlow.value = _settingsFlow.value.copy(selectedQariId = qariId)
    }

    fun updateDailyTarget(target: Int) {
        prefs.edit().putInt("key_daily_ayat_target", target).apply()
        _settingsFlow.value = _settingsFlow.value.copy(dailyAyatTarget = target)
    }

    fun incrementDailyAyatCount(amount: Int = 1) {
        val newCount = _settingsFlow.value.dailyAyatReadCount + amount
        prefs.edit().putInt("key_daily_ayat_read_count", newCount).apply()
        _settingsFlow.value = _settingsFlow.value.copy(dailyAyatReadCount = newCount)
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString("key_theme_mode", mode).apply()
        _settingsFlow.value = _settingsFlow.value.copy(themeMode = mode)
    }

    fun updateLocation(cityName: String, latitude: Double, longitude: Double, timeZoneId: String) {
        prefs.edit()
            .putString("key_city_name", cityName)
            .putString("key_latitude", latitude.toString())
            .putString("key_longitude", longitude.toString())
            .putString("key_timezone_id", timeZoneId)
            .apply()

        _settingsFlow.value = _settingsFlow.value.copy(
            cityName = cityName,
            latitude = latitude,
            longitude = longitude,
            timeZoneId = timeZoneId
        )
    }

    fun updateCalculationMethod(methodId: String) {
        prefs.edit().putString("key_calc_method_id", methodId).apply()
        _settingsFlow.value = _settingsFlow.value.copy(calculationMethodId = methodId)
    }

    fun updateHijriAdjustment(adjustment: Int) {
        prefs.edit().putInt("key_hijri_adjustment", adjustment).apply()
        _settingsFlow.value = _settingsFlow.value.copy(hijriAdjustmentDays = adjustment)
    }

    fun updatePrayerNotification(prayerName: String, enabled: Boolean) {
        val editor = prefs.edit()
        when (prayerName.lowercase()) {
            "subuh" -> editor.putBoolean("key_notif_subuh", enabled)
            "dzuhur" -> editor.putBoolean("key_notif_dzuhur", enabled)
            "ashar" -> editor.putBoolean("key_notif_ashar", enabled)
            "maghrib" -> editor.putBoolean("key_notif_maghrib", enabled)
            "isya" -> editor.putBoolean("key_notif_isya", enabled)
        }
        editor.apply()

        _settingsFlow.value = when (prayerName.lowercase()) {
            "subuh" -> _settingsFlow.value.copy(subuhNotification = enabled)
            "dzuhur" -> _settingsFlow.value.copy(dzuhurNotification = enabled)
            "ashar" -> _settingsFlow.value.copy(asharNotification = enabled)
            "maghrib" -> _settingsFlow.value.copy(maghribNotification = enabled)
            "isya" -> _settingsFlow.value.copy(isyaNotification = enabled)
            else -> _settingsFlow.value
        }
    }

    fun updatePlayAdhanSound(enabled: Boolean) {
        prefs.edit().putBoolean("key_play_adhan_sound", enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(playAdhanSound = enabled)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settingsFlow.value = loadSettings()
        _lastReadFlow.value = loadLastRead()
    }
}
