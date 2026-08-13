package com.indolearn.domain.srs

/**
 * محرك التكرار المتباعد (Spaced Repetition Engine).
 *
 * هذا الملف **Kotlin نقي بلا أي اعتماد على Android**، وذلك متعمّد:
 *  1. يسمح باختباره وتشغيله فعلياً بمترجم Kotlin وحده (وقد تم).
 *  2. يفصل "منطق التعلم" عن "إطار العرض" وعن Room.
 *
 * قبل هذا الملف كان المشروع يحتوي على `FlashcardEntity` بحقول
 * `interval` و`easeFactor` و`nextReview` ولكن **بلا أي منطق يستخدمها إطلاقاً**
 * (صفر مستدعين). أي أن التكرار المتباعد كان اسماً بلا وظيفة.
 */

/** حالة إتقان العنصر — تُشتق من الأداء ولا تُكتب يدوياً. */
enum class MasteryState {
    /** لم يُدرس بعد. */
    NEW,

    /** قيد التعلم، الأداء مقبول. */
    LEARNING,

    /** ضعيف: أخطاء متكررة أو دقة منخفضة. */
    WEAK,

    /** منسي: كان معروفاً ثم سقط. */
    FORGOTTEN,

    /** متقن: فترة طويلة + دقة عالية. */
    MASTERED
}

/**
 * تقييم استرجاع المستخدم للعنصر.
 * مبني على مقياس SM-2 لكن مبسّط إلى أربع درجات صالحة لواجهة لمس.
 */
enum class Grade(val score: Int) {
    /** لم أتذكر إطلاقاً. */
    AGAIN(0),

    /** تذكرت بصعوبة. */
    HARD(1),

    /** تذكرت. */
    GOOD(2),

    /** تذكرت فوراً وبسهولة. */
    EASY(3);

    val isCorrect: Boolean get() = this != AGAIN
}

/** نوع العنصر القابل للتعلم — يسمح بجدولة القواعد والجمل، لا الكلمات فقط. */
enum class ItemKind { WORD, GRAMMAR, SENTENCE, EXPRESSION }

/**
 * حالة المراجعة لعنصر واحد. غير قابلة للتغيير (immutable) —
 * كل جدولة تُنتج نسخة جديدة، مما يجعل المنطق قابلاً للاختبار بدقة.
 */
data class ReviewState(
    val itemId: Int,
    val kind: ItemKind,
    val languageCode: String,
    /** عدد النجاحات المتتالية. يُصفَّر عند الخطأ. */
    val repetitions: Int = 0,
    /** الفاصل الحالي بالأيام. 0 يعني "أعده في نفس الجلسة". */
    val intervalDays: Int = 0,
    /** معامل السهولة SM-2، محصور بين 1.3 و 2.5. */
    val easeFactor: Double = DEFAULT_EASE,
    /** موعد الاستحقاق بالمللي ثانية. 0 يعني مستحق الآن. */
    val dueAt: Long = 0L,
    /** عدد مرات النسيان بعد نجاح سابق. */
    val lapses: Int = 0,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
    val lastReviewedAt: Long = 0L
) {
    /** دقة الاسترجاع التاريخية [0.0 .. 1.0]. */
    val accuracy: Double
        get() = if (totalReviews == 0) 0.0 else correctReviews.toDouble() / totalReviews

    /**
     * حالة الإتقان محسوبة، لا مخزّنة — لا يمكن أن تتناقض مع البيانات.
     */
    val mastery: MasteryState
        get() = when {
            totalReviews == 0 -> MasteryState.NEW
            // سقط بعد أن كان راسخاً
            lapses > 0 && repetitions == 0 -> MasteryState.FORGOTTEN
            // ضعيف: أخطاء كثيرة أو دقة متدنية بعد عدد كافٍ من المحاولات
            lapses >= WEAK_LAPSE_THRESHOLD -> MasteryState.WEAK
            totalReviews >= MIN_REVIEWS_FOR_ACCURACY && accuracy < WEAK_ACCURACY -> MasteryState.WEAK
            intervalDays >= MASTERED_INTERVAL_DAYS && accuracy >= MASTERED_ACCURACY -> MasteryState.MASTERED
            else -> MasteryState.LEARNING
        }

    fun isDue(now: Long): Boolean = dueAt <= now

    companion object {
        const val DEFAULT_EASE = 2.5
        const val MIN_EASE = 1.3
        const val MAX_EASE = 2.5
        const val WEAK_LAPSE_THRESHOLD = 2
        const val WEAK_ACCURACY = 0.6
        const val MIN_REVIEWS_FOR_ACCURACY = 3
        const val MASTERED_INTERVAL_DAYS = 21
        const val MASTERED_ACCURACY = 0.8
    }
}

/**
 * مجدول SM-2 معدّل.
 *
 * سبب التعديل عن SM-2 الأصلي:
 *  - SM-2 الأصلي يستخدم مقياس 0..5 وهو غير عملي في واجهة هاتف.
 *  - أضفنا "إعادة داخل الجلسة" (فاصل صفري) للعناصر الفاشلة بدل تأجيلها ليوم كامل،
 *    لأن المتعلم المبتدئ يحتاج تصحيحاً فورياً (Retrieval Practice).
 */
object Sm2Scheduler {

    const val DAY_MILLIS: Long = 24L * 60 * 60 * 1000

    /** إعادة العنصر الفاشل بعد 10 دقائق ضمن نفس الجلسة. */
    const val RELEARN_DELAY_MILLIS: Long = 10L * 60 * 1000

    const val FIRST_INTERVAL_DAYS = 1
    const val SECOND_INTERVAL_DAYS = 6

    /** أقصى فاصل: سنة. يمنع جدولة عناصر إلى ما لا نهاية. */
    const val MAX_INTERVAL_DAYS = 365

    /**
     * يجدول العنصر بعد استرجاع المستخدم.
     *
     * @param state الحالة الحالية.
     * @param grade تقييم الاسترجاع.
     * @param now الوقت الحالي بالمللي ثانية (يُمرَّر صراحةً ليبقى المنطق قابلاً للاختبار).
     */
    fun schedule(state: ReviewState, grade: Grade, now: Long): ReviewState {
        val newEase = adjustEase(state.easeFactor, grade)
        val totalReviews = state.totalReviews + 1
        val correctReviews = state.correctReviews + if (grade.isCorrect) 1 else 0

        if (grade == Grade.AGAIN) {
            // فشل: صفّر السلسلة، سجّل سقوطاً، وأعد العنصر ضمن نفس الجلسة.
            return state.copy(
                repetitions = 0,
                intervalDays = 0,
                easeFactor = newEase,
                dueAt = now + RELEARN_DELAY_MILLIS,
                lapses = state.lapses + 1,
                totalReviews = totalReviews,
                correctReviews = correctReviews,
                lastReviewedAt = now
            )
        }

        val repetitions = state.repetitions + 1
        val intervalDays = when (repetitions) {
            1 -> FIRST_INTERVAL_DAYS
            2 -> SECOND_INTERVAL_DAYS
            else -> {
                val multiplier = when (grade) {
                    Grade.HARD -> HARD_MULTIPLIER
                    Grade.EASY -> newEase * EASY_BONUS
                    else -> newEase
                }
                (state.intervalDays * multiplier).toInt().coerceAtLeast(state.intervalDays + 1)
            }
        }.coerceAtMost(MAX_INTERVAL_DAYS)

        return state.copy(
            repetitions = repetitions,
            intervalDays = intervalDays,
            easeFactor = newEase,
            dueAt = now + intervalDays * DAY_MILLIS,
            totalReviews = totalReviews,
            correctReviews = correctReviews,
            lastReviewedAt = now
        )
    }

    private const val HARD_MULTIPLIER = 1.2
    private const val EASY_BONUS = 1.3

    private fun adjustEase(current: Double, grade: Grade): Double {
        val delta = when (grade) {
            Grade.AGAIN -> -0.20
            Grade.HARD -> -0.15
            Grade.GOOD -> 0.0
            Grade.EASY -> +0.15
        }
        return (current + delta).coerceIn(ReviewState.MIN_EASE, ReviewState.MAX_EASE)
    }

    /** العناصر المستحقة الآن، الأقدم استحقاقاً أولاً. */
    fun due(states: List<ReviewState>, now: Long, limit: Int = Int.MAX_VALUE): List<ReviewState> =
        states.filter { it.isDue(now) }.sortedBy { it.dueAt }.take(limit)
}
