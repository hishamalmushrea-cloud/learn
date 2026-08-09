package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: Int,
    val vocabId: Int,
    val interval: Int = 1,
    val easeFactor: Float = 2.5f,
    val nextReview: Long = System.currentTimeMillis()
)