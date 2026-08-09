package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey val id: Int,
    val wordId: String,
    val indonesian: String,
    val pronunciation: String,
    val arabic: String,
    val example: String,
    val exampleTranslation: String,
    val category: String,
    val level: Int,
    val isFormal: Boolean = true,
    val favorite: Boolean = false
)