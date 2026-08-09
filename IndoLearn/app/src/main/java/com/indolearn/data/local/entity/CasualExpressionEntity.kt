package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "casual_expressions")
data class CasualExpressionEntity(
    @PrimaryKey val id: Int,
    val expression: String,
    val pronunciation: String,
    val meaning: String,
    val formality: String,          // 🟢 رسمي | 🔵 يومي | 🟡 غير رسمي | 🟠 عامي
    val usage: String,
    val formalEquivalent: String?,
    val category: String,           // سوق، شارع، أصدقاء، عام
    val level: Int = 0
)