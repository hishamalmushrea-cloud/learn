package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.StageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StageDao {
    @Query("SELECT * FROM stages ORDER BY id")
    fun getAllStages(): Flow<List<StageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stages: List<StageEntity>)

    @Query("UPDATE stages SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockStage(id: Int)
}