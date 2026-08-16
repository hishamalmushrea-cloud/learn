package com.indolearn.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.indolearn.data.local.AppDatabase
import com.indolearn.data.repository.LearnRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * الترحيل 4 ← 5.
     *
     * سبب وجوده: كان المشروع يستخدم `fallbackToDestructiveMigration()` وحده،
     * أي أن أي تحديث للتطبيق يغيّر المخطط كان **يمسح بيانات المستخدم بصمت**
     * (المفضلة، الدروس المكتملة، الملاحظات، التقدم).
     * لأن التطبيق يعمل بلا حساب سحابي، البيانات المحذوفة تضيع نهائياً.
     *
     * الملاحظات المكتوبة يدوياً (`notes`) هي الأثمن — لا يمكن إعادة توليدها.
     */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. جدول حالات المراجعة الجديد (محرك التكرار المتباعد).
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS review_states (
                    itemId INTEGER NOT NULL,
                    kind TEXT NOT NULL,
                    languageCode TEXT NOT NULL,
                    repetitions INTEGER NOT NULL DEFAULT 0,
                    intervalDays INTEGER NOT NULL DEFAULT 0,
                    easeFactor REAL NOT NULL DEFAULT 2.5,
                    dueAt INTEGER NOT NULL DEFAULT 0,
                    lapses INTEGER NOT NULL DEFAULT 0,
                    totalReviews INTEGER NOT NULL DEFAULT 0,
                    correctReviews INTEGER NOT NULL DEFAULT 0,
                    lastReviewedAt INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(itemId, kind, languageCode)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_review_states_languageCode_dueAt " +
                    "ON review_states (languageCode, dueAt)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_review_states_languageCode_kind " +
                    "ON review_states (languageCode, kind)"
            )

            // 2. ترحيل ما يمكن إنقاذه من جدول البطاقات القديم.
            //    الجدول القديم لم يكن يُكتب فيه أبداً، لكن الترحيل الدفاعي
            //    أرخص من فقدان بيانات مستخدم في إصدار مستقبلي.
            runCatching {
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO review_states
                        (itemId, kind, languageCode, repetitions, intervalDays,
                         easeFactor, dueAt, lapses, totalReviews, correctReviews, lastReviewedAt)
                    SELECT vocabId, 'WORD', 'ID', 0, interval, easeFactor, nextReview, 0, 0, 0, 0
                    FROM flashcards
                    """.trimIndent()
                )
            }
            db.execSQL("DROP TABLE IF EXISTS flashcards")

            // 3. أعمدة اللغة الجديدة — القيمة الافتراضية 'ID' صحيحة
            //    لأن كل المحتوى الموجود قبل هذا الإصدار كان إندونيسياً.
            addColumnIfMissing(db, "training_items", "languageCode", "TEXT NOT NULL DEFAULT 'ID'")
            addColumnIfMissing(db, "casual_expressions", "languageCode", "TEXT NOT NULL DEFAULT 'ID'")
            addColumnIfMissing(db, "daily_scenarios", "languageCode", "TEXT NOT NULL DEFAULT 'ID'")

            // 4. إعادة تسمية lesson_details.lessonId → unitId.
            //    الاسم القديم كان مضلِّلاً: كان يحمل مُعرِّف الوحدة لا الدرس،
            //    وهو سبب عرض محتوى دروس خاطئة.
            runCatching {
                db.execSQL("ALTER TABLE lesson_details RENAME COLUMN lessonId TO unitId")
            }.onFailure {
                // SQLite أقدم من 3.25 لا يدعم RENAME COLUMN — أعد بناء الجدول.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS lesson_details_new (
                        id INTEGER NOT NULL PRIMARY KEY,
                        unitId INTEGER NOT NULL,
                        explanation TEXT NOT NULL,
                        wordByWord TEXT NOT NULL,
                        sentenceStructure TEXT NOT NULL,
                        dailyUsage TEXT NOT NULL,
                        commonMistakes TEXT NOT NULL,
                        formalVsCasual TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO lesson_details_new " +
                        "SELECT id, lessonId, explanation, wordByWord, sentenceStructure, " +
                        "dailyUsage, commonMistakes, formalVsCasual FROM lesson_details"
                )
                db.execSQL("DROP TABLE lesson_details")
                db.execSQL("ALTER TABLE lesson_details_new RENAME TO lesson_details")
            }
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_lesson_details_unitId ON lesson_details (unitId)"
            )
        }

        private fun addColumnIfMissing(
            db: SupportSQLiteDatabase,
            table: String,
            column: String,
            definition: String
        ) {
            val exists = db.query("PRAGMA table_info($table)").use { c ->
                val nameIdx = c.getColumnIndex("name")
                generateSequence { if (c.moveToNext()) c.getString(nameIdx) else null }
                    .any { it == column }
            }
            if (!exists) {
                db.execSQL("ALTER TABLE $table ADD COLUMN $column $definition")
            }
        }
    }

    /** ترحيل إضافي آمن: جدول مستقل لمحاولات الأسئلة، بلا تعديل لجداول المستخدم الحالية. */
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS question_attempts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    questionId INTEGER NOT NULL,
                    question TEXT NOT NULL,
                    userAnswer TEXT NOT NULL,
                    correctAnswer TEXT NOT NULL,
                    explanation TEXT NOT NULL,
                    isCorrect INTEGER NOT NULL,
                    questionType TEXT NOT NULL,
                    category TEXT NOT NULL,
                    languageCode TEXT NOT NULL,
                    attemptedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_question_attempts_languageCode_isCorrect_attemptedAt " +
                    "ON question_attempts (languageCode, isCorrect, attemptedAt)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_question_attempts_questionId_languageCode " +
                    "ON question_attempts (questionId, languageCode)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "indolearn_db"
        )
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
            // يبقى كشبكة أمان أخيرة للتثبيتات التجريبية القديمة فقط،
            // لكن المسار المعتاد صار ترحيلاً حقيقياً يحفظ البيانات.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLearnRepository(db: AppDatabase): LearnRepository {
        return LearnRepository(db)
    }
}
