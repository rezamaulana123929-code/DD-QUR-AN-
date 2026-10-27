package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseApiResponse<T>(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: T?
)

@JsonClass(generateAdapter = true)
data class SurahItem(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String = "",
    @Json(name = "audioFull") val audioFull: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class AyatItem(
    @Json(name = "nomorAyat") val nomorAyat: Int,
    @Json(name = "teksArab") val teksArab: String,
    @Json(name = "teksLatin") val teksLatin: String = "",
    @Json(name = "teksIndonesia") val teksIndonesia: String = "",
    @Json(name = "audio") val audio: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SurahDetail(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String = "",
    @Json(name = "audioFull") val audioFull: Map<String, String>? = null,
    @Json(name = "ayat") val ayat: List<AyatItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TafsirItem(
    @Json(name = "ayat") val ayat: Int,
    @Json(name = "teks") val teks: String
)

@JsonClass(generateAdapter = true)
data class TafsirDetail(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String,
    @Json(name = "arti") val arti: String,
    @Json(name = "deskripsi") val deskripsi: String = "",
    @Json(name = "tafsir") val tafsir: List<TafsirItem> = emptyList()
)

data class QariInfo(
    val id: String,
    val name: String,
    val style: String
)

val AVAILABLE_QARIS = listOf(
    QariInfo("01", "Abdullah Al-Juhany", "Imam Masjidil Haram Makkah"),
    QariInfo("02", "Abdul-Muhsin Al-Qasim", "Imam Masjid Nabawi Madinah"),
    QariInfo("03", "Abdurrahman as-Sudais", "Ketua Umum Urusan Dua Masjid Suci"),
    QariInfo("04", "Ibrahim Al-Dossari", "Qari & Akademisi Al-Qur'an"),
    QariInfo("05", "Misyari Rasyid Al-Afasi", "Qari Internasional Kuwait"),
    QariInfo("06", "Yasser Al-Dosari", "Imam Masjidil Haram Makkah")
)
