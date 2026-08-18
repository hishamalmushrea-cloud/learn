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

/**
 * معرّف وهمي لنتائج الاختبار المختلط (غير المرتبط بدرس بعينه).
 *
 * سبب وجوده: كانت شاشة الاختبار السريع تحفظ النتيجة بـ `lessonId = 0`،
 * وهو رقم سحري يصطدم دلالياً بالدروس الحقيقية — واستعلام
 * `getResultsForLesson(0)` كان سيخلط نتائج الاختبار العام بدرس ما.
 * القيمة سالبة فلا يمكن أن تطابق أي معرّف درس حقيقي.
 */
const val MIXED_QUIZ_LESSON_ID: Int = -1

/** نتيجة اختبار انتقال A0؛ سالب حتى لا يصطدم بمعرّف درس. */
const val A0_EXIT_QUIZ_LESSON_ID: Int = -3
