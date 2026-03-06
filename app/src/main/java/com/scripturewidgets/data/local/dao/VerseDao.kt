// data/local/dao/VerseDao.kt
package com.scripturewidgets.data.local.dao

import androidx.room.*
import com.scripturewidgets.data.local.entities.FavoriteEntity
import com.scripturewidgets.data.local.entities.VerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {

    // â”€â”€ Verse queries â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Query("SELECT * FROM verses")
    fun getAllVerses(): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE category = :category")
    fun getVersesByCategory(category: String): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE id = :id LIMIT 1")
    suspend fun getVerseById(id: String): VerseEntity?

    @Query("""
        SELECT * FROM verses 
        WHERE text LIKE ''%'' || :query || ''%''
        OR book LIKE ''%'' || :query || ''%''
        OR category LIKE ''%'' || :query || ''%''
    """)
    fun searchVerses(query: String): Flow<List<VerseEntity>>

    // Deterministic daily verse: uses day-of-year mod verse count
    @Query("SELECT * FROM verses LIMIT 1 OFFSET ((:dayOfYear - 1) % (SELECT COUNT(*) FROM verses))")
    suspend fun getDailyVerse(dayOfYear: Int): VerseEntity?

    @Query("SELECT * FROM verses ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerse(): VerseEntity?

    @Query("SELECT * FROM verses WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerseByCategory(category: String): VerseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<VerseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerse(verse: VerseEntity)

    @Query("SELECT COUNT(*) FROM verses")
    suspend fun getVerseCount(): Int

    // â”€â”€ Favorites â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Query("""
        SELECT v.* FROM verses v 
        INNER JOIN favorites f ON v.id = f.verseId
        ORDER BY f.savedDate DESC
    """)
    fun getFavoriteVerses(): Flow<List<VerseEntity>>

    @Query("SELECT * FROM favorites WHERE verseId = :verseId LIMIT 1")
    suspend fun getFavoriteByVerseId(verseId: String): FavoriteEntity?

    @Query("SELECT * FROM favorites ORDER BY savedDate DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    @Query("DELETE FROM favorites WHERE verseId = :verseId")
    suspend fun deleteFavoriteByVerseId(verseId: String)

    @Query("UPDATE favorites SET notes = :notes WHERE id = :id")
    suspend fun updateFavoriteNotes(id: String, notes: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE verseId = :verseId)")
    fun isFavorite(verseId: String): Flow<Boolean>
}
