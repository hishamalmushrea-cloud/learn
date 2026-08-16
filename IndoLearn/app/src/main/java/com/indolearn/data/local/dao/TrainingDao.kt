package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.TrainingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_items WHERE category = :category AND languageCode = :langCode")
    fun getTrainingByCategory(category: String, langCode: String): Flow<List<TrainingItemEntity>>

    /**
     * أسئلة عشوائية **للغة الحالية فقط**، بلا تكرار.
     *
     * قبل الإصلاح لم يكن الاستعلام يرشّح باللغة، فكان متعلم التركية
     * يحصل على أسئلة إندونيسية في "اختبار سريع".
     *
     * وإصلاح ثانٍ: بعض الأسئلة مكررة عمداً بين تصنيف الموضوع
     * وتصنيف «امتحان» (سؤال المراجعة يعيد سؤال الدرس). بدون
     * GROUP BY كان الاختبار العشوائي قد يعرض **نفس السؤال مرتين**
     * في الجلسة الواحدة. نأخذ الآن نسخة واحدة لكل (سؤال، إجابة).
     */
    @Query(
        """
        SELECT * FROM training_items
        WHERE id IN (
            SELECT MIN(id) FROM training_items
            WHERE languageCode = :langCode
            GROUP BY question, correctAnswer
        )
        ORDER BY RANDOM() LIMIT :limit
        """
    )
    suspend fun getRandomQuizzes(limit: Int, langCode: String): List<TrainingItemEntity>

    @Query("SELECT * FROM training_items WHERE id IN (:ids) AND languageCode = :langCode")
    suspend fun getByIds(ids: List<Int>, langCode: String): List<TrainingItemEntity>

    @Query("SELECT COUNT(*) FROM training_items")
    suspend fun countAny(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TrainingItemEntity>)
}