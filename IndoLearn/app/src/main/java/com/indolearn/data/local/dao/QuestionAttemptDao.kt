package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.indolearn.data.local.entity.QuestionAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionAttemptDao {
    @Insert
    suspend fun insert(attempt: QuestionAttemptEntity)

    /** أحدث الأخطاء أولاً؛ المحاولات الصحيحة تبقى للتشخيص ولا تملأ الشاشة. */
    @Query(
        "SELECT * FROM question_attempts " +
            "WHERE languageCode = :langCode AND isCorrect = 0 " +
            "ORDER BY attemptedAt DESC LIMIT :limit"
    )
    fun observeRecentMistakes(langCode: String, limit: Int = 100): Flow<List<QuestionAttemptEntity>>

    @Query(
        "SELECT COUNT(*) FROM question_attempts " +
            "WHERE languageCode = :langCode AND isCorrect = 0"
    )
    suspend fun countMistakes(langCode: String): Int

    /** يسمح للمستخدم بمسح سجل أخطائه دون المساس بنتائج الاختبارات. */
    @Query("DELETE FROM question_attempts WHERE languageCode = :langCode")
    suspend fun clearLanguageHistory(langCode: String)
}
