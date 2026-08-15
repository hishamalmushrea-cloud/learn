package com.indolearn.domain.progress

/** نتيجة تسجيل نشاط تعليمي في السلسلة اليومية. */
data class StreakUpdate(val days: Int, val lastStudyAt: Long)

/**
 * حساب السلسلة بلا اعتماد على Android أو المنطقة الزمنية المخفية.
 * يمرر المستودع رقم اليوم، ويمكن لاحقاً تمرير يوم المنطقة المحلية من UI.
 */
object StudyStreak {
    const val DAY_MILLIS = 86_400_000L

    fun record(
        currentDays: Int,
        lastStudyAt: Long,
        now: Long,
        dayMillis: Long = DAY_MILLIS
    ): StreakUpdate {
        require(dayMillis > 0) { "dayMillis must be positive" }
        val today = now / dayMillis
        val previous = if (lastStudyAt > 0) lastStudyAt / dayMillis else -1L
        val days = when {
            previous == today -> maxOf(1, currentDays)
            previous == today - 1 -> maxOf(1, currentDays + 1)
            else -> 1
        }
        return StreakUpdate(days = days, lastStudyAt = now)
    }
}
