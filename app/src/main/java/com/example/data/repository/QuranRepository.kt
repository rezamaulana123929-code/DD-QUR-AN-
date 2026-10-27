package com.example.data.repository

import com.example.data.api.QuranApiService
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.ReadingHistoryEntity
import com.example.data.local.ReadingNoteEntity
import com.example.data.model.AyatItem
import com.example.data.model.SurahDetail
import com.example.data.model.SurahItem
import com.example.data.model.TafsirDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class SearchResultVerse(
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val arabicText: String,
    val translationText: String,
    val matchedTerm: String
)

sealed class ResourceState<out T> {
    object Idle : ResourceState<Nothing>()
    object Loading : ResourceState<Nothing>()
    data class Success<T>(val data: T) : ResourceState<T>()
    data class Error(val message: String) : ResourceState<Nothing>()
}

class QuranRepository(
    private val apiService: QuranApiService,
    private val database: AppDatabase
) {
    // In-memory cache for fast repeated access
    private var cachedSurahs: List<SurahItem>? = null
    private val surahDetailCache = ConcurrentHashMap<Int, SurahDetail>()
    private val tafsirDetailCache = ConcurrentHashMap<Int, TafsirDetail>()

    suspend fun getSurahs(forceRefresh: Boolean = false): Result<List<SurahItem>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedSurahs != null) {
            return@withContext Result.success(cachedSurahs!!)
        }
        try {
            val response = apiService.getAllSurah()
            if (response.code == 200 && response.data != null) {
                cachedSurahs = response.data
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message.ifEmpty { "Gagal memuat daftar surah" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSurahDetail(nomor: Int, forceRefresh: Boolean = false): Result<SurahDetail> = withContext(Dispatchers.IO) {
        if (!forceRefresh && surahDetailCache.containsKey(nomor)) {
            return@withContext Result.success(surahDetailCache[nomor]!!)
        }
        try {
            val response = apiService.getSurahDetail(nomor)
            if (response.code == 200 && response.data != null) {
                surahDetailCache[nomor] = response.data
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message.ifEmpty { "Gagal memuat surah $nomor" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTafsirDetail(nomor: Int, forceRefresh: Boolean = false): Result<TafsirDetail> = withContext(Dispatchers.IO) {
        if (!forceRefresh && tafsirDetailCache.containsKey(nomor)) {
            return@withContext Result.success(tafsirDetailCache[nomor]!!)
        }
        try {
            val response = apiService.getTafsirDetail(nomor)
            if (response.code == 200 && response.data != null) {
                tafsirDetailCache[nomor] = response.data
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message.ifEmpty { "Gagal memuat tafsir surah $nomor" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Room Bookmarks Operations
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = database.bookmarkDao().getAllBookmarks()

    suspend fun isBookmarked(surahNumber: Int, verseNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val id = "$surahNumber:$verseNumber"
        database.bookmarkDao().isBookmarked(id) > 0
    }

    suspend fun toggleBookmark(
        surahNumber: Int,
        surahName: String,
        verseNumber: Int,
        arabicText: String,
        translationText: String
    ): Boolean = withContext(Dispatchers.IO) {
        val id = "$surahNumber:$verseNumber"
        val exists = database.bookmarkDao().isBookmarked(id) > 0
        if (exists) {
            database.bookmarkDao().deleteBookmarkById(id)
            false
        } else {
            database.bookmarkDao().insertBookmark(
                BookmarkEntity(
                    id = id,
                    surahNumber = surahNumber,
                    surahName = surahName,
                    verseNumber = verseNumber,
                    arabicText = arabicText,
                    translationText = translationText
                )
            )
            true
        }
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        database.bookmarkDao().deleteBookmark(bookmark)
    }

    // Room Reading Notes Operations
    fun getAllNotes(): Flow<List<ReadingNoteEntity>> = database.readingNoteDao().getAllNotes()

    fun getNotesForVerse(surahNumber: Int, verseNumber: Int): Flow<List<ReadingNoteEntity>> =
        database.readingNoteDao().getNotesForVerse(surahNumber, verseNumber)

    suspend fun addNote(
        surahNumber: Int,
        surahName: String,
        verseNumber: Int,
        content: String
    ) = withContext(Dispatchers.IO) {
        database.readingNoteDao().insertNote(
            ReadingNoteEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                verseNumber = verseNumber,
                noteContent = content
            )
        )
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        database.readingNoteDao().deleteNoteById(id)
    }

    // Reading History
    fun getRecentHistory(): Flow<List<ReadingHistoryEntity>> =
        database.readingHistoryDao().getRecentHistory()

    suspend fun addHistory(surahNumber: Int, surahName: String, verseNumber: Int) = withContext(Dispatchers.IO) {
        database.readingHistoryDao().insertHistory(
            ReadingHistoryEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                verseNumber = verseNumber
            )
        )
    }

    // Global Search across loaded surahs and surah names
    suspend fun searchVerses(query: String): List<SearchResultVerse> = withContext(Dispatchers.IO) {
        val trimmed = query.trim().lowercase()
        if (trimmed.length < 2) return@withContext emptyList()

        val results = mutableListOf<SearchResultVerse>()

        // Search through any cached surah details
        for ((_, detail) in surahDetailCache) {
            for (ayat in detail.ayat) {
                if (ayat.teksIndonesia.lowercase().contains(trimmed) ||
                    ayat.teksLatin.lowercase().contains(trimmed) ||
                    ayat.teksArab.contains(trimmed)
                ) {
                    results.add(
                        SearchResultVerse(
                            surahNumber = detail.nomor,
                            surahName = detail.namaLatin,
                            verseNumber = ayat.nomorAyat,
                            arabicText = ayat.teksArab,
                            translationText = ayat.teksIndonesia,
                            matchedTerm = query
                        )
                    )
                }
            }
        }
        results
    }
}
