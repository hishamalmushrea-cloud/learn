package com.indolearn.domain.coach

import com.indolearn.domain.srs.ItemKind
import com.indolearn.domain.srs.MasteryState
import com.indolearn.domain.srs.ReviewState
import com.indolearn.domain.srs.Sm2Scheduler

/**
 * المدرب اليومي (Daily Coach).
 *
 * يجيب على سؤال واحد: **ماذا يجب أن يفعل المتعلم الآن؟**
 *
 * القاعدة الحاكمة (مطلب المستخدم رقم 27):
 * لا نرسل المتعلم إلى درس جديد بينما لديه ديون مراجعة متراكمة.
 * القرار مبني على بيانات المستخدم، لا عشوائي.
 *
 * Kotlin نقي — قابل للتشغيل والاختبار خارج Android (وقد تم).
 */

/** نوع النشاط الذي يقترحه المدرب. */
enum class TaskType {
    /** استرجاع عناصر منسية — الأولوية القصوى. */
    RECOVER_FORGOTTEN,

    /** تقوية العناصر الضعيفة. */
    DRILL_WEAK,

    /** أسئلة أخطأ فيها المتعلم وحان وقت تثبيتها. */
    REVIEW_MISTAKES,

    /** مراجعة مستحقة عادية. */
    REVIEW_DUE,

    /** تدريب على سيناريو واقعي. */
    SCENARIO_PRACTICE,

    /** درس جديد. */
    NEW_LESSON
}

/**
 * مهمة واحدة داخل جلسة اليوم.
 *
 * @param reason سبب مقروء بالعربية يُعرض للمستخدم — الشفافية جزء من التعليم.
 */
data class LearningItemRef(
    val id: Int,
    val kind: ItemKind,
    val languageCode: String
)

data class CoachTask(
    val type: TaskType,
    val items: List<LearningItemRef>,
    val reason: String
) {
    val size: Int get() = items.size
    /** توافق للعرض القديم؛ لا تستخدمه لجلب كيان دون فحص [items.kind]. */
    val itemIds: List<Int> get() = items.map { it.id }
}

/**
 * خطة الجلسة اليومية.
 */
data class DailySession(
    val tasks: List<CoachTask>,
    /** الرسالة التوجيهية الرئيسية (مطلب "Learning Guide"). */
    val guidance: String,
    val dueCount: Int,
    val weakCount: Int,
    val forgottenCount: Int,
    val masteredCount: Int
) {
    val isEmpty: Boolean get() = tasks.isEmpty()

    /** هل يُسمح بدرس جديد اليوم؟ */
    val allowsNewLesson: Boolean get() = tasks.any { it.type == TaskType.NEW_LESSON }
}

/**
 * إعدادات المدرب. قابلة للضبط بدل أن تكون أرقاماً سحرية مبعثرة في الكود.
 */
data class CoachConfig(
    /** أقصى عدد عناصر في جلسة واحدة — يمنع الإرهاق. */
    val maxSessionItems: Int = 20,
    /** إذا تجاوزت ديون المراجعة هذا الحد، امنع الدروس الجديدة. */
    val reviewDebtThreshold: Int = 12,
    /** أقصى عدد عناصر ضعيفة في تمرين تقوية واحد. */
    val maxWeakDrill: Int = 8,
    /** عدد الدروس الجديدة المسموح بها يومياً. */
    val newLessonsPerDay: Int = 1,
    /** حد صغير يمنع جلسة الأخطاء من التحول إلى عقوبة. */
    val maxMistakeReviews: Int = 5
)

object DailyCoach {

    /**
     * يبني جلسة اليوم من حالة المستخدم الفعلية.
     *
     * @param states حالات المراجعة لكل عناصر اللغة الحالية.
     * @param availableNewLessonIds الدروس غير المكتملة، مرتبة حسب المنهج.
     * @param availableScenarioIds السيناريوهات المتاحة للمستوى الحالي.
     * @param now الوقت الحالي (يُمرَّر صراحةً — لا `System.currentTimeMillis()` مخفي، ليبقى قابلاً للاختبار).
     */
    fun buildSession(
        states: List<ReviewState>,
        availableNewLessonIds: List<Int> = emptyList(),
        availableScenarioIds: List<Int> = emptyList(),
        now: Long,
        config: CoachConfig = CoachConfig()
    ): DailySession {
        val due = Sm2Scheduler.due(states, now)
        val forgotten = states.filter { it.mastery == MasteryState.FORGOTTEN }
        val weak = states.filter { it.mastery == MasteryState.WEAK }
        val mastered = states.count { it.mastery == MasteryState.MASTERED }
        val sessionLanguage = states.firstOrNull()?.languageCode.orEmpty()

        val tasks = mutableListOf<CoachTask>()
        var budget = config.maxSessionItems
        fun refs(values: List<ReviewState>) = values.map {
            LearningItemRef(it.itemId, it.kind, it.languageCode)
        }

        // 1) أسئلة الخطأ المستحقة: حد صغير ومشجّع، ولا تخلط بمعرّفات الكلمات.
        val dueQuestions = due
            .filter { it.kind == ItemKind.QUESTION }
            .sortedWith(compareByDescending<ReviewState> { it.lapses }.thenBy { it.accuracy })
            .take(minOf(config.maxMistakeReviews, budget))
        if (dueQuestions.isNotEmpty()) {
            budget -= dueQuestions.size
            tasks += CoachTask(
                type = TaskType.REVIEW_MISTAKES,
                items = refs(dueQuestions),
                reason = "${dueQuestions.size} نقاط اختارها المدرب من محاولاتك السابقة لتثبيتها اليوم."
            )
        }

        // 2) المنسي أولاً — أعلى قيمة تعليمية، وأسرع ما يُفقد نهائياً.
        val forgottenDue = forgotten
            .filter { it.kind != ItemKind.QUESTION && it.isDue(now) }
            .take(budget)
        if (forgottenDue.isNotEmpty()) {
            budget -= forgottenDue.size
            tasks += CoachTask(
                type = TaskType.RECOVER_FORGOTTEN,
                items = refs(forgottenDue),
                reason = "نسيتَ ${forgottenDue.size} عنصراً كنت تعرفها. استرجاعها الآن أهم من أي شيء آخر."
            )
        }

        // 2) العناصر الضعيفة — تحتاج تكراراً مكثفاً لا مجرد عرض.
        val weakDue = weak
            .filter { it.kind != ItemKind.QUESTION && it.isDue(now) && it !in forgottenDue }
            .sortedBy { it.accuracy }
            .take(minOf(config.maxWeakDrill, budget))
        if (weakDue.isNotEmpty()) {
            budget -= weakDue.size
            tasks += CoachTask(
                type = TaskType.DRILL_WEAK,
                items = refs(weakDue),
                reason = "لديك ${weakDue.size} عنصراً ضعيفاً تتكرر فيها أخطاؤك."
            )
        }

        // 3) المراجعة المستحقة العادية.
        val handled = (dueQuestions + forgottenDue + weakDue).map { it.itemId to it.kind }.toSet()
        val plainDue = due
            .filter { it.kind != ItemKind.QUESTION }
            .filter { (it.itemId to it.kind) !in handled }
            .take(budget)
        if (plainDue.isNotEmpty()) {
            budget -= plainDue.size
            tasks += CoachTask(
                type = TaskType.REVIEW_DUE,
                items = refs(plainDue),
                reason = "${plainDue.size} عنصراً حان موعد مراجعتها اليوم."
            )
        }

        // 4) درس جديد — فقط إذا كانت ديون المراجعة تحت السيطرة.
        val reviewDebt = due.size
        val debtUnderControl = reviewDebt <= config.reviewDebtThreshold
        if (debtUnderControl && availableNewLessonIds.isNotEmpty() && budget > 0) {
            val newOnes = availableNewLessonIds.take(config.newLessonsPerDay)
            tasks += CoachTask(
                type = TaskType.NEW_LESSON,
                items = newOnes.map { LearningItemRef(it, ItemKind.LESSON, sessionLanguage) },
                reason = "مراجعاتك تحت السيطرة — يمكنك التقدّم إلى درس جديد."
            )
        }

        // 5) سيناريو تطبيقي — الاستخدام الحقيقي، لا الحفظ فقط.
        if (availableScenarioIds.isNotEmpty() && tasks.isNotEmpty()) {
            tasks += CoachTask(
                type = TaskType.SCENARIO_PRACTICE,
                items = availableScenarioIds.take(1)
                    .map { LearningItemRef(it, ItemKind.SCENARIO, sessionLanguage) },
                reason = "طبّق ما راجعته في موقف واقعي."
            )
        }

        return DailySession(
            tasks = tasks,
            guidance = buildGuidance(
                mistakes = dueQuestions.size,
                forgotten = forgottenDue.size,
                weak = weakDue.size,
                due = plainDue.size,
                newAllowed = debtUnderControl && availableNewLessonIds.isNotEmpty(),
                reviewDebt = reviewDebt,
                totalTracked = states.size
            ),
            dueCount = due.size,
            weakCount = weak.size,
            forgottenCount = forgotten.size,
            masteredCount = mastered
        )
    }

    /**
     * الرسالة التوجيهية. تشرح **لماذا** هذه هي خطة اليوم.
     */
    private fun buildGuidance(
        mistakes: Int,
        forgotten: Int,
        weak: Int,
        due: Int,
        newAllowed: Boolean,
        reviewDebt: Int,
        totalTracked: Int
    ): String {
        if (totalTracked == 0) {
            return "أهلاً بك 👋 لم تبدأ بعد. لنبدأ بأول درس ونبني أساسك خطوة بخطوة."
        }
        if (mistakes == 0 && forgotten == 0 && weak == 0 && due == 0) {
            return if (newAllowed) {
                "ممتاز ✅ لا توجد مراجعات متأخرة. اليوم وقت مناسب تماماً لدرس جديد."
            } else {
                "ممتاز ✅ أنهيت كل مراجعات اليوم. استرح — التكرار المتباعد يعمل أثناء راحتك."
            }
        }
        val parts = mutableListOf<String>()
        if (mistakes > 0) parts += "$mistakes نقاط من محاولات سابقة"
        if (forgotten > 0) parts += "$forgotten عنصراً منسياً"
        if (weak > 0) parts += "$weak عنصراً ضعيفاً"
        if (due > 0) parts += "$due عنصراً مستحقاً للمراجعة"
        val summary = parts.joinToString(" و")
        return if (!newAllowed && reviewDebt > 0) {
            "لديك $summary. لن أرسلك إلى درس جديد الآن — تثبيت ما تعرفه أهم من إضافة الجديد."
        } else {
            "لديك $summary. لنراجعها أولاً ثم نتقدّم."
        }
    }

    /** أضعف العناصر حسب نوعها — يفيد شاشة التقدم وكشف الضعف. */
    fun weakestByKind(states: List<ReviewState>, kind: ItemKind, limit: Int = 10): List<ReviewState> =
        states.asSequence()
            .filter { it.kind == kind }
            .filter { it.mastery == MasteryState.WEAK || it.mastery == MasteryState.FORGOTTEN }
            .sortedBy { it.accuracy }
            .take(limit)
            .toList()
}
