// data/VerseRepositoryImpl.kt
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

    // ── Anti-repeat shuffle queue ─────────────────────────────────
    // Keeps a per-category queue of verse IDs in shuffled order.
    // When the queue empties it refills and reshuffles, guaranteeing
    // every verse is seen before any repeats.
    private val shuffleQueues = mutableMapOf<String, ArrayDeque<String>>()

    private suspend fun nextVerseFromQueue(category: VerseCategory): BibleVerse? {
        val key = category.name
        val queue = shuffleQueues.getOrPut(key) { ArrayDeque() }

        // Refill when empty
        if (queue.isEmpty()) {
            val allIds = if (category == VerseCategory.ALL)
                verseDao.getAllVerseIds()
            else
                verseDao.getVerseIdsByCategory(category.name)

            if (allIds.isEmpty()) return null
            val shuffled = allIds.shuffled()
            queue.addAll(shuffled)
        }

        val nextId = queue.removeFirst()
        return verseDao.getVerseById(nextId)?.toDomain()
    }

    // ── Read Operations ───────────────────────────────────────────
    override fun getAllVerses(): Flow<List<BibleVerse>> =
        verseDao.getAllVerses().map { list -> list.map { it.toDomain() } }

    override fun getVersesByCategory(category: VerseCategory): Flow<List<BibleVerse>> {
        if (category == VerseCategory.ALL) return getAllVerses()
        return verseDao.getVersesByCategory(category.name).map { list -> list.map { it.toDomain() } }
    }

    override fun searchVerses(query: String): Flow<List<BibleVerse>> =
        verseDao.searchVerses(query).map { list -> list.map { it.toDomain() } }

    // Called on every app open — always returns a unique verse from the shuffle queue
    override suspend fun getDailyVerse(): BibleVerse? = nextVerseFromQueue(VerseCategory.ALL)

    // Called when user taps "New" — same queue so it never repeats the current verse
    override suspend fun getRandomVerse(category: VerseCategory): BibleVerse? =
        nextVerseFromQueue(category)

    // ── Favorites ─────────────────────────────────────────────────
    override fun getFavorites(): Flow<List<BibleVerse>> =
        verseDao.getFavoriteVerses().map { list -> list.map { it.toDomain() } }

    override fun isFavorite(verseId: String): Flow<Boolean> = verseDao.isFavorite(verseId)

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

    override suspend fun removeFavorite(verseId: String) = verseDao.deleteFavoriteByVerseId(verseId)

    override suspend fun updateFavoriteNotes(id: String, notes: String) =
        verseDao.updateFavoriteNotes(id, notes)

    // ── API enrichment (optional) ─────────────────────────────────
    private suspend fun fetchFromApi(verseRef: String): BibleVerse? {
        return try {
            if (BuildConfig.BIBLE_API_KEY == "YOUR_API_KEY_HERE") return null
            val response = apiService.getVerse(
                apiKey  = BuildConfig.BIBLE_API_KEY,
                bibleId = KJV_BIBLE_ID,
                verseId = verseRef
            )
            val verse = response.data.toDomain()
            verseDao.insertVerse(verse.toEntity())
            verse
        } catch (e: Exception) {
            Log.d("VerseRepo", "API fetch failed: ${e.message}")
            null
        }
    }

    // ── Mappers ───────────────────────────────────────────────────
    private fun VerseEntity.toDomain() = BibleVerse(
        id          = id,
        book        = book,
        chapter     = chapter,
        verse       = verse,
        text        = text,
        translation = BibleTranslation.fromAbbreviation(translation),
        category    = try { VerseCategory.valueOf(category) } catch (e: Exception) { VerseCategory.ALL }
    )

    private fun BibleVerse.toEntity() = VerseEntity(
        id          = id,
        book        = book,
        chapter     = chapter,
        verse       = verse,
        text        = text,
        translation = translation.abbreviation,
        category    = category.name
    )

    private fun com.scripturewidgets.data.remote.dto.VerseDataDto.toDomain(): BibleVerse {
        val parts   = id.split(".")
        val chapter = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val verse   = parts.getOrNull(2)?.toIntOrNull() ?: 1
        return BibleVerse(
            id          = id,
            book        = bookIdToName(parts.firstOrNull() ?: ""),
            chapter     = chapter,
            verse       = verse,
            text        = content.replace(Regex("<[^>]+>"), "").trim(),
            translation = BibleTranslation.KJV,
            category    = VerseCategory.ALL
        )
    }

    private fun bookIdToName(id: String): String = BOOK_MAP[id] ?: id

    companion object {
        const val KJV_BIBLE_ID = "de4e12af7f28f599-01"
        val BOOK_MAP = mapOf(
            "GEN" to "Genesis",    "EXO" to "Exodus",        "LEV" to "Leviticus",
            "NUM" to "Numbers",    "DEU" to "Deuteronomy",   "JOS" to "Joshua",
            "1SA" to "1 Samuel",   "2SA" to "2 Samuel",      "1KI" to "1 Kings",
            "2KI" to "2 Kings",    "1CH" to "1 Chronicles",  "2CH" to "2 Chronicles",
            "EZR" to "Ezra",       "NEH" to "Nehemiah",      "JOB" to "Job",
            "PSA" to "Psalm",      "PRO" to "Proverbs",      "ECC" to "Ecclesiastes",
            "SNG" to "Song of Solomon", "ISA" to "Isaiah",   "JER" to "Jeremiah",
            "LAM" to "Lamentations","EZK" to "Ezekiel",      "DAN" to "Daniel",
            "HOS" to "Hosea",      "JOL" to "Joel",          "AMO" to "Amos",
            "MIC" to "Micah",      "NAM" to "Nahum",         "HAB" to "Habakkuk",
            "ZEP" to "Zephaniah",  "HAG" to "Haggai",        "ZEC" to "Zechariah",
            "MAL" to "Malachi",    "MAT" to "Matthew",       "MRK" to "Mark",
            "LUK" to "Luke",       "JHN" to "John",          "ACT" to "Acts",
            "ROM" to "Romans",     "1CO" to "1 Corinthians", "2CO" to "2 Corinthians",
            "GAL" to "Galatians",  "EPH" to "Ephesians",     "PHP" to "Philippians",
            "COL" to "Colossians", "1TH" to "1 Thessalonians","2TH" to "2 Thessalonians",
            "1TI" to "1 Timothy",  "2TI" to "2 Timothy",     "TIT" to "Titus",
            "PHM" to "Philemon",   "HEB" to "Hebrews",       "JAS" to "James",
            "1PE" to "1 Peter",    "2PE" to "2 Peter",       "1JN" to "1 John",
            "2JN" to "2 John",     "3JN" to "3 John",        "JUD" to "Jude",
            "REV" to "Revelation"
        )
    }
}
