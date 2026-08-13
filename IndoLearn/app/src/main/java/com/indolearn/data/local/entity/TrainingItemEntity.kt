package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_items")
data class TrainingItemEntity(
    @PrimaryKey val id: Int,
    val type: String, // LISTEN_CHOOSE, TRANSLATE, ORDER_WORDS, SITUATION
    val question: String,
    val correctAnswer: String,
    val options: String, // comma separated
    val explanation: String,
    val category: String,
    /**
     * لغة السؤال. أُضيف لإصلاح خلط اللغات:
     * كان استعلام الاختبار يسحب من كل الصفوف بلا ترشيح،
     * فيحصل متعلم التركية على أسئلة إندونيسية.
     */
    val languageCode: String = "ID"
)