// data/VerseRepositoryImpl.kt
// Concrete repository: offline-first with optional API enrichment

package com.scripturewidgets.data

import android.util.Log
import com.scripturewidgets.BuildConfig
import com.scripturewidgets.data.local.dao.VerseDao
import com.scripturewidgets.data.local.entities.FavoriteEntity
import com.scripturewidgets.data.local.entities.VerseEntity
import com.scripturewidgets.data.remote.api.BibleApiService
import com.scripturewidgets.domain.model.*
import com.scripturewidgets.domain.repository.VerseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerseRepositoryImpl @Inject constructor(
    private val verseDao: VerseDao,
    private val apiService: BibleApiService
) : VerseRepository {

    // â”€â”€ Read Operations (offline-first) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    override fun getAllVerses(): Flow<List<BibleVerse>> =
        verseDao.getAllVerses().map { list -> list.map { it.toDomain() } }

    override fun getVersesByCategory(category: VerseCategory): Flow<List<BibleVerse>> {
        val catStr = if (category == VerseCategory.ALL) {
            return getAllVerses()
        } else category.name
        return verseDao.getVersesByCategory(catStr).map { list -> list.map { it.toDomain() } }
    }

    override fun searchVerses(query: String): Flow<List<BibleVerse>> =
        verseDao.searchVerses(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getDailyVerse(): BibleVerse? {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        // Try offline first
        val offline = verseDao.getDailyVerse(dayOfYear)?.toDomain()

        // Optionally enrich from API (silent fail)
        if (BuildConfig.BIBLE_API_KEY != "YOUR_API_KEY_HERE") {
            try {
                val apiVerse = fetchDailyVerseFromApi(dayOfYear)
                if (apiVerse != null) {
                    verseDao.insertVerse(apiVerse.toEntity())
                    return apiVerse
                }
            } catch (e: Exception) {
                Log.d("VerseRepo", "API unavailable, using offline: ${e.message}")
            }
        }
        return offline
    }

    override suspend fun getRandomVerse(category: VerseCategory): BibleVerse? {
        return if (category == VerseCategory.ALL) {
            verseDao.getRandomVerse()?.toDomain()
        } else {
            verseDao.getRandomVerseByCategory(category.name)?.toDomain()
        }
    }

    // â”€â”€ Favorites â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    override fun getFavorites(): Flow<List<BibleVerse>> =
        verseDao.getFavoriteVerses().map { list -> list.map { it.toDomain() } }

    override fun isFavorite(verseId: String): Flow<Boolean> =
        verseDao.isFavorite(verseId)

    override suspend fun addFavorite(verse: BibleVerse, notes: String) {
        verseDao.insertVerse(verse.toEntity())
        verseDao.insertFavorite(
            FavoriteEntity(
                id = UUID.randomUUID().toString(),
                verseId = verse.id,
                savedDate = System.currentTimeMillis(),
                notes = notes
            )
        )
    }

    override suspend fun removeFavorite(verseId: String) {
        verseDao.deleteFavoriteByVerseId(verseId)
    }

    override suspend fun updateFavoriteNotes(id: String, notes: String) {
        verseDao.updateFavoriteNotes(id, notes)
    }

    // â”€â”€ API helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private suspend fun fetchDailyVerseFromApi(dayOfYear: Int): BibleVerse? {
        val verseRefs = listOf(
            "JHN.3.16", "PSA.23.1", "ROM.8.28", "PHP.4.13", "JER.29.11",
            "ISA.40.31", "PRO.3.5", "MAT.11.28", "ROM.8.38", "PSA.46.1"
        )
        val ref = verseRefs[dayOfYear % verseRefs.size]
        val response = apiService.getVerse(
            apiKey = BuildConfig.BIBLE_API_KEY,
            bibleId = KJV_BIBLE_ID,
            verseId = ref
        )
        return response.data.toDomain()
    }

    // â”€â”€ Mappers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun VerseEntity.toDomain() = BibleVerse(
        id = id,
        book = book,
        chapter = chapter,
        verse = verse,
        text = text,
        translation = BibleTranslation.fromAbbreviation(translation),
        category = try { VerseCategory.valueOf(category) } catch (e: Exception) { VerseCategory.ALL }
    )

    private fun BibleVerse.toEntity() = VerseEntity(
        id = id,
        book = book,
        chapter = chapter,
        verse = verse,
        text = text,
        translation = translation.abbreviation,
        category = category.name
    )

    private fun com.scripturewidgets.data.remote.dto.VerseDataDto.toDomain(): BibleVerse {
        val parts = id.split(".")
        val chapter = if (parts.size > 1) parts[1].toIntOrNull() ?: 1 else 1
        val verse   = if (parts.size > 2) parts[2].toIntOrNull() ?: 1 else 1
        val cleanText = content.replace(Regex("<[^>]+>"), "").trim()
        return BibleVerse(
            id = id,
            book = bookIdToName(parts.firstOrNull() ?: ""),
            chapter = chapter,
            verse = verse,
            text = cleanText,
            translation = BibleTranslation.KJV,
            category = VerseCategory.ALL
        )
    }

    private fun bookIdToName(id: String): String = BOOK_MAP[id] ?: id

    companion object {
        const val KJV_BIBLE_ID = "de4e12af7f28f599-01"
        val BOOK_MAP = mapOf(
            "GEN" to "Genesis", "EXO" to "Exodus", "LEV" to "Leviticus",
            "PSA" to "Psalm", "PRO" to "Proverbs", "ISA" to "Isaiah",
            "JER" to "Jeremiah", "MAT" to "Matthew", "MRK" to "Mark",
            "LUK" to "Luke", "JHN" to "John", "ACT" to "Acts",
            "ROM" to "Romans", "1CO" to "1 Corinthians", "2CO" to "2 Corinthians",
            "GAL" to "Galatians", "EPH" to "Ephesians", "PHP" to "Philippians",
            "COL" to "Colossians", "1TH" to "1 Thessalonians", "2TI" to "2 Timothy",
            "HEB" to "Hebrews", "JAS" to "James", "1PE" to "1 Peter",
            "1JN" to "1 John", "REV" to "Revelation"
        )
    }
}
