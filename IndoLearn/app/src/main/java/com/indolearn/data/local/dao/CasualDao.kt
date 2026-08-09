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
    @Query("SELECT * FROM casual_expressions WHERE category = :category ORDER BY id")
    fun getExpressionsByCategory(category: String): Flow<List<CasualExpressionEntity>>

    @Query("SELECT * FROM casual_expressions")
    fun getAllExpressions(): Flow<List<CasualExpressionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expressions: List<CasualExpressionEntity>)

    @Query("SELECT * FROM daily_scenarios")
    fun getAllScenarios(): Flow<List<DailyScenarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenarios(scenarios: List<DailyScenarioEntity>)
}