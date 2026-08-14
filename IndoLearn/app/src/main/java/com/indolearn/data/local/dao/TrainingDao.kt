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
     * أسئلة عشوائية **للغة الحالية فقط**.
     * قبل الإصلاح لم يكن الاستعلام يرشّح باللغة، فكان متعلم التركية
     * يحصل على أسئلة إندونيسية في "اختبار سريع".
     */
    @Query("SELECT * FROM training_items WHERE languageCode = :langCode ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuizzes(limit: Int, langCode: String): List<TrainingItemEntity>

    @Query("SELECT COUNT(*) FROM training_items")
    suspend fun countAny(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TrainingItemEntity>)
}