package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentLevel: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 50,
    val learnedWords: Int = 0,
    val totalWords: Int = 300,
    val lastStudyDate: Long = 0L,
    val streakDays: Int = 0
)