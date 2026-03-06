// domain/repository/VerseRepository.kt + impl
// Repository pattern: single source of truth

package com.scripturewidgets.domain.repository

import com.scripturewidgets.domain.model.*
import kotlinx.coroutines.flow.Flow

interface VerseRepository {
    fun getAllVerses(): Flow<List<BibleVerse>>
    fun getVersesByCategory(category: VerseCategory): Flow<List<BibleVerse>>
    fun searchVerses(query: String): Flow<List<BibleVerse>>
    suspend fun getDailyVerse(): BibleVerse?
    suspend fun getRandomVerse(category: VerseCategory = VerseCategory.ALL): BibleVerse?
    fun getFavorites(): Flow<List<BibleVerse>>
    fun isFavorite(verseId: String): Flow<Boolean>
    suspend fun addFavorite(verse: BibleVerse, notes: String = "")
    suspend fun removeFavorite(verseId: String)
    suspend fun updateFavoriteNotes(id: String, notes: String)
}
