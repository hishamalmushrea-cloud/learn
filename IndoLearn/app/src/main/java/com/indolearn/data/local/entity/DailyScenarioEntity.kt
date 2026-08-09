package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_scenarios")
data class DailyScenarioEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val titleAr: String,
    val dialogue: String,
    val translation: String,
    val level: Int = 0,
    val category: String
)