package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stages")
data class StageEntity(
    @PrimaryKey val id: Int,
    val titleAr: String,
    val titleId: String,
    val description: String,
    val level: Int,
    val isUnlocked: Boolean = false
)