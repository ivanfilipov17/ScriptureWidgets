// data/remote/api/BibleApiService.kt
// Retrofit interface for scripture.api.bible

package com.scripturewidgets.data.remote.api

import com.scripturewidgets.data.remote.dto.VerseResponseDto
import com.scripturewidgets.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BibleApiService {

    /**
     * Fetch a specific verse by ID.
     * Verse ID format: "JHN.3.16"
     * Bible ID for KJV: "de4e12af7f28f599-01"
     */
    @GET("bibles/{bibleId}/verses/{verseId}")
    suspend fun getVerse(
        @Header("api-key") apiKey: String,
        @Path("bibleId") bibleId: String,
        @Path("verseId") verseId: String,
        @Query("content-type") contentType: String = "text",
        @Query("include-notes") includeNotes: Boolean = false,
        @Query("include-titles") includeTitles: Boolean = false,
        @Query("include-chapter-numbers") includeChapterNumbers: Boolean = false,
        @Query("include-verse-numbers") includeVerseNumbers: Boolean = false
    ): VerseResponseDto

    /**
     * Search verses in a Bible translation.
     */
    @GET("bibles/{bibleId}/search")
    suspend fun searchVerses(
        @Header("api-key") apiKey: String,
        @Path("bibleId") bibleId: String,
        @Query("query") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SearchResponseDto
}
