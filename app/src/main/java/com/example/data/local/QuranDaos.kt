package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT COUNT(*) FROM bookmarks WHERE id = :id")
    suspend fun isBookmarked(id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
}

@Dao
interface ReadingNoteDao {
    @Query("SELECT * FROM reading_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<ReadingNoteEntity>>

    @Query("SELECT * FROM reading_notes WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber ORDER BY timestamp DESC")
    fun getNotesForVerse(surahNumber: Int, verseNumber: Int): Flow<List<ReadingNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ReadingNoteEntity)

    @Update
    suspend fun updateNote(note: ReadingNoteEntity)

    @Delete
    suspend fun deleteNote(note: ReadingNoteEntity)

    @Query("DELETE FROM reading_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<ReadingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history")
    suspend fun clearHistory()
}
