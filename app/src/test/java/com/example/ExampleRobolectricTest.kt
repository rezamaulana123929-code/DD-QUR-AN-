package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.config.AppConfig
import com.example.data.local.UserPreferences
import com.example.data.model.AVAILABLE_CALCULATION_METHODS
import com.example.data.model.AVAILABLE_QARIS
import com.example.data.model.PRESET_CITIES
import com.example.service.HijriCalendarHelper
import com.example.service.PrayerTimesCalculator
import com.example.service.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DD Qur'an", appName)
    }

    @Test
    fun `verify AppConfig values`() {
        assertEquals("DD Qur'an", AppConfig.APP_NAME)
        assertEquals("Teman Ibadah, Setiap Hari.", AppConfig.TAGLINE)
        assertEquals("#064E3B", AppConfig.PRIMARY_COLOR_HEX)
        assertEquals("#D4AF37", AppConfig.ACCENT_GOLD_HEX)
        assertEquals(21.422487, AppConfig.KAABA_LATITUDE, 0.0001)
        assertEquals(39.826206, AppConfig.KAABA_LONGITUDE, 0.0001)
    }

    @Test
    fun `verify available reciters count`() {
        assertTrue(AVAILABLE_QARIS.size >= 6)
        val mishary = AVAILABLE_QARIS.find { it.id == "05" }
        assertNotNull(mishary)
        assertEquals("Misyari Rasyid Al-Afasi", mishary?.name)
    }

    @Test
    fun `verify Qibla bearing calculation from Jakarta`() {
        // Jakarta coordinates: -6.2088, 106.8456 -> expected bearing ~295° (North-West)
        val bearing = QiblaCalculator.calculateQiblaBearing(-6.2088, 106.8456)
        assertTrue("Bearing should be near 295° (actual: $bearing)", bearing in 294.0..296.0)

        // Distance to Kaaba should be around 7900 km
        val distance = QiblaCalculator.calculateDistanceToKaabaKm(-6.2088, 106.8456)
        assertTrue("Distance should be ~7900 km (actual: $distance)", distance in 7800.0..8000.0)
    }

    @Test
    fun `verify Prayer Times calculation ordering and format`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        cal.set(2026, Calendar.OCTOBER, 27)

        val kemenag = AVAILABLE_CALCULATION_METHODS[0]
        val schedule = PrayerTimesCalculator.calculateSchedule(
            calendar = cal,
            latitude = -6.2088,
            longitude = 106.8456,
            cityName = "Jakarta",
            method = kemenag
        )

        assertNotNull(schedule)
        assertEquals("Jakarta", schedule.cityName)
        assertTrue(schedule.subuh.timestamp < schedule.terbit.timestamp)
        assertTrue(schedule.terbit.timestamp < schedule.dzuhur.timestamp)
        assertTrue(schedule.dzuhur.timestamp < schedule.ashar.timestamp)
        assertTrue(schedule.ashar.timestamp < schedule.maghrib.timestamp)
        assertTrue(schedule.maghrib.timestamp < schedule.isya.timestamp)
    }

    @Test
    fun `verify Hijri Date conversion`() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.OCTOBER, 27)
        val hDate = HijriCalendarHelper.getHijriDate(cal)

        assertNotNull(hDate)
        assertEquals(1448, hDate.year)
        assertEquals(5, hDate.month) // Jumadil Awal
        assertEquals("Jumadil Awal", hDate.monthName)
    }

    @Test
    fun `verify UserPreferences persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferences(context)
        prefs.saveLastRead(18, "Al-Kahf", 10)

        val lastRead = prefs.lastReadFlow.value
        assertEquals(18, lastRead.surahNumber)
        assertEquals("Al-Kahf", lastRead.surahName)
        assertEquals(10, lastRead.verseNumber)

        prefs.updateArabicFontSize(32f)
        assertEquals(32f, prefs.settingsFlow.value.arabicFontSize)

        prefs.updateLocation("Surabaya", -7.2575, 112.7521, "Asia/Jakarta")
        assertEquals("Surabaya", prefs.settingsFlow.value.cityName)
    }
}
