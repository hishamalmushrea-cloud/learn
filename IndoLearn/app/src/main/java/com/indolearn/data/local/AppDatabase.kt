package com.indolearn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.indolearn.data.local.dao.*
import com.indolearn.data.local.entity.*

@Database(
    entities = [
        LessonEntity::class,
        VocabularyEntity::class,
        GrammarEntity::class,
        DialogueEntity::class,
        UserProgressEntity::class,
        FlashcardEntity::class,
        CasualExpressionEntity::class,
        DailyScenarioEntity::class,
        LessonDetailEntity::class,
        QuizResultEntity::class,
        StageEntity::class,
        TrainingItemEntity::class,
        UnitEntity::class,
        NoteEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun grammarDao(): GrammarDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun progressDao(): ProgressDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun casualDao(): CasualDao
    abstract fun trainingDao(): TrainingDao
    abstract fun stageDao(): StageDao
    abstract fun unitDao(): UnitDao
    abstract fun lessonDetailDao(): LessonDetailDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun noteDao(): NoteDao
}