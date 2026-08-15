package com.indolearn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.indolearn.data.local.dao.*
import com.indolearn.data.local.entity.*

/**
 * قاعدة بيانات التطبيق.
 *
 * الإصدار 6:
 *  - أُضيف `QuestionAttemptEntity` لحفظ أخطاء كل سؤال وبناء مراجعة أخطاء فعلية.
 *
 * الإصدار 5:
 *  - أُضيف `ReviewStateEntity` (محرك التكرار المتباعد الفعلي).
 *  - أُزيل `FlashcardEntity` — كان هيكلاً بلا أي كود يستخدمه، ويدعم المفردات فقط.
 *  - أُضيف `languageCode` إلى `training_items` و`casual_expressions` و`daily_scenarios`.
 *  - أُعيدت تسمية `lesson_details.lessonId` إلى `unitId` (كانت التسمية مضلِّلة وسبّبت عرض دروس خاطئة).
 *
 * `exportSchema = true` مقصود: بدون تصدير المخطط لا يمكن كتابة اختبارات ترحيل موثوقة.
 */
@Database(
    entities = [
        LessonEntity::class,
        VocabularyEntity::class,
        GrammarEntity::class,
        DialogueEntity::class,
        UserProgressEntity::class,
        ReviewStateEntity::class,
        CasualExpressionEntity::class,
        DailyScenarioEntity::class,
        LessonDetailEntity::class,
        QuizResultEntity::class,
        StageEntity::class,
        TrainingItemEntity::class,
        UnitEntity::class,
        NoteEntity::class,
        QuestionAttemptEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun grammarDao(): GrammarDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun progressDao(): ProgressDao
    abstract fun reviewStateDao(): ReviewStateDao
    abstract fun casualDao(): CasualDao
    abstract fun trainingDao(): TrainingDao
    abstract fun stageDao(): StageDao
    abstract fun unitDao(): UnitDao
    abstract fun lessonDetailDao(): LessonDetailDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun questionAttemptDao(): QuestionAttemptDao
    abstract fun noteDao(): NoteDao
}
