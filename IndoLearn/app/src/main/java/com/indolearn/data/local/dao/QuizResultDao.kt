package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results WHERE lessonId = :lessonId ORDER BY date DESC")
    fun getResultsForLesson(lessonId: Int): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE lessonId = :lessonId ORDER BY percentage DESC LIMIT 1")
    suspend fun getBestResult(lessonId: Int): QuizResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: QuizResultEntity)

    @Query("UPDATE quiz_results SET isBest = 0 WHERE lessonId = :lessonId")
    suspend fun clearBestFlag(lessonId: Int)
}