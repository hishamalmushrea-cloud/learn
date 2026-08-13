package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

/** نتيجة تجميع: عدد الدروس في مستوى. */
data class LevelCount(val level: Int, val total: Int)

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE level = :level AND languageCode = :langCode ORDER BY id")
    fun getLessonsByLevel(level: Int, langCode: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: Int): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: Int)

    /** الدروس غير المكتملة بالترتيب — يستخدمها المدرب اليومي لاقتراح الدرس التالي. */
    @Query("SELECT * FROM lessons WHERE languageCode = :langCode AND completed = 0 ORDER BY id")
    suspend fun getIncomplete(langCode: String): List<LessonEntity>

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun countAny(): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE languageCode = :langCode")
    suspend fun countAll(langCode: String): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE languageCode = :langCode AND completed = 1")
    suspend fun countCompleted(langCode: String): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE level = :level AND languageCode = :langCode")
    suspend fun countByLevel(level: Int, langCode: String): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE level = :level AND languageCode = :langCode AND completed = 1")
    suspend fun countCompletedByLevel(level: Int, langCode: String): Int

    @Query("SELECT level, COUNT(*) AS total FROM lessons WHERE languageCode = :langCode GROUP BY level")
    suspend fun countGroupedByLevel(langCode: String): List<LevelCount>
}
