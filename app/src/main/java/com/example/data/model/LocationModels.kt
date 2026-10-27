package com.example.data.model

data class CityLocation(
    val name: String,
    val provinceOrCountry: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

val PRESET_CITIES = listOf(
    CityLocation("Jakarta", "DKI Jakarta", -6.2088, 106.8456, "Asia/Jakarta"),
    CityLocation("Surabaya", "Jawa Timur", -7.2575, 112.7521, "Asia/Jakarta"),
    CityLocation("Bandung", "Jawa Barat", -6.9175, 107.6191, "Asia/Jakarta"),
    CityLocation("Medan", "Sumatera Utara", 3.5952, 98.6722, "Asia/Jakarta"),
    CityLocation("Semarang", "Jawa Tengah", -6.9667, 110.4167, "Asia/Jakarta"),
    CityLocation("Makassar", "Sulawesi Selatan", -5.1477, 119.4327, "Asia/Makassar"),
    CityLocation("Palembang", "Sumatera Selatan", -2.9761, 104.7754, "Asia/Jakarta"),
    CityLocation("Yogyakarta", "DI Yogyakarta", -7.7956, 110.3695, "Asia/Jakarta"),
    CityLocation("Banda Aceh", "Aceh", 5.5483, 95.3238, "Asia/Jakarta"),
    CityLocation("Padang", "Sumatera Barat", -0.9471, 100.4172, "Asia/Jakarta"),
    CityLocation("Pekanbaru", "Riau", 0.5071, 101.4478, "Asia/Jakarta"),
    CityLocation("Banjarmasin", "Kalimantan Selatan", -3.3194, 114.5908, "Asia/Makassar"),
    CityLocation("Pontianak", "Kalimantan Barat", -0.0263, 109.3425, "Asia/Pontianak"),
    CityLocation("Samarinda", "Kalimantan Timur", -0.5021, 117.1537, "Asia/Makassar"),
    CityLocation("Balikpapan", "Kalimantan Timur", -1.2379, 116.8529, "Asia/Makassar"),
    CityLocation("Denpasar", "Bali", -8.6705, 115.2126, "Asia/Makassar"),
    CityLocation("Mataram (Lombok)", "Nusa Tenggara Barat", -8.5833, 116.1167, "Asia/Makassar"),
    CityLocation("Kupang", "Nusa Tenggara Timur", -10.1772, 123.6070, "Asia/Makassar"),
    CityLocation("Manado", "Sulawesi Utara", 1.4748, 124.8421, "Asia/Makassar"),
    CityLocation("Ambon", "Maluku", -3.6554, 128.1906, "Asia/Jayapura"),
    CityLocation("Jayapura", "Papua", -2.5916, 140.6690, "Asia/Jayapura"),
    // Global Islamic Centers
    CityLocation("Makkah", "Arab Saudi", 21.4225, 39.8262, "Asia/Riyadh"),
    CityLocation("Madinah", "Arab Saudi", 24.5247, 39.5692, "Asia/Riyadh"),
    CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
    CityLocation("Singapura", "Singapura", 1.3521, 103.8198, "Asia/Singapore"),
    CityLocation("Istanbul", "Turki", 41.0082, 28.9784, "Europe/Istanbul")
)
