package com.example.service

import com.example.config.AppConfig
import kotlin.math.*

object QiblaCalculator {

    /**
     * Calculates the initial bearing (forward azimuth) from current position to Kaaba.
     * Returned in degrees: 0° .. 360° (0° = North, 90° = East, 180° = South, 270° = West).
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Double {
        val lat1 = Math.toRadians(userLat)
        val lng1 = Math.toRadians(userLng)
        val lat2 = Math.toRadians(AppConfig.KAABA_LATITUDE)
        val lng2 = Math.toRadians(AppConfig.KAABA_LONGITUDE)

        val deltaLng = lng2 - lng1

        val y = sin(deltaLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)

        var initialBearing = Math.toDegrees(atan2(y, x))
        // Normalize to 0° - 360°
        initialBearing = (initialBearing + 360.0) % 360.0

        return initialBearing
    }

    /**
     * Calculates great-circle distance to Kaaba in Kilometers using Haversine formula.
     */
    fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(AppConfig.KAABA_LATITUDE - userLat)
        val dLng = Math.toRadians(AppConfig.KAABA_LONGITUDE - userLng)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(AppConfig.KAABA_LATITUDE)) *
                sin(dLng / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Calculates the relative angle for the compass needle pointing to Kaaba.
     * When needleAngle is around 0° (e.g. -3°..+3°), device is pointing directly towards Kaaba.
     */
    fun calculateRelativeQiblaAngle(qiblaBearing: Double, currentHeading: Double): Double {
        var diff = qiblaBearing - currentHeading
        while (diff > 180.0) diff -= 360.0
        while (diff < -180.0) diff += 360.0
        return diff
    }

    /**
     * Smooth angle transition to prevent needle flickering or 359° <-> 1° jumps.
     */
    fun smoothDegree(current: Float, target: Float, factor: Float = 0.15f): Float {
        var diff = target - current
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        return (current + diff * factor + 360f) % 360f
    }
}
