package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar")
data class GrammarEntity(
    @PrimaryKey val id: Int,
    val titleAr: String,
    val titleId: String,
    val explanation: String,
    val rule: String,
    val examples: String,
    val level: Int
)