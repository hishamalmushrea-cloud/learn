package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: Int,
    val stageId: Int,
    val titleAr: String,
    val titleId: String,
    val description: String,
    val isCompleted: Boolean = false
)