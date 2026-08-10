package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.DialogueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DialogueDao {
    @Query("SELECT * FROM dialogues WHERE level = :level")
    fun getDialoguesByLevel(level: Int): Flow<List<DialogueEntity>>

    @Query("SELECT * FROM dialogues")
    fun getAllDialogues(): Flow<List<DialogueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dialogues: List<DialogueEntity>)
}
