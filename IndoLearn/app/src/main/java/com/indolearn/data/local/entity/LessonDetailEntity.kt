package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_details")
data class LessonDetailEntity(
    @PrimaryKey val id: Int,
    val lessonId: Int,
    val explanation: String,
    val wordByWord: String,
    val sentenceStructure: String,
    val dailyUsage: String,
    val commonMistakes: String,
    val formalVsCasual: String
)