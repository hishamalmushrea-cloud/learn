package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary WHERE languageCode = :langCode ORDER BY id")
    fun getAllVocabulary(langCode: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE category = :category AND languageCode = :langCode")
    fun getVocabularyByCategory(category: String, langCode: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE favorite = 1 AND languageCode = :langCode")
    fun getFavorites(langCode: String): Flow<List<VocabularyEntity>>

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun countAny(): Int

    @Query("SELECT COUNT(*) FROM vocabulary WHERE languageCode = :langCode")
    suspend fun countAll(langCode: String): Int

    @Query("SELECT * FROM vocabulary WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<VocabularyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabularyEntity>)

    @Query("UPDATE vocabulary SET favorite = :fav WHERE id = :id")
    suspend fun toggleFavorite(id: Int, fav: Boolean)

    @Query("SELECT id FROM vocabulary WHERE favorite = 1")
    suspend fun getFavoriteIds(): List<Int>

    @Query("UPDATE vocabulary SET favorite = 1 WHERE id IN (:ids)")
    suspend fun restoreFavorites(ids: List<Int>)
}
