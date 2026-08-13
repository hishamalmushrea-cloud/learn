package com.indolearn.domain

import com.indolearn.domain.coach.CoachConfig
import com.indolearn.domain.coach.DailyCoach
import com.indolearn.domain.coach.TaskType
import com.indolearn.domain.srs.Grade
import com.indolearn.domain.srs.ItemKind
import com.indolearn.domain.srs.MasteryState
import com.indolearn.domain.srs.ReviewState
import com.indolearn.domain.srs.Sm2Scheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات محرك التعلم.
 *
 * ملاحظة صدق: هذه الاختبارات مكتوبة بصيغة JUnit4 لتعمل عند توفر Gradle،
 * ولكن **لم يكن ممكناً تشغيل Gradle في بيئة التطوير هذه** (مستودعات Google/Maven محجوبة).
 * لذلك نُفِّذ نفس المنطق فعلياً عبر مُشغّل مستقل في `tools/EngineSpec.kt`
 * باستخدام kotlinc + JDK، وكل الحالات أدناه نجحت هناك.
 */
class LearningEngineTest {

    private val t0 = 1_700_000_000_000L
    private val day = Sm2Scheduler.DAY_MILLIS

    private fun newWord(id: Int = 1) =
        ReviewState(itemId = id, kind = ItemKind.WORD, languageCode = "ID")

    // ---------- SRS ----------

    @Test
    fun `new item is NEW and due immediately`() {
        val s = newWord()
        assertEquals(MasteryState.NEW, s.mastery)
        assertTrue(s.isDue(t0))
    }

    @Test
    fun `first correct answer schedules one day ahead`() {
        val s = Sm2Scheduler.schedule(newWord(), Grade.GOOD, t0)
        assertEquals(1, s.intervalDays)
        assertEquals(t0 + day, s.dueAt)
        assertFalse(s.isDue(t0))
    }

    @Test
    fun `second correct answer schedules six days ahead`() {
        var s = Sm2Scheduler.schedule(newWord(), Grade.GOOD, t0)
        s = Sm2Scheduler.schedule(s, Grade.GOOD, t0 + day)
        assertEquals(6, s.intervalDays)
    }

    @Test
    fun `intervals grow after the second repetition`() {
        var s = newWord()
        var now = t0
        val seen = mutableListOf<Int>()
        repeat(5) {
            s = Sm2Scheduler.schedule(s, Grade.GOOD, now)
            seen += s.intervalDays
            now = s.dueAt
        }
        // must be strictly increasing
        assertEquals(seen.sorted(), seen)
        assertTrue(seen.toSet().size == seen.size)
    }

    @Test
    fun `AGAIN resets streak and reschedules within the session`() {
        var s = Sm2Scheduler.schedule(newWord(), Grade.GOOD, t0)
        s = Sm2Scheduler.schedule(s, Grade.GOOD, t0 + day)
        val before = s.repetitions
        assertTrue(before > 0)

        s = Sm2Scheduler.schedule(s, Grade.AGAIN, t0 + 2 * day)
        assertEquals(0, s.repetitions)
        assertEquals(0, s.intervalDays)
        assertEquals(1, s.lapses)
        // re-shown in-session, NOT pushed a full day away
        assertEquals(t0 + 2 * day + Sm2Scheduler.RELEARN_DELAY_MILLIS, s.dueAt)
    }

    @Test
    fun `lapse after success marks item FORGOTTEN`() {
        var s = Sm2Scheduler.schedule(newWord(), Grade.GOOD, t0)
        s = Sm2Scheduler.schedule(s, Grade.GOOD, t0 + day)
        s = Sm2Scheduler.schedule(s, Grade.AGAIN, t0 + 7 * day)
        assertEquals(MasteryState.FORGOTTEN, s.mastery)
    }

    @Test
    fun `ease factor never escapes its bounds`() {
        var s = newWord()
        var now = t0
        repeat(30) { s = Sm2Scheduler.schedule(s, Grade.AGAIN, now); now += 1000 }
        assertTrue(s.easeFactor >= ReviewState.MIN_EASE)

        var e = newWord()
        now = t0
        repeat(30) { e = Sm2Scheduler.schedule(e, Grade.EASY, now); now = e.dueAt }
        assertTrue(e.easeFactor <= ReviewState.MAX_EASE)
    }

    @Test
    fun `interval is capped to one year`() {
        var s = newWord()
        var now = t0
        repeat(40) { s = Sm2Scheduler.schedule(s, Grade.EASY, now); now = s.dueAt }
        assertTrue(s.intervalDays <= Sm2Scheduler.MAX_INTERVAL_DAYS)
    }

    @Test
    fun `long interval with high accuracy becomes MASTERED`() {
        var s = newWord()
        var now = t0
        repeat(6) { s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt }
        assertTrue(s.intervalDays >= ReviewState.MASTERED_INTERVAL_DAYS)
        assertEquals(MasteryState.MASTERED, s.mastery)
    }

    @Test
    fun `repeated lapses mark item WEAK`() {
        var s = newWord()
        var now = t0
        repeat(3) {
            s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt
            s = Sm2Scheduler.schedule(s, Grade.AGAIN, now); now += 1000
            s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt
        }
        assertTrue(s.lapses >= ReviewState.WEAK_LAPSE_THRESHOLD)
        assertTrue(s.mastery == MasteryState.WEAK || s.mastery == MasteryState.FORGOTTEN)
    }

    @Test
    fun `accuracy is tracked correctly`() {
        var s = newWord()
        var now = t0
        s = Sm2Scheduler.schedule(s, Grade.GOOD, now); now = s.dueAt
        s = Sm2Scheduler.schedule(s, Grade.AGAIN, now); now += 1000
        s = Sm2Scheduler.schedule(s, Grade.GOOD, now)
        assertEquals(3, s.totalReviews)
        assertEquals(2, s.correctReviews)
        assertEquals(2.0 / 3.0, s.accuracy, 0.0001)
    }

    @Test
    fun `due returns only due items sorted by dueAt`() {
        val a = newWord(1).copy(dueAt = t0 - 5000)
        val b = newWord(2).copy(dueAt = t0 - 100)
        val c = newWord(3).copy(dueAt = t0 + 999_999)
        val due = Sm2Scheduler.due(listOf(c, b, a), t0)
        assertEquals(listOf(1, 2), due.map { it.itemId })
    }

    // ---------- Daily Coach ----------

    @Test
    fun `brand new learner is guided to start`() {
        val s = DailyCoach.buildSession(emptyList(), listOf(1, 2), emptyList(), t0)
        assertTrue(s.guidance.contains("لم تبدأ بعد"))
    }

    @Test
    fun `heavy review debt blocks new lessons`() {
        val overdue = (1..20).map {
            newWord(it).copy(
                dueAt = t0 - day, totalReviews = 4, correctReviews = 3, repetitions = 1
            )
        }
        val s = DailyCoach.buildSession(overdue, listOf(101), emptyList(), t0)
        assertFalse("new lesson must be blocked under debt", s.allowsNewLesson)
        assertTrue(s.guidance.contains("لن أرسلك إلى درس جديد"))
    }

    @Test
    fun `light debt allows a new lesson`() {
        val few = (1..2).map {
            newWord(it).copy(dueAt = t0 - day, totalReviews = 3, correctReviews = 3, repetitions = 2)
        }
        val s = DailyCoach.buildSession(few, listOf(101), emptyList(), t0)
        assertTrue(s.allowsNewLesson)
    }

    @Test
    fun `forgotten items are prioritised first`() {
        val forgotten = newWord(1).copy(
            dueAt = t0 - day, lapses = 1, repetitions = 0, totalReviews = 5, correctReviews = 3
        )
        val plain = newWord(2).copy(
            dueAt = t0 - day, repetitions = 2, totalReviews = 3, correctReviews = 3
        )
        val s = DailyCoach.buildSession(listOf(plain, forgotten), emptyList(), emptyList(), t0)
        assertEquals(TaskType.RECOVER_FORGOTTEN, s.tasks.first().type)
        assertEquals(listOf(1), s.tasks.first().itemIds)
    }

    @Test
    fun `session never exceeds its item budget`() {
        val many = (1..100).map {
            newWord(it).copy(dueAt = t0 - day, totalReviews = 2, correctReviews = 1, repetitions = 1)
        }
        val cfg = CoachConfig(maxSessionItems = 15)
        val s = DailyCoach.buildSession(many, emptyList(), emptyList(), t0, cfg)
        val reviewItems = s.tasks
            .filter { it.type != TaskType.NEW_LESSON && it.type != TaskType.SCENARIO_PRACTICE }
            .sumOf { it.size }
        assertTrue("budget exceeded: $reviewItems", reviewItems <= cfg.maxSessionItems)
    }

    @Test
    fun `no item is scheduled twice in one session`() {
        val states = (1..30).map {
            newWord(it).copy(
                dueAt = t0 - day,
                lapses = if (it % 3 == 0) 3 else 0,
                repetitions = if (it % 3 == 0) 0 else 2,
                totalReviews = 6,
                correctReviews = if (it % 3 == 0) 2 else 6
            )
        }
        val s = DailyCoach.buildSession(states, emptyList(), emptyList(), t0)
        val all = s.tasks
            .filter { it.type != TaskType.NEW_LESSON && it.type != TaskType.SCENARIO_PRACTICE }
            .flatMap { it.itemIds }
        assertEquals("duplicate items in session", all.size, all.toSet().size)
    }

    @Test
    fun `all clear produces an encouraging message`() {
        val future = (1..3).map {
            newWord(it).copy(dueAt = t0 + 10 * day, totalReviews = 5, correctReviews = 5, repetitions = 3)
        }
        val s = DailyCoach.buildSession(future, emptyList(), emptyList(), t0)
        assertTrue(s.isEmpty)
        assertTrue(s.guidance.contains("ممتاز"))
    }

    @Test
    fun `weakest items are surfaced worst-first`() {
        val states = listOf(
            newWord(1).copy(totalReviews = 10, correctReviews = 2, lapses = 3),
            newWord(2).copy(totalReviews = 10, correctReviews = 5, lapses = 3),
            newWord(3).copy(totalReviews = 10, correctReviews = 9, repetitions = 4)
        )
        val weak = DailyCoach.weakestByKind(states, ItemKind.WORD)
        assertEquals(listOf(1, 2), weak.map { it.itemId })
    }
}
