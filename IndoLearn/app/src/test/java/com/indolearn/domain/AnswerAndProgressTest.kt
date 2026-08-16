package com.indolearn.domain

import com.indolearn.domain.progress.StudyStreak
import com.indolearn.domain.quiz.AnswerEvaluator
import com.indolearn.domain.quiz.QuestionReviewScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerAndProgressTest {
    @Test
    fun `Turkish letters survive answer normalization`() {
        assertTrue(AnswerEvaluator.isCorrect(" Öğrenciyim! ", "Öğrenciyim"))
    }

    @Test
    fun `partial word is not a correct answer`() {
        assertFalse(AnswerEvaluator.isCorrect("miyor", "Bilmiyorum"))
    }

    @Test
    fun `explicit alternatives are supported`() {
        assertTrue(AnswerEvaluator.isCorrect("لا بأس", "لا مشكلة|لا بأس"))
    }

    @Test
    fun `mistake is reviewed tomorrow and recovery expands intervals`() {
        val start = 1_700_000_000_000L
        var state = QuestionReviewScheduler.schedule(
            QuestionReviewScheduler.newState(42, "ID"),
            correct = false,
            now = start
        )
        assertEquals(start + QuestionReviewScheduler.DAY_MILLIS, state.dueAt)
        state = QuestionReviewScheduler.schedule(state, correct = true, now = state.dueAt)
        assertEquals(3, state.intervalDays)
    }

    @Test
    fun `repeated question error pulls interval back to one day`() {
        val start = 1_700_000_000_000L
        var state = QuestionReviewScheduler.schedule(
            QuestionReviewScheduler.newState(42, "TR"), false, start
        )
        state = QuestionReviewScheduler.schedule(state, true, state.dueAt)
        state = QuestionReviewScheduler.schedule(state, false, state.dueAt)
        assertEquals(1, state.intervalDays)
        assertEquals(0, state.repetitions)
    }

    @Test
    fun `same-day activity does not inflate streak`() {
        val start = 1_700_000_000_000L
        val first = StudyStreak.record(0, 0, start)
        val second = StudyStreak.record(first.days, first.lastStudyAt, start + 60_000)
        assertEquals(1, second.days)
    }

    @Test
    fun `next-day activity increments and missed day resets`() {
        val start = 1_700_000_000_000L
        assertEquals(5, StudyStreak.record(4, start, start + StudyStreak.DAY_MILLIS).days)
        assertEquals(1, StudyStreak.record(4, start, start + 2 * StudyStreak.DAY_MILLIS).days)
    }
}
