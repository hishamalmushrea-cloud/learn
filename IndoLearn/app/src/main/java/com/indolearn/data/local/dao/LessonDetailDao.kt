package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.LessonDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDetailDao {
    @Query("SELECT * FROM lesson_details WHERE lessonId = :lessonId")
    suspend fun getLessonDetail(lessonId: Int): LessonDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(details: List<LessonDetailEntity>)
}