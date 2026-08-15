package com.indolearn.domain.quiz

import com.indolearn.domain.srs.ItemKind
import com.indolearn.domain.srs.ReviewState
import kotlin.math.max

/**
 * جدولة علاج أخطاء الأسئلة.
 * الخطأ يقرّب المراجعة إلى الغد؛ النجاحات العلاجية تباعدها 3، 7، 14، ثم تصاعدياً.
 * لا تُنشأ الحالة أصلًا للسؤال الذي كان صحيحاً من أول مرة.
 */
object QuestionReviewScheduler {
    const val DAY_MILLIS = 86_400_000L
    const val MAX_INTERVAL_DAYS = 90

    fun newState(questionId: Int, languageCode: String) = ReviewState(
        itemId = questionId,
        kind = ItemKind.QUESTION,
        languageCode = languageCode
    )

    fun schedule(state: ReviewState, correct: Boolean, now: Long): ReviewState {
        require(state.kind == ItemKind.QUESTION) { "Question scheduler only accepts QUESTION states" }
        return if (!correct) {
            state.copy(
                repetitions = 0,
                intervalDays = 1,
                easeFactor = max(ReviewState.MIN_EASE, state.easeFactor - 0.2),
                dueAt = now + DAY_MILLIS,
                lapses = state.lapses + 1,
                totalReviews = state.totalReviews + 1,
                lastReviewedAt = now
            )
        } else {
            val interval = when (state.repetitions) {
                0 -> 3
                1 -> 7
                2 -> 14
                else -> (state.intervalDays * 2).coerceIn(30, MAX_INTERVAL_DAYS)
            }
            state.copy(
                repetitions = state.repetitions + 1,
                intervalDays = interval,
                dueAt = now + interval * DAY_MILLIS,
                totalReviews = state.totalReviews + 1,
                correctReviews = state.correctReviews + 1,
                lastReviewedAt = now
            )
        }
    }
}
