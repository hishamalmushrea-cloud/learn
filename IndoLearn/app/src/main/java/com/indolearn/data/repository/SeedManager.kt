package com.indolearn.data.repository

import androidx.room.withTransaction
import com.indolearn.data.local.AppDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يضمن بذر المحتوى **مرة واحدة، بالكامل، وقبل أي قراءة**.
 *
 * ── سبب وجود هذا الملف (عطل حقيقي أبلغ عنه المستخدم) ──
 *
 * الأعراض: فتح «الدروس» أو «المفردات» أو «القواعد» أو «اللغة اليومية»
 * يعرض دوّامة تحميل لا تتوقف، ولا تظهر أي بيانات.
 *
 * السبب الجذري كان ثلاثة عيوب متراكبة:
 *
 * 1) **البذر في المكان الخطأ:** `seedIfNeeded()` كانت تُستدعى من
 *    `HomeViewModel` فقط، بينما كل شاشات المحتوى تستخدم `LearnViewModel`.
 *    ولأن `hiltViewModel()` داخل `NavHost` يُنشئ ViewModel مرتبطاً
 *    بمدخل التنقل، فكل شاشة تحصل على نسخة **جديدة** تبدأ القراءة فوراً.
 *    فإذا وصل المستخدم إلى شاشة محتوى قبل أن ينتهي بذر HomeViewModel
 *    (أو دون المرور بالرئيسية أصلاً) قرأ جداول فارغة.
 *
 * 2) **لا معاملة (transaction):** البذر ~460 صفاً عبر عشرات عمليات الإدراج.
 *    أي انقطاع في المنتصف يترك قاعدة بيانات نصف ممتلئة، ومع ذلك يصبح
 *    `countAny() > 0` صحيحاً، فيُعتبر البذر «منتهياً» إلى الأبد.
 *    نتيجته: جداول ممتلئة وأخرى فارغة بشكل دائم.
 *
 * 3) **سباق (race):** لا شيء يمنع تشغيل البذر مرتين بالتوازي من
 *    ViewModels مختلفة أُنشئت في نفس اللحظة.
 *
 * الحل هنا:
 *  - `Mutex` يمنع التنفيذ المتوازي.
 *  - `withTransaction` يجعل البذر **ذرياً**: إما كامل أو لا شيء.
 *  - `ensureSeeded()` تُستدعى من **كل** ViewModel يقرأ محتوى،
 *    والقراءة لا تبدأ قبل اكتمالها.
 */
@Singleton
class SeedManager @Inject constructor(
    private val db: AppDatabase,
    private val repository: LearnRepository
) {
    private val mutex = Mutex()

    @Volatile
    private var done = false

    /**
     * تضمن اكتمال البذر. آمنة للاستدعاء من عدة أماكن وبالتوازي؛
     * المنفّذ الأول يبذر والبقية تنتظره ثم تعود فوراً.
     */
    suspend fun ensureSeeded() {
        if (done) return
        mutex.withLock {
            if (done) return
            // الفحص داخل القفل: قد يكون منفّذ آخر أنهى البذر أثناء الانتظار.
            if (isComplete()) {
                done = true
                return
            }
            // معاملة واحدة: لا وجود لحالة "نصف مبذور".
            db.withTransaction {
                repository.seedInitialData()
            }
            repository.recomputeProgress(DEFAULT_LANGUAGE)
            done = true
        }
    }

    /**
     * البذر مكتمل فقط إذا امتلأت **كل** الجداول الأساسية.
     *
     * الفحص القديم كان `lessons > 0` وحده، وهو غير كافٍ:
     * قاعدة بيانات فيها دروس بلا مفردات أو بلا تعبيرات كانت تُعتبر جاهزة،
     * فتبقى تلك الشاشات فارغة إلى الأبد.
     */
    private suspend fun isComplete(): Boolean =
        db.lessonDao().countAny() > 0 &&
            db.lessonDetailDao().count() > 0 &&
            db.vocabularyDao().countAny() > 0 &&
            db.casualDao().countAny() > 0 &&
            db.trainingDao().countAny() > 0 &&
            db.stageDao().countAny() > 0

    private companion object {
        const val DEFAULT_LANGUAGE = "ID"
    }
}
