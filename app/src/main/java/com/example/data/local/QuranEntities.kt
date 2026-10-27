package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String, // "$surahNumber:$verseNumber"
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val arabicText: String,
    val translationText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_notes")
data class ReadingNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val noteContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
