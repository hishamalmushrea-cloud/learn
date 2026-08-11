package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.GrammarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarDao {
    @Query("SELECT * FROM grammar WHERE level = :level AND languageCode = :langCode ORDER BY id")
    fun getGrammarByLevel(level: Int, langCode: String): Flow<List<GrammarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grammar: List<GrammarEntity>)
}
