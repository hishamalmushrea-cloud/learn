package tools

import com.indolearn.domain.coach.CoachConfig
import com.indolearn.domain.coach.DailyCoach
import com.indolearn.domain.coach.TaskType
import com.indolearn.domain.srs.Grade
import com.indolearn.domain.srs.ItemKind
import com.indolearn.domain.srs.MasteryState
import com.indolearn.domain.srs.ReviewState
import com.indolearn.domain.srs.Sm2Scheduler

/**
 * مُشغّل اختبارات مستقل عن Gradle / JUnit / Android.
 *
 * سبب وجوده: مستودعات Gradle و Google Maven محجوبة في بيئة التطوير هذه،
 * فلا يمكن تشغيل `./gradlew test`. هذا الملف يجمّع نفس منطق الـ domain
 * ويشغّل نفس التأكيدات فعلياً عبر `kotlinc` + `java`، حتى لا يبقى الادعاء
 * بأن "المنطق يعمل" بلا دليل تنفيذي.
 *
 * التشغيل: انظر `tools/run_engine_spec.sh`
 */

private var passed = 0
private var failed = 0
private val failures = mutableListOf<String>()

private fun check(name: String, block: () -> Unit) {
    try {
        block()
        passed++
        println("  ✓ $name")
    } catch (e: Throwable) {
        failed++
        failures += "$name → ${e.message}"
        println("  ✗ $name → ${e.message}")
    }
}

private fun assertTrue(msg: String, cond: Boolean) {
    if (!cond) throw AssertionError(msg)
}

private fun <T> assertEq(expected: T, actual: T) {
    if (expected != actual) throw AssertionError("expected <$expected> but was <$actual>")
}

private fun assertClose(expected: Double, actual: Double, eps: Double) {
    if (kotlin.math.abs(expected - actual) > eps) {
        throw AssertionError("expected <$expected> but was <$actual>")
    }
}

private const val T0 = 1_700_000_000_000L
private val DAY = Sm2Scheduler.DAY_MILLIS

private fun word(id: Int = 1) = ReviewState(itemId = id, kind = ItemKind.WORD, languageCode = "ID")

fun main() {
    println("=".repeat(66))
    println("IndoLearn — Learning Engine Spec (standalone, no Gradle)")
    println("=".repeat(66))

    println("\n[SRS] Spaced Repetition")

    check("new item is NEW and due now") {
        val s = word()
        assertEq(MasteryState.NEW, s.mastery)
        assertTrue("should be due", s.isDue(T0))
    }

    check("first correct -> 1 day") {
        val s = Sm2Scheduler.schedule(word(), Grade.GOOD, T0)
        assertEq(1, s.intervalDays)
        assertEq(T0 + DAY, s.dueAt)
        assertTrue("not due yet", !s.isDue(T0))
    }

    check("second correct -> 6 days") {
        var s = Sm2Scheduler.schedule(word(), Grade.GOOD, T0)
        s = Sm2Scheduler.schedule(s, Grade.GOOD, T0 + DAY)
        assertEq(6, s.intervalDays)
    }

    check("intervals strictly increase") {
        var s = word(); var now = T0
        val seen = mutableListOf<Int>()
        repeat(5) { s = Sm2Scheduler.schedule(s, Grade.GOOD, now); seen += s.intervalDays; now = s.dueAt }
        assertEq(seen.sorted(), seen)
        assertEq(seen.size, seen.toSet().size)
    }

    check("AGAIN resets streak + relearn in-session") {
        var s = Sm2Scheduler.schedule(word(), Grade.GOOD, T0)
        s = Sm2Scheduler.schedule(s, Grade.GOOD, T0 + DAY)
        s = Sm2Scheduler.schedule(s, Grade.AGAIN, T0 + 2 * DAY)
        assertEq(0, s.repetitions)
        assertEq(0, s.intervalDays)
        assertEq(1, s.lapses)
        assertEq(T0 + 2 * DAY + Sm2Scheduler.RELEARN_DELAY_MILLIS, s.dueAt)
    }

    check("lapse after success -> FORGOTTEN") {
        var s = Sm2Scheduler.schedule(word(), Grade.GOOD, T0)
        s = Sm2Scheduler.schedule(s, Grade.GOOD, T0 + DAY)
        s = Sm2Scheduler.schedule(s, Grade.AGAIN, T0 + 7 * DAY)
        assertEq(MasteryState.FORGOTTEN, s.mastery)
    }

    check("ease factor stays within bounds") {
        var s = word(); var now = T0
        repeat(30) { s = Sm2Scheduler.schedule(s, Grade.AGAIN, now); now += 1000 }
        assertTrue("ease below min: ${s.easeFactor}", s.easeFactor >= ReviewState.MIN_EASE)
        var e = word(); now = T0
        repeat(30) { e = Sm2Scheduler.schedule(e, Grade.EASY, now); now = e.dueAt }
        assertTrue("ease above max: ${e.easeFactor}", e.easeFactor <= ReviewState.MAX_EASE)
    }

    check("interval capped at 365 days") {
        var s = word(); var now = T0
        repeat(40) { s = Sm2Scheduler.schedule(s, Grade.EASY, now); now = s.dueAt }
        assertTrue("cap exceeded: ${s.intervalDays}", s.intervalDays <= Sm2Scheduler.MAX_INTERVAL_DAYS)
    }

    check("long interval + high accuracy -> MASTERED") {
        var s = word(); var now = T0
        repeat(6) { s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt }
        assertTrue("interval too small: ${s.intervalDays}", s.intervalDays >= ReviewState.MASTERED_INTERVAL_DAYS)
        assertEq(MasteryState.MASTERED, s.mastery)
    }

    check("repeated lapses -> WEAK or FORGOTTEN") {
        var s = word(); var now = T0
        repeat(3) {
            s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt
            s = Sm2Scheduler.schedule(s, Grade.AGAIN, now); now += 1000
            s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt
        }
        assertTrue("lapses=${s.lapses}", s.lapses >= ReviewState.WEAK_LAPSE_THRESHOLD)
        assertTrue("mastery=${s.mastery}", s.mastery == MasteryState.WEAK || s.mastery == MasteryState.FORGOTTEN)
    }

    check("accuracy tracked correctly") {
        var s = word(); var now = T0
        s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt
        s = Sm2Scheduler.schedule(s, Grade.AGAIN, now); now += 1000
        s = Sm2Scheduler.schedule(s, Grade.GOOD, now)
        assertEq(3, s.totalReviews)
        assertEq(2, s.correctReviews)
        assertClose(2.0 / 3.0, s.accuracy, 0.0001)
    }

    check("due() filters and sorts") {
        val a = word(1).copy(dueAt = T0 - 5000)
        val b = word(2).copy(dueAt = T0 - 100)
        val c = word(3).copy(dueAt = T0 + 999_999)
        assertEq(listOf(1, 2), Sm2Scheduler.due(listOf(c, b, a), T0).map { it.itemId })
    }

    println("\n[COACH] Daily Coach")

    check("brand new learner is guided to start") {
        val s = DailyCoach.buildSession(emptyList(), listOf(1, 2), emptyList(), T0)
        assertTrue("guidance=${s.guidance}", s.guidance.contains("لم تبدأ بعد"))
    }

    check("heavy review debt blocks new lessons") {
        val overdue = (1..20).map {
            word(it).copy(dueAt = T0 - DAY, totalReviews = 4, correctReviews = 3, repetitions = 1)
        }
        val s = DailyCoach.buildSession(overdue, listOf(101), emptyList(), T0)
        assertTrue("new lesson should be blocked", !s.allowsNewLesson)
        assertTrue("guidance=${s.guidance}", s.guidance.contains("لن أرسلك إلى درس جديد"))
    }

    check("light debt allows a new lesson") {
        val few = (1..2).map {
            word(it).copy(dueAt = T0 - DAY, totalReviews = 3, correctReviews = 3, repetitions = 2)
        }
        assertTrue("should allow", DailyCoach.buildSession(few, listOf(101), emptyList(), T0).allowsNewLesson)
    }

    check("forgotten items come first") {
        val forgotten = word(1).copy(dueAt = T0 - DAY, lapses = 1, repetitions = 0, totalReviews = 5, correctReviews = 3)
        val plain = word(2).copy(dueAt = T0 - DAY, repetitions = 2, totalReviews = 3, correctReviews = 3)
        val s = DailyCoach.buildSession(listOf(plain, forgotten), emptyList(), emptyList(), T0)
        assertEq(TaskType.RECOVER_FORGOTTEN, s.tasks.first().type)
        assertEq(listOf(1), s.tasks.first().itemIds)
    }

    check("session respects item budget") {
        val many = (1..100).map {
            word(it).copy(dueAt = T0 - DAY, totalReviews = 2, correctReviews = 1, repetitions = 1)
        }
        val cfg = CoachConfig(maxSessionItems = 15)
        val s = DailyCoach.buildSession(many, emptyList(), emptyList(), T0, cfg)
        val n = s.tasks.filter { it.type != TaskType.NEW_LESSON && it.type != TaskType.SCENARIO_PRACTICE }.sumOf { it.size }
        assertTrue("budget exceeded: $n", n <= cfg.maxSessionItems)
    }

    check("no duplicate item within a session") {
        val states = (1..30).map {
            word(it).copy(
                dueAt = T0 - DAY,
                lapses = if (it % 3 == 0) 3 else 0,
                repetitions = if (it % 3 == 0) 0 else 2,
                totalReviews = 6,
                correctReviews = if (it % 3 == 0) 2 else 6
            )
        }
        val s = DailyCoach.buildSession(states, emptyList(), emptyList(), T0)
        val all = s.tasks.filter { it.type != TaskType.NEW_LESSON && it.type != TaskType.SCENARIO_PRACTICE }.flatMap { it.itemIds }
        assertEq(all.size, all.toSet().size)
    }

    check("all clear -> encouraging message") {
        val future = (1..3).map {
            word(it).copy(dueAt = T0 + 10 * DAY, totalReviews = 5, correctReviews = 5, repetitions = 3)
        }
        val s = DailyCoach.buildSession(future, emptyList(), emptyList(), T0)
        assertTrue("should be empty", s.isEmpty)
        assertTrue("guidance=${s.guidance}", s.guidance.contains("ممتاز"))
    }

    check("weakest surfaced worst-first") {
        val states = listOf(
            word(1).copy(totalReviews = 10, correctReviews = 2, lapses = 3),
            word(2).copy(totalReviews = 10, correctReviews = 5, lapses = 3),
            word(3).copy(totalReviews = 10, correctReviews = 9, repetitions = 4)
        )
        assertEq(listOf(1, 2), DailyCoach.weakestByKind(states, ItemKind.WORD).map { it.itemId })
    }

    println("\n" + "=".repeat(66))
    println("PASSED: $passed    FAILED: $failed")
    if (failures.isNotEmpty()) {
        println("\nFailures:")
        failures.forEach { println("  - $it") }
    }
    println("=".repeat(66))
    if (failed > 0) kotlin.system.exitProcess(1)
}
