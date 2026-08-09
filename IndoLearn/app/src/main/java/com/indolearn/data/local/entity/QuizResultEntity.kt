package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lessonId: Int,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val date: Long = System.currentTimeMillis(),
    val isBest: Boolean = false
)