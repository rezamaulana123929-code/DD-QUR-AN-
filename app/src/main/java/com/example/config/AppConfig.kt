package com.example.config

/**
 * DD Qur'an Central Configuration
 * Single Source of Truth for identity, branding, typography, and default preferences.
 */
object AppConfig {
    const val APP_NAME = "DD Qur'an"
    const val TAGLINE = "Teman Ibadah, Setiap Hari."
    const val BASE_API_URL = "https://equran.id/api/v2/"

    // Color definitions (Hex tokens)
    const val PRIMARY_COLOR_HEX = "#064E3B"      // Emerald Green
    const val PRIMARY_DARK_HEX = "#022C22"       // Forest Green
    const val ACCENT_GOLD_HEX = "#D4AF37"        // Elegant Gold
    const val BACKGROUND_LIGHT_HEX = "#F8F3E7"   // Warm Ivory Parchment
    const val SURFACE_LIGHT_HEX = "#FFFFFF"      // Pure White
    const val TEXT_MAIN_HEX = "#1F2937"          // Deep Charcoal
    const val TEXT_MUTED_HEX = "#667085"         // Slate Grey
    const val BORDER_LIGHT_HEX = "#E5DCC8"       // Soft Parchment Border
    const val SUCCESS_HEX = "#166534"
    const val ERROR_HEX = "#B42318"

    // Dark Mode tokens
    const val BACKGROUND_DARK_HEX = "#0F1713"    // Very Dark Greenish Black
    const val SURFACE_DARK_HEX = "#16221C"       // Dark Pine Slate
    const val BORDER_DARK_HEX = "#25382E"        // Dark Border

    // Typography
    const val ARABIC_FONT = "Amiri"
    const val INTERFACE_FONT = "Inter"

    // Default Reading Preferences
    const val DEFAULT_ARABIC_FONT_SIZE_SP = 28f
    const val DEFAULT_TRANSLATION_FONT_SIZE_SP = 14f
    const val DEFAULT_SHOW_LATIN = true
    const val DEFAULT_SHOW_TRANSLATION = true
    const val DEFAULT_AUTO_SCROLL_SPEED = 2 // 1..5
    const val DEFAULT_DAILY_AYAT_TARGET = 20
    const val DEFAULT_QARI_ID = "05" // Misyari Rasyid Al-Afasi

    // Ka'bah / Makkah Coordinates for Qibla calculation
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    // Default Location (Jakarta, Indonesia)
    const val DEFAULT_CITY_NAME = "Jakarta"
    const val DEFAULT_LATITUDE = -6.2088
    const val DEFAULT_LONGITUDE = 106.8456
    const val DEFAULT_TIMEZONE_ID = "Asia/Jakarta"
    const val DEFAULT_CALCULATION_METHOD = "Kemenag RI"
}
