package com.example.service

import com.example.data.model.HIJRI_MONTHS
import com.example.data.model.HijriCalendarDay
import com.example.data.model.HijriDate
import com.example.data.model.ISLAMIC_EVENTS_LIST
import com.example.data.model.IslamicEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor

object HijriCalendarHelper {

    /**
     * Converts a Gregorian Date (Calendar) to Hijri Date with adjustment offset in days.
     */
    fun getHijriDate(calendar: Calendar, adjustmentDays: Int = 0): HijriDate {
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        if (month <= 2) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b - 1524.5 + adjustmentDays

        val jdInt = floor(jd).toLong()
        var l = jdInt - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toLong()
        l = l - 10631 * n + 354
        val j = (floor((10985 - l) / 5316.0).toLong()) * (floor((50 * l) / 17719.0).toLong()) +
                (floor(l / 5670.0).toLong()) * (floor((43 * l) / 15238.0).toLong())
        l = l - (floor((30 - j) / 15.0).toLong()) * (floor((17719 * j) / 50.0).toLong()) -
                (floor(j / 16.0).toLong()) * (floor((15238 * j) / 43.0).toLong()) + 29
        val m = floor((24 * l) / 709.0).toInt()
        val d = (l - floor((709 * m) / 24.0)).toInt()
        val y = (30 * n + j - 30).toInt()

        val monthIndex = (m - 1).coerceIn(0, 11)
        val (monthName, monthArabic) = HIJRI_MONTHS[monthIndex]

        val dayFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))
        val dayName = dayFormat.format(calendar.time)

        val gregFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        val gregFormatted = gregFormat.format(calendar.time)

        return HijriDate(
            day = d,
            month = m,
            year = y,
            monthName = monthName,
            monthNameArabic = monthArabic,
            dayName = dayName,
            gregorianFormatted = gregFormatted
        )
    }

    /**
     * Finds an Islamic event matching the given Hijri day and month.
     */
    fun findEvent(hijriDay: Int, hijriMonth: Int): IslamicEvent? {
        return ISLAMIC_EVENTS_LIST.find { it.hijriDay == hijriDay && it.hijriMonth == hijriMonth }
    }

    /**
     * Generates calendar days grid for a given Gregorian Month.
     */
    fun getMonthGrid(year: Int, monthZeroIndexed: Int, adjustmentDays: Int = 0): List<HijriCalendarDay> {
        val days = mutableListOf<HijriCalendarDay>()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroIndexed)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ...

        // Convert Sunday-first (1) to Monday-first (0..6)
        val leadingEmptyDays = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

        // Previous month filler
        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val maxDaysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in (maxDaysInPrevMonth - leadingEmptyDays + 1)..maxDaysInPrevMonth) {
            prevCal.set(Calendar.DAY_OF_MONTH, i)
            val hDate = getHijriDate(prevCal, adjustmentDays)
            days.add(
                HijriCalendarDay(
                    gregorianDay = i,
                    gregorianMonth = prevCal.get(Calendar.MONTH) + 1,
                    gregorianYear = prevCal.get(Calendar.YEAR),
                    hijriDay = hDate.day,
                    hijriMonth = hDate.month,
                    hijriYear = hDate.year,
                    isCurrentMonth = false,
                    isToday = false,
                    event = findEvent(hDate.day, hDate.month),
                    isAyyamulBidh = hDate.day in 13..15
                )
            )
        }

        // Current month days
        val todayCal = Calendar.getInstance()
        val isThisYearMonth = todayCal.get(Calendar.YEAR) == year && todayCal.get(Calendar.MONTH) == monthZeroIndexed
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        for (i in 1..maxDaysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            val hDate = getHijriDate(cal, adjustmentDays)
            val isToday = isThisYearMonth && (i == todayDay)
            val event = findEvent(hDate.day, hDate.month)

            days.add(
                HijriCalendarDay(
                    gregorianDay = i,
                    gregorianMonth = monthZeroIndexed + 1,
                    gregorianYear = year,
                    hijriDay = hDate.day,
                    hijriMonth = hDate.month,
                    hijriYear = hDate.year,
                    isCurrentMonth = true,
                    isToday = isToday,
                    event = event,
                    isAyyamulBidh = hDate.day in 13..15
                )
            )
        }

        // Next month trailing filler to complete 35 or 42 cells (7 columns)
        val remaining = (7 - (days.size % 7)) % 7
        val nextCal = cal.clone() as Calendar
        nextCal.add(Calendar.MONTH, 1)

        for (i in 1..remaining) {
            nextCal.set(Calendar.DAY_OF_MONTH, i)
            val hDate = getHijriDate(nextCal, adjustmentDays)
            days.add(
                HijriCalendarDay(
                    gregorianDay = i,
                    gregorianMonth = nextCal.get(Calendar.MONTH) + 1,
                    gregorianYear = nextCal.get(Calendar.YEAR),
                    hijriDay = hDate.day,
                    hijriMonth = hDate.month,
                    hijriYear = hDate.year,
                    isCurrentMonth = false,
                    isToday = false,
                    event = findEvent(hDate.day, hDate.month),
                    isAyyamulBidh = hDate.day in 13..15
                )
            )
        }

        return days
    }
}
