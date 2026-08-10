package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.TrainingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_items WHERE category = :category")
    fun getTrainingByCategory(category: String): Flow<List<TrainingItemEntity>>

    @Query("SELECT * FROM training_items ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuizzes(limit: Int): List<TrainingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TrainingItemEntity>)
}