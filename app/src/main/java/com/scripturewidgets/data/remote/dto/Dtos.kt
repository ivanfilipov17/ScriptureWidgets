// data/remote/dto/Dtos.kt
// Data Transfer Objects for Bible API responses

package com.scripturewidgets.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerseResponseDto(
    val data: VerseDataDto
)

@Serializable
data class VerseDataDto(
    val id: String,
    val reference: String,
    val content: String,
    @SerialName("bookId") val bookId: String = "",
    @SerialName("chapterId") val chapterId: String = ""
)

@Serializable
data class SearchResponseDto(
    val data: SearchDataDto
)

@Serializable
data class SearchDataDto(
    val verses: List<VerseDataDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 20,
    val offset: Int = 0
)
