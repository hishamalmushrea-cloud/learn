package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import com.indolearn.domain.srs.ItemKind
import com.indolearn.domain.srs.ReviewState

/**
 * التمثيل المخزَّن لحالة المراجعة.
 *
 * يحل محل `FlashcardEntity` الذي كان هيكلاً معطّلاً:
 *  - كان مفتاحه `id` منفصلاً عن `vocabId` بلا قيد تفرّد، فيسمح بحالات مكررة لنفس الكلمة.
 *  - كان يدعم المفردات فقط، فلا يمكن جدولة قاعدة نحوية أو جملة.
 *  - لم يكن يحمل `languageCode`، فتختلط جدولة الإندونيسية بالتركية.
 *  - لم يُكتب فيه أي كود إطلاقاً.
 *
 * المفتاح المركّب (itemId, kind, languageCode) يمنع التكرار بنيوياً.
 */
@Entity(
    tableName = "review_states",
    primaryKeys = ["itemId", "kind", "languageCode"],
    indices = [Index("languageCode", "dueAt"), Index("languageCode", "kind")]
)
data class ReviewStateEntity(
    val itemId: Int,
    /** اسم ثابت من [ItemKind]. */
    val kind: String,
    val languageCode: String,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = ReviewState.DEFAULT_EASE,
    val dueAt: Long = 0L,
    val lapses: Int = 0,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
    val lastReviewedAt: Long = 0L
) {
    fun toDomain(): ReviewState = ReviewState(
        itemId = itemId,
        kind = runCatching { ItemKind.valueOf(kind) }.getOrDefault(ItemKind.WORD),
        languageCode = languageCode,
        repetitions = repetitions,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        dueAt = dueAt,
        lapses = lapses,
        totalReviews = totalReviews,
        correctReviews = correctReviews,
        lastReviewedAt = lastReviewedAt
    )

    companion object {
        fun fromDomain(s: ReviewState) = ReviewStateEntity(
            itemId = s.itemId,
            kind = s.kind.name,
            languageCode = s.languageCode,
            repetitions = s.repetitions,
            intervalDays = s.intervalDays,
            easeFactor = s.easeFactor,
            dueAt = s.dueAt,
            lapses = s.lapses,
            totalReviews = s.totalReviews,
            correctReviews = s.correctReviews,
            lastReviewedAt = s.lastReviewedAt
        )
    }
}
