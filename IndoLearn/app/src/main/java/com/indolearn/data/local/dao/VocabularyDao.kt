package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY id")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE category = :category")
    fun getVocabularyByCategory(category: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE favorite = 1")
    fun getFavorites(): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabularyEntity>)

    @Query("UPDATE vocabulary SET favorite = :fav WHERE id = :id")
    suspend fun toggleFavorite(id: Int, fav: Boolean)
}