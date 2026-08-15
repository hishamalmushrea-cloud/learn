package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** محاولة واحدة لسؤال؛ تحفظ الخطأ نفسه لا النتيجة الإجمالية فقط. */
@Entity(
    tableName = "question_attempts",
    indices = [
        Index("languageCode", "isCorrect", "attemptedAt"),
        Index("questionId", "languageCode")
    ]
)
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Int,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val isCorrect: Boolean,
    val questionType: String,
    val category: String,
    val languageCode: String,
    val attemptedAt: Long = System.currentTimeMillis()
)
