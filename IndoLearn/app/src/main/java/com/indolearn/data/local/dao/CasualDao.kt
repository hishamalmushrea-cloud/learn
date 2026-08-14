package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.CasualExpressionEntity
import com.indolearn.data.local.entity.DailyScenarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CasualDao {
    @Query("SELECT * FROM casual_expressions WHERE category = :category AND languageCode = :langCode ORDER BY id")
    fun getExpressionsByCategory(category: String, langCode: String): Flow<List<CasualExpressionEntity>>

    @Query("SELECT * FROM casual_expressions WHERE languageCode = :langCode ORDER BY id")
    fun getAllExpressions(langCode: String): Flow<List<CasualExpressionEntity>>

    @Query("SELECT COUNT(*) FROM casual_expressions")
    suspend fun countAny(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expressions: List<CasualExpressionEntity>)

    @Query("SELECT * FROM daily_scenarios WHERE languageCode = :langCode ORDER BY id")
    fun getAllScenarios(langCode: String): Flow<List<DailyScenarioEntity>>

    @Query("SELECT * FROM daily_scenarios WHERE languageCode = :langCode")
    suspend fun getAllScenariosOnce(langCode: String): List<DailyScenarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenarios(scenarios: List<DailyScenarioEntity>)
}