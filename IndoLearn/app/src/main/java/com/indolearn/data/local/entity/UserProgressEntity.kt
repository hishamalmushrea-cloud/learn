package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تقدّم المستخدم.
 *
 * ⚠️ ملاحظة على القيم الافتراضية:
 * كانت `totalLessons = 50` و`totalWords = 300` قيماً ثابتة **خاطئة**
 * (العدد الحقيقي وقتها 55 و197)، ولم تكن تتحدث أبداً لأن `updateProgress`
 * لم يكن يُستدعى من أي مكان. فكانت شاشة التقدم تعرض نِسباً مضللة دائماً.
 *
 * الآن القيم الافتراضية أصفار، وتُحسب فعلياً في
 * [com.indolearn.data.repository.LearnRepository.recomputeProgress]
 * من عدّ الصفوف في قاعدة البيانات. الصفر يعني "لم يُحسب بعد" — وهو
 * أصدق من رقم مخترع.
 */
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentLevel: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val learnedWords: Int = 0,
    val totalWords: Int = 0,
    val lastStudyDate: Long = System.currentTimeMillis(),
    val streakDays: Int = 0
)
