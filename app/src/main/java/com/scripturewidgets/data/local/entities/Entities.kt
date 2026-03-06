// data/local/entities/Entities.kt
// Room database entities

package com.scripturewidgets.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verses")
data class VerseEntity(
    @PrimaryKey val id: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val translation: String,
    val category: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val verseId: String,
    val savedDate: Long,
    val notes: String = ""
)
