package com.example.service

import com.example.data.model.CalculationMethod
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.SinglePrayerTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

object PrayerTimesCalculator {

    /**
     * Calculates the daily prayer schedule for a given date, latitude, longitude, and calculation method.
     */
    fun calculateSchedule(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        cityName: String,
        method: CalculationMethod
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // TimeZone offset in hours
        val timeZone = calendar.timeZone
        val offsetHours = timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        // Julian Date
        val jd = calculateJulianDate(year, month, day)
        val d = jd - 2451545.0

        // Sun position parameters
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d

        // Right ascension in degrees
        var ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))
        ra = fixAngle(ra) / 15.0 // Convert to hours

        // Declination
        val sinDelta = sin(Math.toRadians(e)) * sin(Math.toRadians(l))
        val delta = Math.toDegrees(asin(sinDelta))

        // Equation of time in hours
        val eqT = q / 15.0 - ra

        // Midday (Dhuhr base time)
        val noon = 12.0 + offsetHours - (longitude / 15.0) - eqT
        // Add 2 minutes ihtiyat (safety precaution standard in Kemenag)
        val dhuhrHours = noon + (2.0 / 60.0)

        // Sunrise & Sunset angle (accounting for refraction: -0.833°)
        val sunriseHourAngle = calculateHourAngle(-0.833, latitude, delta)
        val sunriseHours = noon - sunriseHourAngle
        val sunsetHours = noon + sunriseHourAngle + (2.0 / 60.0) // Maghrib = sunset + ihtiyat

        // Fajr (Subuh)
        val fajrHourAngle = calculateHourAngle(-method.fajrAngle, latitude, delta)
        val fajrHours = noon - fajrHourAngle

        // Imsak (10 minutes before Fajr)
        val imsakHours = fajrHours - (10.0 / 60.0)

        // Dhuha (when sun is ~4.5° above horizon)
        val dhuhaHourAngle = calculateHourAngle(4.5, latitude, delta)
        val dhuhaHours = noon - dhuhaHourAngle

        // Asr (Shafi'i: shadow = 1)
        val asrAltitude = Math.toDegrees(atan(1.0 / (1.0 + tan(Math.toRadians(abs(latitude - delta))))))
        val asrHourAngle = calculateHourAngle(asrAltitude, latitude, delta)
        val asrHours = noon + asrHourAngle + (2.0 / 60.0)

        // Isha
        val isyaHours = if (method.isIshaFixedMinutes) {
            sunsetHours + (method.ishaFixedMinutes / 60.0)
        } else {
            val isyaHourAngle = calculateHourAngle(-method.ishaAngle, latitude, delta)
            noon + isyaHourAngle + (2.0 / 60.0)
        }

        // Convert decimal hours to Timestamps & Formatted Strings
        val imsakTime = createPrayerTime(calendar, PrayerName.IMSAK, imsakHours)
        val subuhTime = createPrayerTime(calendar, PrayerName.SUBUH, fajrHours)
        val terbitTime = createPrayerTime(calendar, PrayerName.TERBIT, sunriseHours)
        val dhuhaTime = createPrayerTime(calendar, PrayerName.DHUHA, dhuhaHours)
        val dzuhurTime = createPrayerTime(calendar, PrayerName.DZUHUR, dhuhrHours)
        val asharTime = createPrayerTime(calendar, PrayerName.ASHAR, asrHours)
        val maghribTime = createPrayerTime(calendar, PrayerName.MAGHRIB, sunsetHours)
        val isyaTime = createPrayerTime(calendar, PrayerName.ISYA, isyaHours)

        val allTimes = listOf(imsakTime, subuhTime, terbitTime, dhuhaTime, dzuhurTime, asharTime, maghribTime, isyaTime)

        // Find next prayer time and countdown
        val now = System.currentTimeMillis()
        var nextPrayer: SinglePrayerTime? = null
        var minDiff = Long.MAX_VALUE

        for (pt in allTimes) {
            val diff = pt.timestamp - now
            if (diff > 0 && diff < minDiff) {
                minDiff = diff
                nextPrayer = pt
            }
        }

        // If all prayers today passed, next is tomorrow's Imsak/Subuh
        val countdown = if (nextPrayer != null) {
            (nextPrayer.timestamp - now).coerceAtLeast(0L)
        } else {
            // Approx until tomorrow Subuh
            val calTomorrow = calendar.clone() as Calendar
            calTomorrow.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowSubuh = createPrayerTime(calTomorrow, PrayerName.SUBUH, fajrHours)
            (tomorrowSubuh.timestamp - now).coerceAtLeast(0L)
        }

        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val dateString = dateFormat.format(calendar.time)

        return DailyPrayerSchedule(
            dateFormatted = dateString,
            cityName = cityName,
            calculationMethod = method.name,
            imsak = imsakTime.copy(isNext = nextPrayer?.prayer == PrayerName.IMSAK),
            subuh = subuhTime.copy(isNext = nextPrayer?.prayer == PrayerName.SUBUH),
            terbit = terbitTime.copy(isNext = nextPrayer?.prayer == PrayerName.TERBIT),
            dhuha = dhuhaTime.copy(isNext = nextPrayer?.prayer == PrayerName.DHUHA),
            dzuhur = dzuhurTime.copy(isNext = nextPrayer?.prayer == PrayerName.DZUHUR),
            ashar = asharTime.copy(isNext = nextPrayer?.prayer == PrayerName.ASHAR),
            maghrib = maghribTime.copy(isNext = nextPrayer?.prayer == PrayerName.MAGHRIB),
            isya = isyaTime.copy(isNext = nextPrayer?.prayer == PrayerName.ISYA),
            nextPrayer = nextPrayer,
            countdownMillis = countdown
        )
    }

    private fun calculateHourAngle(alpha: Double, latitude: Double, delta: Double): Double {
        val latRad = Math.toRadians(latitude)
        val deltaRad = Math.toRadians(delta)
        val alphaRad = Math.toRadians(alpha)

        val cosHourAngle = (sin(alphaRad) - sin(latRad) * sin(deltaRad)) / (cos(latRad) * cos(deltaRad))
        val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped)) / 15.0
    }

    private fun calculateJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun createPrayerTime(baseCal: Calendar, prayer: PrayerName, decimalHours: Double): SinglePrayerTime {
        val totalMinutes = (decimalHours * 60).roundToInt()
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60

        val cal = baseCal.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val formatted = String.format(Locale.US, "%02d:%02d", hours, minutes)
        return SinglePrayerTime(
            prayer = prayer,
            timeFormatted = formatted,
            timestamp = cal.timeInMillis
        )
    }

    fun formatCountdown(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "%02dj %02dm %02dd", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02dm %02dd", minutes, seconds)
        }
    }
}
