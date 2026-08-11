package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dialogues")
data class DialogueEntity(
    @PrimaryKey val id: Int,
    val titleAr: String,
    val titleId: String,
    val content: String,
    val level: Int,
    val languageCode: String = "ID"
)
