package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val level: Int,
    val titleAr: String,
    val titleId: String,
    val description: String,
    val content: String,
    val completed: Boolean = false,
    val languageCode: String = "ID"
)
