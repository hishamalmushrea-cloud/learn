package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.StageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StageDao {
    @Query("SELECT * FROM stages WHERE languageCode = :langCode ORDER BY id")
    fun getAllStages(langCode: String): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages WHERE languageCode = :langCode ORDER BY id")
    suspend fun getAllStagesOnce(langCode: String): List<StageEntity>

    @Query("SELECT COUNT(*) FROM stages")
    suspend fun countAny(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stages: List<StageEntity>)

    @Query("UPDATE stages SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockStage(id: Int)

    /** يفتح كل المراحل — لا تقييد تدريجي (قرار المستخدم). */
    @Query("UPDATE stages SET isUnlocked = 1 WHERE isUnlocked = 0")
    suspend fun unlockAll()
}
