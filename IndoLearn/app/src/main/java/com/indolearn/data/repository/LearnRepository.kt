package com.indolearn.data.repository

import com.indolearn.data.local.AppDatabase
import com.indolearn.data.local.entity.*
import com.indolearn.domain.srs.Grade
import com.indolearn.domain.srs.ItemKind
import com.indolearn.domain.srs.ReviewState
import com.indolearn.domain.srs.Sm2Scheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LearnRepository(private val db: AppDatabase) {

    // Lessons
    fun getLessons(level: Int, langCode: String) = db.lessonDao().getLessonsByLevel(level, langCode)

    /**
     * يُكمل الدرس ثم **يعيد احتساب التقدم فوراً**.
     *
     * قبل الإصلاح كان `markLessonCompleted` يحدّث عمود `completed` فقط،
     * بينما `user_progress` لا يُحدَّث أبداً (صفر مستدعين لـ `updateProgress`)،
     * فتبقى شاشة التقدم على 0 مهما أنجز المستخدم.
     */
    suspend fun markLessonCompleted(id: Int, langCode: String) {
        db.lessonDao().markCompleted(id)
        recomputeProgress(langCode)
    }

    // Vocabulary
    fun getAllVocabulary(langCode: String) = db.vocabularyDao().getAllVocabulary(langCode)
    fun getFavorites(langCode: String) = db.vocabularyDao().getFavorites(langCode)
    suspend fun toggleFavorite(id: Int, fav: Boolean) = db.vocabularyDao().toggleFavorite(id, fav)

    // Grammar
    fun getGrammar(level: Int, langCode: String) = db.grammarDao().getGrammarByLevel(level, langCode)

    // Dialogues
    fun getAllDialogues(langCode: String): Flow<List<DialogueEntity>> = db.dialogueDao().getAllDialogues(langCode)

    // Stages
    fun getAllStages(langCode: String): Flow<List<StageEntity>> = db.stageDao().getAllStages(langCode)

    // Progress
    fun getUserProgress() = db.progressDao().getProgress()
    suspend fun updateProgress(progress: UserProgressEntity) = db.progressDao().updateProgress(progress)

    /**
     * يعيد احتساب التقدم من الحقائق المخزّنة، لا من أرقام ثابتة.
     *
     * كانت `UserProgressEntity` تستخدم `totalLessons = 50` و`totalWords = 300`
     * وهما رقمان **خاطئان** (الحقيقي 55 و197)، ولا يتغيران عند إضافة محتوى.
     */
    suspend fun recomputeProgress(langCode: String) {
        val totalLessons = db.lessonDao().countAll(langCode)
        val completed = db.lessonDao().countCompleted(langCode)
        val totalWords = db.vocabularyDao().countAll(langCode)
        val learned = db.reviewStateDao().countLearned(langCode)

        val current = db.progressDao().getProgressOnce() ?: UserProgressEntity()
        db.progressDao().updateProgress(
            current.copy(
                completedLessons = completed,
                totalLessons = totalLessons,
                learnedWords = learned,
                totalWords = totalWords,
                lastStudyDate = System.currentTimeMillis()
            )
        )
        unlockReachedStages(langCode)
    }

    /**
     * يفتح المرحلة التالية عند إتمام نسبة كافية من الحالية.
     *
     * قبل الإصلاح: `unlockStage` مُعرَّفة بلا أي مستدعٍ، فكانت المراحل 2..5
     * مقفلة إلى الأبد و11 درساً غير قابلة للوصول.
     */
    suspend fun unlockReachedStages(langCode: String, threshold: Double = 0.7) {
        val stages = db.stageDao().getAllStagesOnce(langCode).sortedBy { it.level }
        for (stage in stages) {
            val total = db.lessonDao().countByLevel(stage.level, langCode)
            if (total == 0) continue
            val done = db.lessonDao().countCompletedByLevel(stage.level, langCode)
            if (done.toDouble() / total >= threshold) {
                stages.firstOrNull { it.level == stage.level + 1 }
                    ?.takeIf { !it.isUnlocked }
                    ?.let { db.stageDao().unlockStage(it.id) }
            }
        }
    }

    // Lesson details
    suspend fun getLessonById(id: Int) = db.lessonDao().getLessonById(id)
    suspend fun getLessonDetail(id: Int) = db.lessonDetailDao().getLessonDetail(id)

    // Quizzes
    suspend fun getRandomQuizzes(limit: Int, langCode: String) =
        db.trainingDao().getRandomQuizzes(limit, langCode)

    /** يحفظ نتيجة الاختبار — لم تكن تُحفظ إطلاقاً قبل الإصلاح. */
    suspend fun saveQuizResult(lessonId: Int, score: Int, total: Int) {
        val percentage = if (total == 0) 0 else (score * 100) / total
        val best = db.quizResultDao().getBestResult(lessonId)
        val isBest = best == null || percentage > best.percentage
        if (isBest) db.quizResultDao().clearBestFlag(lessonId)
        db.quizResultDao().insertResult(
            QuizResultEntity(
                lessonId = lessonId,
                score = score,
                totalQuestions = total,
                percentage = percentage,
                isBest = isBest
            )
        )
    }

    fun getQuizHistory(lessonId: Int) = db.quizResultDao().getResultsForLesson(lessonId)

    // ---------- Spaced repetition ----------

    /** كل حالات المراجعة للغة الحالية، كنماذج domain نقية. */
    fun getReviewStates(langCode: String): Flow<List<ReviewState>> =
        db.reviewStateDao().getAllForLanguage(langCode).map { rows -> rows.map { it.toDomain() } }

    suspend fun getReviewStatesOnce(langCode: String): List<ReviewState> =
        db.reviewStateDao().getAllForLanguageOnce(langCode).map { it.toDomain() }

    /**
     * يسجّل استرجاعاً واحداً ويعيد جدولة العنصر عبر محرك SM-2.
     *
     * هذه هي الحلقة المفقودة التي جعلت `FlashcardEntity` هيكلاً بلا وظيفة:
     * كانت الحقول `interval` و`easeFactor` و`nextReview` موجودة بلا أي كود يكتب فيها.
     */
    suspend fun recordReview(
        itemId: Int,
        kind: ItemKind,
        langCode: String,
        grade: Grade,
        now: Long = System.currentTimeMillis()
    ): ReviewState {
        val existing = db.reviewStateDao().find(itemId, kind.name, langCode)?.toDomain()
            ?: ReviewState(itemId = itemId, kind = kind, languageCode = langCode)
        val updated = Sm2Scheduler.schedule(existing, grade, now)
        db.reviewStateDao().upsert(ReviewStateEntity.fromDomain(updated))
        return updated
    }

    suspend fun countDueReviews(langCode: String, now: Long = System.currentTimeMillis()): Int =
        db.reviewStateDao().countDue(langCode, now)

    /** مُعرِّفات الدروس غير المكتملة بالترتيب — مدخل للمدرب اليومي. */
    suspend fun getIncompleteLessonIds(langCode: String): List<Int> =
        db.lessonDao().getIncomplete(langCode).map { it.id }

    /**
     * عدد الدروس في كل مستوى.
     *
     * تحتاجه شاشة المنهج لتقول الحقيقة: المراحل 3 و4 و5 معروضة للمستخدم
     * لكنها **لا تحتوي على أي درس**. بدون هذا العدّ كان المستخدم يفتحها
     * (بعد تفعيل نظام الفتح) فيجد شاشة فارغة بلا تفسير.
     */
    suspend fun getLessonCountsByLevel(langCode: String): Map<Int, Int> =
        db.lessonDao().countGroupedByLevel(langCode).associate { it.level to it.total }

    suspend fun getScenarioIds(langCode: String): List<Int> =
        db.casualDao().getAllScenariosOnce(langCode).map { it.id }

    /**
     * يجلب المفردات بالمعرّفات مع الحفاظ على ترتيب الأولوية القادم من المدرب.
     * (استعلام `IN` في SQL لا يضمن ترتيب المدخلات.)
     */
    suspend fun getVocabularyByIds(ids: List<Int>): List<VocabularyEntity> {
        if (ids.isEmpty()) return emptyList()
        val byId = db.vocabularyDao().getByIds(ids).associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    // Scenarios / casual (language-aware)
    fun getCasualExpressions(langCode: String) = db.casualDao().getAllExpressions(langCode)
    fun getScenarios(langCode: String) = db.casualDao().getAllScenarios(langCode)

    // Notebook Notes
    fun getAllNotes() = db.noteDao().getAllNotes()
    suspend fun saveNote(note: NoteEntity) = db.noteDao().insertNote(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)

    /**
     * يبذر المحتوى **مرة واحدة فقط**.
     *
     * ⚠️ العطل الذي أصلحه هذا الحارس (P0-2):
     * كانت `seedInitialData()` تُستدعى من `HomeViewModel.init` بلا شرط،
     * وكل عمليات الإدراج تستخدم `OnConflictStrategy.REPLACE`.
     * فكان كل فتح للشاشة الرئيسية يُعيد كتابة ~450 صفاً ويمسح:
     *   - `lessons.completed`  (الدروس المكتملة)
     *   - `vocabulary.favorite` (المفضلة)
     *   - `user_progress`       (التقدم بالكامل)
     * أي أن تقدّم المستخدم كان يُدمَّر في كل تشغيل.
     */
    suspend fun seedIfNeeded() {
        if (db.lessonDao().countAny() > 0) return
        seedInitialData()
        recomputeProgress("ID")
    }

    /** يبذر البيانات. عام لأغراض الاختبار؛ الاستخدام العادي عبر [seedIfNeeded]. */
    suspend fun seedInitialData() {
        // Level 0 and Level 1 Lessons (Complete Curriculum)
        val lessons = listOf(
            LessonEntity(1, 0, "التحيات", "Salam", "تعلم التحيات الأساسية", "content1", false),
            LessonEntity(2, 0, "التعارف", "Perkenalan", "تقديم النفس", "content2", false),
            LessonEntity(3, 0, "الأرقام 1-10", "Angka 1-10", "تعلم الأرقام", "content3", false),
            LessonEntity(4, 0, "الأيام", "Hari", "أيام الأسبوع", "content4", false),
            LessonEntity(5, 0, "الضمائر", "Kata Ganti", "أنا، أنت، هو...", "content5", false),
            LessonEntity(6, 0, "الأفعال الأساسية", "Kata Kerja Dasar", "makan, minum, pergi...", "content6", false),
            LessonEntity(7, 0, "النفي", "Negasi", "tidak, bukan...", "content7", false),
            LessonEntity(8, 0, "السؤال", "Pertanyaan", "apa, siapa, di mana...", "content8", false),
            LessonEntity(9, 0, "الأرقام المتقدمة", "Angka Lanjut", "sepuluh, dua puluh...", "content9", false),
            LessonEntity(10, 0, "الوقت والتاريخ", "Waktu & Tanggal", "الساعة واليوم والمستقبل...", "content10", false),
            LessonEntity(11, 0, "الأفعال الأساسية 2", "Kata Kerja Dasar 2", "mau, suka, pulang...", "content11", false),
            LessonEntity(12, 0, "الضمائر المتقدمة", "Kata Ganti Lanjut", "kami, kita, Anda...", "content12", false),
            LessonEntity(13, 0, "النفي المتقدم", "Negasi Lanjut", "belum, jangan...", "content13", false),
            LessonEntity(14, 0, "أدوات الاستفهام 2", "Kata Tanya Lanjut", "kenapa, bagaimana...", "content14", false),
            LessonEntity(15, 0, "الأرقام المتقدمة 2", "Angka Lanjut 2", "sepuluh, dua puluh...", "content15", false),
            LessonEntity(16, 0, "الوقت والتاريخ 2", "Waktu Lanjut 2", "الساعة واليوم والمستقبل...", "content16", false),
            LessonEntity(17, 0, "الملكية", "Kepemilikan", "rumah saya, buku kamu...", "content17", false),
            LessonEntity(18, 0, "الملكية 2", "Kepemilikan 2", "اختصارات الملكية...", "content18", false),
            LessonEntity(19, 0, "الصفات", "Kata Sifat", "besar, kecil, bagus...", "content19", false),
            LessonEntity(20, 0, "حروف الجر", "Preposisi", "di, ke, dari...", "content20", false),
            LessonEntity(21, 0, "الأسرة", "Keluarga", "ayah, ibu, kakak...", "content21", false),
            LessonEntity(22, 0, "الأشياء اليومية", "Benda", "meja, kursi, buku...", "content22", false),
            LessonEntity(23, 0, "مراجعة المرحلة الأولى", "Review", "مراجعة شاملة للمرحلة الأولى", "content23", false),
            
            // Stage 2 (Level 1)
            LessonEntity(24, 1, "توسيع تكوين الجملة", "Kalimat Panjang", "جمل أطول + عناصر متعددة", "content24", false),
            LessonEntity(25, 1, "التعبير عن الزمن", "Waktu", "sudah, sedang, akan, belum, pernah", "content25", false),
            LessonEntity(26, 1, "الأفعال اليومية المتقدمة", "Kata Kerja Lanjut", "bekerja, mencari, membawa...", "content26", false),
            LessonEntity(27, 1, "القدرة والرغبة والوجوب", "Bisa, Mau, Harus", "bisa, mau, harus, boleh", "content27", false),
            LessonEntity(28, 1, "مقدمة البادئات", "Awalan Dasar", "me-, ber-, di-", "content28", false),
            LessonEntity(29, 1, "meN- بالتفصيل", "meN-", "membeli, menulis, memakai, menyapu", "content29", false),
            LessonEntity(30, 1, "di- و me-", "Aktif & Pasif", "Saya membeli vs Buku dibeli", "content30", false),
            LessonEntity(31, 1, "المقارنة والتفضيل", "Perbandingan", "lebih, paling, sangat, terlalu", "content31", false),
            LessonEntity(32, 1, "ربط الجمل", "Penghubung", "dan, tetapi, karena, kalau", "content32", false),
            LessonEntity(33, 1, "اللغة اليومية", "Bahasa Sehari-hari", "nggak, udah, dong, banget", "content33", false),
            LessonEntity(34, 1, "السوق والبيع", "Belanja", "Berapa, bisa kurang, harga teman", "content34", false),
            LessonEntity(35, 1, "العمل والحياة", "Kerja", "bekerja, kantor, sibuk, terlambat", "content35", false),
            LessonEntity(36, 1, "المحادثات", "Percakapan", "حوارات واقعية", "content36", false),
            LessonEntity(37, 1, "التفكير بالإندونيسية", "Berpikir", "إنتاج جمل ومواقف", "content37", false)
        )
        db.lessonDao().insertAll(lessons)

        // Vocabulary
        val vocab = listOf(
            VocabularyEntity(1, "halo", "halo", "ها لو", "مرحبا", "Halo, apa kabar?", "مرحبا، كيف حالك؟", "تحيات", 0, true),
            VocabularyEntity(2, "terima kasih", "terima kasih", "تيريما كاسيه", "شكراً", "Terima kasih banyak.", "شكراً جزيلاً.", "تحيات", 0, true),
            VocabularyEntity(3, "saya", "saya", "سايا", "أنا", "Saya dari Yaman.", "أنا من اليمن.", "تعارف", 0, true),
            VocabularyEntity(4, "makan", "makan", "ماكان", "يأكل", "Saya makan nasi.", "أنا آكل الأرز.", "أفعال", 0, true),
            VocabularyEntity(5, "minum", "minum", "مينوم", "يشرب", "Saya minum air.", "أنا أشرب الماء.", "أفعال", 0, true),
            VocabularyEntity(6, "tidur", "tidur", "تيدور", "ينام", "Saya mau tidur.", "أريد أن أنام.", "أفعال", 0, true),
        )
        db.vocabularyDao().insertAll(vocab)

        // Grammar
        val grammar = listOf(
            GrammarEntity(1, "ترتيب الجملة", "Susunan Kalimat", "الفاعل + الفعل + المفعول", "S + V + O", "Saya makan nasi.", 0),
        )
        db.grammarDao().insertAll(grammar)

        // Default progress
        db.progressDao().updateProgress(UserProgressEntity())

        // Seed Dialogues
        val dialoguesList = listOf(
            DialogueEntity(1, "التعارف الأول في جاكرتا", "Perkenalan Pertama di Jakarta", "A: Halo, nama saya Ahmad. Saya dari Yaman.\nB: Halo, saya Siti. Senang bertemu denganmu.\nA: Saya senang juga. Kamu tinggal di mana?\nB: Saya tinggal di Jakarta.", 0),
            DialogueEntity(2, "المساومة في السوق التقليدي", "Tawar-menawar di Pasar Tradisional", "Penjual: Ke sini dong! Lihat-lihat baju bagus dan murah.\nPembeli: Terima kasih. Baju merah ini berapa harganya?\nPenjual: Itu murah banget, cuma seratus ribu.\nPembeli: Bisa kurang tidak? Delapan puluh ribu saja ya?\nPenjual: Boleh deh, ambil saja!", 0)
        )
        db.dialogueDao().insertAll(dialoguesList)

        // === Bahasa Sehari-hari (Casual Indonesian) - Expanded ===
        val casual = listOf(
            // تعبيرات ودية
            CasualExpressionEntity(1, "Makasih ya!", "ماكاسي يا", "شكرًا!", "🔵 يومي", "مع الأصدقاء والمعارف", "Terima kasih", "تعبيرات ودية", 0),
            CasualExpressionEntity(2, "Nggak apa-apa", "نجاك أبا-أبا", "لا بأس", "🔵 يومي", "رد على الاعتذار", "Tidak apa-apa", "تعبيرات ودية", 0),
            CasualExpressionEntity(3, "Santai aja", "سانتاي أجا", "خذها ببساطة", "🔵 يومي", "تهدئة شخص", null, "تعبيرات ودية", 0),
            CasualExpressionEntity(4, "Tenang aja", "تينانج أجا", "لا تقلق", "🔵 يومي", "تهدئة", null, "تعبيرات ودية", 0),
            CasualExpressionEntity(5, "Gampang kok", "جامبانج كوك", "الأمر سهل", "🔵 يومي", "طمأنة", null, "تعبيرات ودية", 0),
            
            // السوق
            CasualExpressionEntity(6, "Ke sini dong!", "كي سيني دونج", "تعال هنا!", "🟡 غير رسمي", "جذب الزبون", null, "سوق", 0),
            CasualExpressionEntity(7, "Murah banget!", "موراه بانجيت", "رخيص جدًا!", "🟡 غير رسمي", "التفاوض", null, "سوق", 0),
            CasualExpressionEntity(8, "Bisa kurang?", "بيسا كورانج", "هل يمكن تخفيض السعر؟", "🔵 يومي", "التفاوض", null, "سوق", 0),
            CasualExpressionEntity(9, "Berapa harganya?", "بيرابا هارجانيا", "كم سعره؟", "🔵 يومي", "سؤال عن السعر", null, "سوق", 0),
            CasualExpressionEntity(10, "Ambil dua aja", "أمبيل دوا أجا", "خذ اثنين فقط", "🔵 يومي", "شراء كمية", null, "سوق", 0),
            CasualExpressionEntity(11, "Mau yang mana?", "ماو يانج مانا", "أي واحد تريد؟", "🔵 يومي", "عرض المنتج", null, "سوق", 0),
            CasualExpressionEntity(12, "Yang ini bagus", "يانج إيني باغوس", "هذا جيد", "🔵 يومي", "تقييم المنتج", null, "سوق", 0),
            CasualExpressionEntity(13, "Ada yang lebih murah?", "آدا يانج لبيه موراه", "هل يوجد أرخص؟", "🔵 يومي", "طلب خيار أرخص", null, "سوق", 0),
            CasualExpressionEntity(14, "Ke sini, aku kasih harga teman!", "كي سيني، أكو كاسيه هارجا تيمان", "تعال، أعطيك سعر الصديق", "🟡 غير رسمي", "عرض خاص", null, "سوق", 0),
            CasualExpressionEntity(15, "Pas banget", "باس بانجيت", "مناسب جدًا", "🔵 يومي", "الموافقة على السعر", null, "سوق", 0),
            
            // الشارع والحياة اليومية
            CasualExpressionEntity(16, "Udah makan?", "أوداه ماكان", "هل أكلت؟", "🔵 يومي", "سؤال يومي", null, "شارع", 0),
            CasualExpressionEntity(17, "Mau ke mana?", "ماو كي مانا", "إلى أين ذاهب؟", "🔵 يومي", "سؤال يومي", null, "شارع", 0),
            CasualExpressionEntity(18, "Lagi apa?", "لاجي أبا", "ماذا تفعل؟", "🔵 يومي", "سؤال يومي", null, "شارع", 0),
            CasualExpressionEntity(19, "Bentar ya", "بينتار يا", "لحظة واحدة", "🔵 يومي", "طلب الانتظار", null, "شارع", 0),
            CasualExpressionEntity(20, "Ayo jalan!", "آيو جالان", "هيا نذهب!", "🔵 يومي", "دعوة", null, "شارع", 0),
            
            // الأصدقاء والمزاح
            CasualExpressionEntity(21, "Kamu pelit banget ya!", "كامو بيليت بانجيت يا", "أنت بخيل جدًا 😄", "🟠 عامي", "مزاح مع الأصدقاء", null, "أصدقاء", 0),
            CasualExpressionEntity(22, "Ah, kamu lebay!", "آه، كامو ليباي", "أنت تبالغ! 😄", "🟠 عامي", "مزاح", null, "أصدقاء", 0),
            CasualExpressionEntity(23, "Masa sih?", "ماسا سيه", "حقًا؟", "🔵 يومي", "تعجب", null, "أصدقاء", 0),
            CasualExpressionEntity(24, "Serius?", "سيريوس", "حقًا؟", "🔵 يومي", "تعجب", null, "أصدقاء", 0),
            CasualExpressionEntity(25, "Iya dong!", "إيا دونج", "طبعًا!", "🔵 يومي", "تأكيد ودي", null, "أصدقاء", 0),
            CasualExpressionEntity(26, "Boleh banget", "بوليه بانجيت", "بالتأكيد", "🔵 يومي", "موافقة", null, "تعبيرات ودية", 0),
            CasualExpressionEntity(27, "Nih", "نيه", "هذا (للإشارة)", "🟡 غير رسمي", "إعطاء شيء", null, "شارع", 0),
            CasualExpressionEntity(28, "Tuh", "توه", "ذاك (للإشارة)", "🟡 غير رسمي", "الإشارة إلى شيء", null, "شارع", 0),
            CasualExpressionEntity(29, "Kok", "كوك", "لماذا / كيف", "🟡 غير رسمي", "تعجب", null, "أصدقاء", 0),
            CasualExpressionEntity(30, "Sih", "سيه", "أداة تأكيد", "🟡 غير رسمي", "في نهاية الجملة", null, "أصدقاء", 0),
            CasualExpressionEntity(31, "Deh", "ديه", "أداة تأكيد", "🟡 غير رسمي", "إعطاء نصيحة", null, "أصدقاء", 0),
            CasualExpressionEntity(32, "Banget", "بانجيت", "جداً", "🔵 يومي", "تكبير المعنى", null, "عام", 0),
            CasualExpressionEntity(33, "Kayak", "كاياك", "مثل", "🔵 يومي", "مقارنة", null, "عام", 0),
            CasualExpressionEntity(34, "Biar", "بيار", "حتى / دع", "🔵 يومي", "السماح", null, "عام", 0),
            CasualExpressionEntity(35, "Emang", "إيمانج", "فعلاً", "🔵 يومي", "تأكيد", null, "عام", 0),
            CasualExpressionEntity(36, "Cuma", "تشوما", "فقط", "🔵 يومي", "تقييد", null, "عام", 0),
            CasualExpressionEntity(37, "Gitu", "جيتو", "هكذا", "🟡 غير رسمي", "الإشارة إلى طريقة", null, "أصدقاء", 0),
            CasualExpressionEntity(38, "Gimana", "جيمانا", "كيف", "🔵 يومي", "سؤال", null, "عام", 0),
            CasualExpressionEntity(39, "Kenapa", "كينابا", "لماذا", "🔵 يومي", "سؤال", null, "عام", 0),
            CasualExpressionEntity(40, "Bener", "بينير", "صحيح", "🔵 يومي", "تأكيد", null, "عام", 0),
        )
        db.casualDao().insertAll(casual)

        val scenarios = listOf(
            DailyScenarioEntity(1, "Market Negotiation", "تفاوض في السوق", 
                "Penjual: Ke sini dong! Lihat-lihat dulu.\nPembeli: Iya, saya lihat-lihat dulu.\nPenjual: Mau yang mana?\nPembeli: Yang ini berapa?\nPenjual: Murah banget ini, cuma 50 ribu!",
                "البائع: تعال هنا! تفرّج أولًا.\nالمشتري: نعم، سأتفرج أولًا.\nالبائع: أي واحد تريد؟\nالمشتري: هذا كم سعره؟\nالبائع: رخيص جدًا، فقط 50 ألف!", 0, "سوق"),
            DailyScenarioEntity(2, "Restaurant", "المطعم",
                "Pelayan: Mau pesan apa?\nPelanggan: Nasi goreng satu.\nPelayan: Minum apa?\nPelanggan: Es teh.",
                "النادل: ماذا تريد أن تطلب؟\nالزبون: ناسي غورينغ واحد.\nالنادل: ماذا تشرب؟\nالزبون: شاي مثلج.", 0, "مطعم"),
            DailyScenarioEntity(3, "Street Meeting", "لقاء في الشارع",
                "A: Lagi apa?\nB: Nggak apa-apa. Mau ke mana?\nA: Ke pasar. Ikut?",
                "أ: ماذا تفعل؟\nب: لا شيء. إلى أين ذاهب؟\nأ: إلى السوق. ترافقني؟", 0, "شارع"),
            DailyScenarioEntity(4, "Buying Clothes", "شراء ملابس",
                "Penjual: Mau yang warna apa?\nPembeli: Yang hitam.\nPenjual: Ini ada yang lebih murah.",
                "البائع: أي لون تريد؟\nالمشتري: الأسود.\nالبائع: هذا يوجد أرخص.", 0, "محل"),
        )
        db.casualDao().insertScenarios(scenarios)

        // Training Items - Expanded
        val training = listOf(
            TrainingItemEntity(1, "LISTEN_CHOOSE", "Mau ke mana?", "إلى أين ذاهب؟", "ماذا تريد؟,إلى أين ذاهب؟,كم السعر؟,هل أكلت؟", "الإجابة الصحيحة: B", "شارع"),
            TrainingItemEntity(2, "TRANSLATE", "تعال هنا!", "Ke sini dong!", "Ke sini dong!,Ke sini aja,Sini dong", "Ke sini dong! = تعال هنا (ودي)", "سوق"),
            TrainingItemEntity(3, "ORDER_WORDS", "dong / Ke / sini", "Ke sini dong!", "Ke sini dong!,Sini ke dong", "Ke + sini + dong", "سوق"),
            TrainingItemEntity(4, "SITUATION", "أنت في السوق وتريد معرفة السعر", "Berapa harganya?", "Berapa harganya?,Mau apa?,Santai aja", "السؤال عن السعر", "سوق"),
            TrainingItemEntity(5, "FORMALITY", "أي تعبير أكثر يومية؟", "Makasih ya!", "Terima kasih.,Makasih ya!,Silakan", "Makasih ya! = يومي وودي", "عام"),
            TrainingItemEntity(6, "LISTEN_CHOOSE", "Udah makan?", "هل أكلت؟", "هل أكلت؟,هل شربت؟,هل انتهيت؟", "الإجابة الصحيحة: A", "شارع"),
            TrainingItemEntity(7, "TRANSLATE", "رخيص جدًا!", "Murah banget!", "Murah banget!,Mahal banget!,Murah sekali", "Murah banget! = رخيص جداً (يومي)", "سوق"),
        )
        db.trainingDao().insertAll(training)

        // === Full Curriculum Stages ===
        val stages = listOf(
            StageEntity(1, "المرحلة 1 — الصفر", "Tahap 1 - Nol", "الحروف، النطق، التحيات، التعارف، الأرقام", 0, true),
            StageEntity(2, "المرحلة 2 — المبتدئ", "Tahap 2 - Pemula", "ترتيب الجملة، النفي، السؤال، الصفات", 1, false),
            StageEntity(3, "المرحلة 3 — المبتدئ المتقدم", "Tahap 3 - Pemula Lanjut", "الأزمنة، القدرة، المقارنة", 2, false),
            StageEntity(4, "المرحلة 4 — البادئات واللواحق", "Tahap 4 - Awalan & Akhiran", "me-, ber-, di-, ter-, -kan, -i", 3, false),
            StageEntity(5, "المرحلة 5 — المتوسط العملي", "Tahap 5 - Menengah Praktis", "محادثات، قراءة، كتابة، مواقف حقيقية", 4, false),
        )
        db.stageDao().insertAll(stages)

        // === STAGE 2: THE BEGINNER TO UPPER-BEGINNER CURRICULUM (FINAL) ===

        val stage2Units = listOf(
            UnitEntity(15, 2, "الوحدة 1: توسيع تكوين الجملة", "Unit 1: Kalimat Panjang", "جمل أطول + عناصر متعددة", false),
            UnitEntity(16, 2, "الوحدة 2: التعبير عن الزمن", "Unit 2: Waktu", "sudah, sedang, akan, belum, pernah", false),
            UnitEntity(17, 2, "الوحدة 3: الأفعال اليومية المتقدمة", "Unit 3: Kata Kerja Lanjut", "bekerja, mencari, membawa...", false),
            UnitEntity(18, 2, "الوحدة 4: القدرة والرغبة والوجوب", "Unit 4: Bisa, Mau, Harus", "bisa, mau, harus, boleh", false),
            UnitEntity(19, 2, "الوحدة 5: مقدمة البادئات", "Unit 5: Awalan Dasar", "me-, ber-, di-", false),
            UnitEntity(20, 2, "الوحدة 6: meN- بالتفصيل", "Unit 6: meN-", "membeli, menulis, memakai, menyapu", false),
            UnitEntity(21, 2, "الوحدة 7: di- و me-", "Unit 7: Aktif & Pasif", "Saya membeli vs Buku dibeli", false),
            UnitEntity(22, 2, "الوحدة 8: المقارنة والتفضيل", "Unit 8: Perbandingan", "lebih, paling, sangat, terlalu", false),
            UnitEntity(23, 2, "الوحدة 9: ربط الجمل", "Unit 9: Penghubung", "dan, tetapi, karena, kalau", false),
            UnitEntity(24, 2, "الوحدة 10: اللغة اليومية", "Unit 10: Bahasa Sehari-hari", "nggak, udah, dong, banget", false),
            UnitEntity(25, 2, "الوحدة 11: السوق والبيع", "Unit 11: Belanja", "Berapa, bisa kurang, harga teman", false),
            UnitEntity(26, 2, "الوحدة 12: العمل والحياة", "Unit 12: Kerja", "bekerja, kantor, sibuk, terlambat", false),
            UnitEntity(27, 2, "الوحدة 13: المحادثات", "Unit 13: Percakapan", "حوارات واقعية", false),
            UnitEntity(28, 2, "الوحدة 14: التفكير بالإندونيسية", "Unit 14: Berpikir", "إنتاج جمل ومواقف", false),
        )
        db.unitDao().insertAll(stage2Units)

        // === STAGE 2 LESSON DETAILS (Real Content) ===
        val stage2Lessons = listOf(
            LessonDetailEntity(24, 15, "ستتعلم بناء جمل أطول بإضافة المكان والزمان.",
                "Saya makan nasi di rumah setiap malam.",
                "Saya + makan + nasi + di rumah + setiap malam",
                "ابدأ بجملة بسيطة ثم أضف عناصر.", "كل عنصر جديد يأتي في نهاية الجملة عادة.", "Formal & Casual: sama"),
            
            LessonDetailEntity(25, 16, "ستتعلم التعبير عن الزمن باستخدام sudah, sedang, akan, belum.",
                "Saya sudah makan.\nSaya sedang makan.\nSaya akan makan.\nSaya belum makan.",
                "sudah = انتهى\nsedang = الآن\nakan = المستقبل\nbelum = لم يحدث بعد",
                "هذه الكلمات تغير الزمن دون تغيير الفعل.", "sudah vs belum مهم جداً.", "Casual: udah"),
            
            LessonDetailEntity(26, 17, "ستتعلم أفعالاً يومية أكثر تقدماً.",
                "Saya bekerja di kantor.\nSaya sedang mencari pekerjaan.",
                "bekerja = يعمل\nmencari = يبحث",
                "me- غالباً ما يحول الاسم إلى فعل.", "استخدمها في جمل يومية.", "Formal & Casual: sama"),
            
            LessonDetailEntity(27, 18, "ستتعلم التعبير عن القدرة والرغبة والوجوب.",
                "Saya bisa datang.\nSaya mau datang.\nSaya harus datang.\nSaya boleh datang.",
                "bisa = يستطيع\nmau = يريد\nharus = يجب\nboleh = يسمح",
                "bisa = قدرة\nmau = رغبة\nharus = إلزام", "harus أقوى من perlu.", "Casual: mau, harus"),
            
            LessonDetailEntity(28, 19, "ستتعلم كيف تتكون الكلمات من جذر + بادئة.",
                "beli → membeli\njual → menjual\najar → belajar",
                "me- + beli = membeli\nber- + kerja = bekerja",
                "البادئة تغير المعنى والنوع.", "ابدأ بفهم الجذر أولاً.", "Formal & Casual: sama"),
            
            LessonDetailEntity(29, 20, "ستتعلم قواعد meN- وتغير الحرف الأول.",
                "beli → membeli\ntulis → menulis\npakai → memakai\nsapu → menyapu",
                "meN- يتغير حسب الحرف الأول من الجذر.", "m + b = mb\nn + t = nt\nny + s = ny", "هذه القاعدة مهمة جداً.", "Formal & Casual: sama"),
            
            LessonDetailEntity(30, 21, "ستتعلم الفرق بين الجملة النشطة والسلبية.",
                "Saya membeli buku.\nBuku dibeli oleh saya.",
                "me- = نشط\ndi- = سلبي", "di- يستخدم عندما نركز على المفعول.", "استخدم di- عندما لا تعرف الفاعل.", "Formal & Casual: sama"),
            
            LessonDetailEntity(31, 22, "ستتعلم المقارنة والتفضيل.",
                "Ini lebih murah.\nIni paling murah.\nIni sangat murah.\nIni terlalu mahal.",
                "lebih = أكثر\npaling = الأكثر\nsangat = جداً\nterlalu = أكثر من اللازم", "terlalu غالباً ما يكون سلبياً.", "", "Casual: banget"),
            
            LessonDetailEntity(32, 23, "ستتعلم ربط الجمل بأدوات الربط.",
                "Saya lapar, jadi saya makan.\nSaya tidak lapar, tetapi saya makan.",
                "dan = و\ntetapi = لكن\nkarena = لأن\njadi = لذلك\nkalau = إذا", "ابدأ بـ dan ثم tetapi ثم karena.", "", "Casual: tapi"),
            
            LessonDetailEntity(33, 24, "ستتعلم اللغة اليومية الواقعية المستخدمة في الشارع.",
                "Nggak apa-apa.\nSantai aja.\nMahal banget!\nBisa kurang?\nJangan gitu dong.",
                "nggak = tidak\nudah = sudah\naja = saja\ndong = أداة تأكيد\nbanget = sangat", "هذه الكلمات شائعة جداً في الحياة اليومية.", "", "عامي جداً"),
            
            LessonDetailEntity(34, 25, "ستتعلم كيف تتفاوض في السوق بلغة طبيعية.",
                "Berapa harganya?\nBisa kurang?\nMahal banget.\nHarga teman dong.\nSaya ambil dua.",
                "Berapa = كم\nBisa kurang = هل يمكن تخفيض\nHarga teman = سعر الصديق", "استخدم هذه الجمل في السوق.", "", "عامي"),
            
            LessonDetailEntity(35, 26, "ستتعلم الحديث عن العمل والحياة اليومية.",
                "Saya bekerja di kantor.\nSaya sibuk hari ini.\nSaya terlambat.",
                "bekerja = يعمل\nsibuk = مشغول\nterlambat = متأخر", "استخدمها مع الزملاء.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(36, 27, "ستتعلم محادثات واقعية في مواقف يومية.",
                "A: Mau ke mana?\nB: Ke pasar. Ikut?\nA: Nggak, nanti saja.",
                "Mau ke mana? = إلى أين ذاهب؟\nIkut? = ترافقني؟\nNanti saja = لاحقاً", "هذه المحادثات شائعة جداً.", "", "عامي"),
            
            LessonDetailEntity(37, 28, "ستبدأ في التفكير وإنتاج الجمل بالإندونيسية.",
                "Saya mau pergi ke pasar besok.\nKamu mau ikut?",
                "حاول تكوين جمل باستخدام ما تعلمته.", "لا تترجم حرفياً من العربية.", "", "Formal & Casual mixed"),
        )
        db.lessonDetailDao().insertAll(stage2Lessons)

        // === STAGE 2 REAL QUIZZES (Unit Quizzes) ===
        val stage2Quizzes = listOf(
            // Unit 15 - Expanding Sentences
            TrainingItemEntity(100, "TRANSLATE", "أنا آكل الأرز في البيت كل ليلة", "Saya makan nasi di rumah setiap malam.", "", "ترتيب: فاعل + فعل + مفعول + مكان + زمان", "جمل"),
            TrainingItemEntity(101, "ORDER_WORDS", "Saya / makan / nasi / di rumah", "Saya makan nasi di rumah.", "", "أضف المكان بعد المفعول", "جمل"),

            // Unit 16 - Time Expressions
            TrainingItemEntity(102, "MULTIPLE_CHOICE", "ما معنى 'sudah'؟", "انتهى", "الآن,المستقبل,انتهى,لم يحدث", "sudah = already (انتهى)", "زمن"),
            TrainingItemEntity(103, "MULTIPLE_CHOICE", "Saya ___ makan. (أنا لم آكل بعد)", "belum", "sudah,sedang,akan,belum", "belum = not yet", "زمن"),

            // Unit 17 - Advanced Verbs
            TrainingItemEntity(104, "TRANSLATE", "أنا أعمل في المكتب", "Saya bekerja di kantor.", "", "bekerja = يعمل", "أفعال"),
            TrainingItemEntity(105, "MULTIPLE_CHOICE", "mencari = ?", "يبحث", "يعمل,يبحث,يأخذ,يعطي", "mencari = to search/look for", "أفعال"),

            // Unit 18 - Ability & Obligation
            TrainingItemEntity(106, "MULTIPLE_CHOICE", "Saya ___ datang. (أنا يجب أن أأتي)", "harus", "bisa,mau,harus,boleh", "harus = must", "قدرة"),
            TrainingItemEntity(107, "TRANSLATE", "أنا أستطيع أن أأتي", "Saya bisa datang.", "", "bisa = can/able to", "قدرة"),

            // Unit 20 - meN- Prefix
            TrainingItemEntity(108, "MULTIPLE_CHOICE", "beli → ?", "membeli", "menbeli,memeli,membeli,mebeli", "me- + beli = membeli", "بادئات"),
            TrainingItemEntity(109, "ORDER_WORDS", "membeli / buku / Saya", "Saya membeli buku.", "", "meN- يحول الفعل إلى نشط", "بادئات"),

            // Unit 22 - Comparison
            TrainingItemEntity(110, "MULTIPLE_CHOICE", "Ini ___ murah. (هذا أرخص)", "lebih", "paling,sangat,lebih,terlalu", "lebih = more", "مقارنة"),
            TrainingItemEntity(111, "MULTIPLE_CHOICE", "Ini ___ murah. (هذا الأرخص)", "paling", "lebih,paling,sangat,terlalu", "paling = the most", "مقارنة"),

            // Unit 24 - Daily Language
            TrainingItemEntity(112, "MULTIPLE_CHOICE", "nggak = ?", "tidak", "sudah,mau,tidak,pergi", "nggak = tidak (يومي)", "يومي"),
            TrainingItemEntity(113, "TRANSLATE", "رخيص جداً!", "Murah banget!", "", "banget = sangat (يومي)", "يومي"),

            // Unit 25 - Market
            TrainingItemEntity(114, "SITUATION", "أنت في السوق وتريد معرفة السعر", "Berapa harganya?", "Mau apa?,Berapa harganya?,Bisa kurang?,Santai aja", "Berapa harganya? = كم سعره؟", "سوق"),
            // كانت الإجابة الصحيحة "هل يمكن تخفيض السعر؟" غير موجودة ضمن الخيارات
            // (الخيار كان "هل يمكن تخفيض؟") فيستحيل على المستخدم الإجابة صحيحاً.
            TrainingItemEntity(115, "MULTIPLE_CHOICE", "Bisa kurang? = ?", "هل يمكن تخفيض السعر؟", "كم السعر؟,هل يمكن تخفيض السعر؟,هل تريد؟,رخيص", "Bisa kurang? = Can it be cheaper?", "سوق"),
        )
        db.trainingDao().insertAll(stage2Quizzes)

        // === STAGE 2 FINAL EXAM (Real Questions) ===
        val stage2FinalExam = listOf(
            TrainingItemEntity(200, "MULTIPLE_CHOICE", "Saya ___ makan. (أنا لم آكل بعد)", "belum", "sudah,sedang,akan,belum", "belum = not yet", "امتحان"),
            TrainingItemEntity(201, "TRANSLATE", "أنا أعمل في المكتب", "Saya bekerja di kantor.", "", "bekerja = يعمل", "امتحان"),
            TrainingItemEntity(202, "MULTIPLE_CHOICE", "beli → ?", "membeli", "menbeli,membeli,memeli,mebeli", "me- + beli = membeli", "امتحان"),
            TrainingItemEntity(203, "MULTIPLE_CHOICE", "Ini ___ murah. (هذا أرخص)", "lebih", "paling,lebih,sangat,terlalu", "lebih = more", "امتحان"),
            TrainingItemEntity(204, "SITUATION", "أنت في السوق وتريد معرفة السعر", "Berapa harganya?", "Mau apa?,Berapa harganya?,Santai aja,Bisa kurang?", "Berapa harganya?", "امتحان"),
            TrainingItemEntity(205, "MULTIPLE_CHOICE", "nggak = ?", "tidak", "mau,tidak,sudah,pergi", "nggak = tidak (يومي)", "امتحان"),
            TrainingItemEntity(206, "ORDER_WORDS", "Saya / membeli / buku", "Saya membeli buku.", "", "meN- يحول الفعل إلى نشط", "امتحان"),
            TrainingItemEntity(207, "MULTIPLE_CHOICE", "Saya ___ datang. (أنا يجب أن أأتي)", "harus", "bisa,mau,harus,boleh", "harus = must", "امتحان"),
            TrainingItemEntity(208, "TRANSLATE", "رخيص جداً!", "Murah banget!", "", "banget = sangat", "امتحان"),
            TrainingItemEntity(209, "MULTIPLE_CHOICE", "Saya ___ makan. (أنا أكلت بالفعل)", "sudah", "belum,sedang,sudah,akan", "sudah = already", "امتحان"),
        )
        db.trainingDao().insertAll(stage2FinalExam)
            
        // === STAGE 1 UNITS ===
        val units = listOf(
            UnitEntity(1, 1, "الوحدة 1: التحيات والتعارف", "Unit 1: Salam & Perkenalan", "تحيات + تقديم النفس", false),
            UnitEntity(2, 1, "الوحدة 2: الضمائر", "Unit 2: Kata Ganti", "saya, aku, kamu, dia, kami, kita", false),
            UnitEntity(3, 1, "الوحدة 3: تكوين الجملة", "Unit 3: Kalimat Dasar", "فاعل + فعل + مفعول", false),
            UnitEntity(4, 1, "الوحدة 4: الأفعال الأساسية", "Unit 4: Kata Kerja Dasar", "makan, minum, pergi, mau, suka", false),
            UnitEntity(5, 1, "الوحدة 5: النفي", "Unit 5: Negasi", "tidak, bukan, belum, jangan", false),
            UnitEntity(6, 1, "الوحدة 6: السؤال", "Unit 6: Pertanyaan", "apa, siapa, di mana, berapa", false),
            UnitEntity(7, 1, "الوحدة 7: الأرقام", "Unit 7: Angka", "0–100 + الأسعار", false),
            UnitEntity(8, 1, "الوحدة 8: الوقت والتاريخ", "Unit 8: Waktu & Tanggal", "الساعة، الأيام، الشهور", false),
            UnitEntity(9, 1, "الوحدة 9: الملكية", "Unit 9: Kepemilikan", "rumah saya, buku kamu", false),
            UnitEntity(10, 1, "الوحدة 10: الصفات", "Unit 10: Kata Sifat", "besar, kecil, bagus, murah", false),
            UnitEntity(11, 1, "الوحدة 11: حروف الجر", "Unit 11: Preposisi", "di, ke, dari", false),
            UnitEntity(12, 1, "الوحدة 12: الأسرة", "Unit 12: Keluarga", "ayah, ibu, kakak, adik", false),
            UnitEntity(13, 1, "الوحدة 13: الأشياء اليومية", "Unit 13: Benda", "meja, kursi, buku, telepon", false),
            UnitEntity(14, 1, "الوحدة 14: مراجعة المرحلة الأولى", "Unit 14: Review", "مراجعة + اختبار شامل", false),
        )
        db.unitDao().insertAll(units)

        // === REAL LESSON DETAILS FOR STAGE 1 (Expanded) ===
        val lessonDetails = listOf(
            LessonDetailEntity(1, 1, "بعد هذا الدرس ستستطيع تحية الآخرين وتقديم نفسك.", 
                "Halo = مرحبا\nSelamat pagi = صباح الخير\nSelamat siang = مساء الخير\nSelamat sore = مساء الخير\nSelamat malam = تصبح على خير", 
                "Halo + Apa kabar?", "تستخدم يومياً مع الجميع", "لا تستخدم Selamat pagi بعد الظهر", "Formal: Selamat pagi • Casual: Halo"),
            
            LessonDetailEntity(2, 2, "ستتعلم الضمائر الأساسية ومتى تستخدم كل واحدة.", 
                "saya = أنا (رسمي)\naku = أنا (يومي)\nkamu = أنت (يومي)\nAnda = أنت (رسمي)\ndia = هو/هي\nkami = نحن (بدونك)\nkita = نحن (معك)", 
                "Saya + makan + nasi", "saya أكثر أماناً في البداية", "لا تستخدم Anda مع الأصدقاء", "Formal: saya • Casual: aku"),
            
            LessonDetailEntity(3, 3, "ستتعلم بناء أول جملة صحيحة: فاعل + فعل + مفعول.", 
                "Saya makan nasi.\nSaya minum air.\nKamu pergi ke pasar.", 
                "Saya + makan + nasi\nفاعل + فعل + مفعول", "هذا الترتيب الأساسي في الإندونيسية", "لا توجد 'to be' مثل الإنجليزية", "Formal: Saya makan nasi • Casual: Aku makan nasi"),
            
            LessonDetailEntity(4, 4, "ستتعلم أهم الأفعال التي تستخدمها يومياً.", 
                "makan = يأكل\nminum = يشرب\npergi = يذهب\nmau = يريد\nsuka = يحب", 
                "Saya mau makan.", "mau هو أحد أهم الأفعال", "mau + verb = أريد أن...", "Formal & Casual: mau"),
            
            LessonDetailEntity(5, 5, "ستتعلم كيف تنفي الجمل بشكل صحيح.", 
                "tidak = لا (للفعل)\nbukan = ليس (للاسم)\nbelum = لم بعد\njangan = لا تفعل", 
                "Saya tidak tahu.\nIni bukan buku saya.", "tidak vs bukan هو أحد أكثر الأخطاء شيوعاً", "tidak + verb • bukan + noun", "Formal: tidak • Casual: nggak"),
            
            LessonDetailEntity(6, 6, "ستتعلم كيف تسأل الأسئلة الأساسية.", 
                "apa = ماذا\nsiapa = من\ndi mana = أين\nke mana = إلى أين\nberapa = كم", 
                "Apa ini?\nSiapa nama kamu?\nBerapa harganya?", "berapa مهم جداً في السوق", "استخدم 'di mana' للمكان و'ke mana' للاتجاه", "Formal & Casual: sama"),
            
            LessonDetailEntity(7, 7, "ستتعلم الأرقام من 0 إلى 100 وكيفية استخدامها.", 
                "satu = 1\ndua = 2\ntiga = 3\nempat = 4\nlima = 5\nsepuluh = 10\ndua puluh = 20\nseratus = 100", 
                "Berapa? Dua puluh ribu.", "الأرقام تستخدم كثيراً في التسوق", "puluh = عشرة • ratus = مائة", "Formal & Casual: sama"),
            
            LessonDetailEntity(8, 8, "ستتعلم كيف تسأل عن الوقت والتاريخ.", 
                "jam berapa? = الساعة كم؟\nhari ini = اليوم\nbesok = غداً\nkemarin = أمس", 
                "Jam berapa sekarang?", "الإندونيسيون يستخدمون 24 ساعة أحياناً", "gunakan 'pagi', 'siang', 'sore', 'malam'", "Formal & Casual: sama"),
            
            LessonDetailEntity(9, 9, "ستتعلم كيف تعبر عن الملكية.", 
                "rumah saya = بيتي\nbuku kamu = كتابك\nmobil dia = سيارته", 
                "Ini rumah saya.", "الملكية تأتي بعد الاسم", "لا توجد 'of' أو 's", "Formal: saya • Casual: aku"),
            
            LessonDetailEntity(10, 10, "ستتعلم الصفات الأساسية وكيفية استخدامها.", 
                "besar = كبير\nkecil = صغير\nbagus = جيد\nmurah = رخيص\nmahal = غالي", 
                "Rumah ini besar dan bagus.", "الصفة تأتي بعد الاسم", "murah vs mahal مهم في السوق", "Formal & Casual: sama"),

            // Unit 4 - Verbs (multiple lessons)
            LessonDetailEntity(11, 4, "ستتعلم مفهوم الفعل في الإندونيسية وأهم الأفعال اليومية.", 
                "الفعل في الإندونيسية لا يتغير حسب الزمن أو الفاعل.\nmakan = يأكل\nminum = يشرب\npergi = يذهب\npulang = يعود", 
                "Saya makan nasi.\nKamu minum air.", "الفعل يبقى ثابتاً", "mau + verb = أريد أن...", "Formal & Casual: sama"),
            
            LessonDetailEntity(12, 4, "ستتعلم استخدام mau وsuka.", 
                "mau = يريد / أريد\nsuka = يحب\nSaya mau makan.\nSaya suka kopi.", 
                "Saya mau pergi.\nDia suka membaca.", "mau = رغبة\nsuka = إعجاب", "mau أكثر استخداماً من ingin في الحياة اليومية", "Casual: mau"),

            // Unit 5 - Negation
            LessonDetailEntity(13, 5, "ستتعلم الفرق بين tidak وbukan.", 
                "tidak = لا (مع الفعل أو الصفة)\nbukan = ليس (مع الاسم)\nSaya tidak lapar.\nIni bukan buku saya.", 
                "Saya tidak tahu.\nDia bukan guru.", "tidak + verb/adj\nbukan + noun", "هذا أحد أكثر الأخطاء شيوعاً للمبتدئين", "Casual: nggak"),

            LessonDetailEntity(14, 5, "ستتعلم استخدام belum وjangan.", 
                "belum = لم بعد\njangan = لا تفعل\nSaya belum makan.\nJangan lari!", 
                "Belum = لم يحدث بعد\nJangan = أمر سلبي", "jangan مهم جداً للأوامر", "", "Formal & Casual: sama"),

            // Unit 6 - Questions
            LessonDetailEntity(15, 6, "ستتعلم جميع أدوات السؤال الأساسية.", 
                "apa = ماذا\nsiapa = من\ndi mana = أين\nke mana = إلى أين\nberapa = كم\nkenapa = لماذا", 
                "Apa ini?\nSiapa nama kamu?\nBerapa harganya?", "berapa هو الأكثر استخداماً في السوق", "di mana = مكان ثابت\nke mana = حركة", "Formal & Casual: sama"),

            // Unit 7 - Numbers (Expanded)
            LessonDetailEntity(16, 7, "ستتعلم الأرقام من 1 إلى 100 وكيفية نطقها.", 
                "satu, dua, tiga, empat, lima, enam, tujuh, delapan, sembilan, sepuluh\nsebelas, dua belas...\ndua puluh, tiga puluh...\nseratus", 
                "Berapa? Lima puluh ribu.", "puluh = عشرة\nratus = مائة", "الأرقام مهمة جداً في التسوق", "Formal & Casual: sama"),

            // Unit 8 - Time
            LessonDetailEntity(17, 8, "ستتعلم كيف تسأل عن الوقت والتاريخ.", 
                "Jam berapa sekarang?\nPagi = صباح\nSiang = ظهر\nSore = مساء\nMalam = ليل\nHari ini = اليوم\nBesok = غداً\nKemarin = أمس", 
                "Jam berapa? Jam tujuh.", "الإندونيسيون يستخدمون 24 ساعة أحياناً", "pagi, siang, sore, malam مهمة جداً", "Formal & Casual: sama"),

            // Unit 9 - Possession
            LessonDetailEntity(18, 9, "ستتعلم طرق التعبير عن الملكية.", 
                "rumah saya = بيتي\nbuku kamu = كتابك\nmobil dia = سيارته\nbukuku = كتابي (يومي)", 
                "Ini rumah saya.\nBukumu di mana?", "الملكية تأتي بعد الاسم", "bukuku أكثر يومية من buku saya", "Casual: -ku / -mu"),

            // Unit 10 - Adjectives (Expanded)
            LessonDetailEntity(19, 10, "ستتعلم الصفات الأساسية مع أمثلة كثيرة.", 
                "besar, kecil, tinggi, rendah, panjang, pendek, bagus, jelek, mahal, murah, cepat, lambat, panas, dingin, baru, lama, bersih, kotor", 
                "Rumah ini besar dan bagus.\nHarganya mahal sekali.", "الصفة تأتي بعد الاسم", "sangat + adjective = جداً", "Formal & Casual: sama"),

            // Unit 11 - Prepositions
            LessonDetailEntity(20, 11, "ستتعلم الفرق بين di, ke, dari.", 
                "di = في (مكان ثابت)\nke = إلى (حركة)\ndari = من\nSaya di rumah.\nSaya pergi ke pasar.\nSaya dari Indonesia.", 
                "di rumah\nke pasar\ndari rumah", "di = location\nke = direction", "هذا الفرق مهم جداً", "Formal & Casual: sama"),

            // Unit 12 - Family
            LessonDetailEntity(21, 12, "ستتعلم مفردات الأسرة والأشخاص.", 
                "ayah / bapak = الأب\nibu / mama = الأم\nkakak = الأخ/الأخت الأكبر\nadik = الأخ/الأخت الأصغر\nanak = الابن/الابنة\nsuami = الزوج\nistri = الزوجة\nteman = الصديق", 
                "Ini ayah saya.\nKakak saya bekerja di bank.", "kakak vs adik يعتمد على العمر", "", "Formal & Casual: sama"),

            // Unit 13 - Daily Objects
            LessonDetailEntity(22, 13, "ستتعلم أسماء الأشياء اليومية.", 
                "meja = طاولة\nkursi = كرسي\nbuku = كتاب\ntelepon = هاتف\npintu = باب\njendela = نافذة\nkamar = غرفة\nkamar mandi = حمام", 
                "Buku saya di meja.", "اربط الكلمات بجمل بسيطة", "استخدمها في جمل يومية", "Formal & Casual: sama"),

            // Unit 14 - Final Review
            LessonDetailEntity(23, 14, "مراجعة شاملة للمرحلة الأولى + اختبار.", 
                "مراجعة كل المفردات والقواعد من الوحدات 1-13.\nاختبار شامل يغطي:\n- التحيات\n- الضمائر\n- الجمل\n- النفي\n- السؤال\n- الأرقام\n- الوقت\n- الصفات", 
                "Saya makan nasi.\nApa kabar?\nBerapa harganya?", "هذا الاختبار يحدد إتقانك للمرحلة الأولى", "يجب اجتياز 70% للانتقال", "Formal & Casual mixed"),
        )
        db.lessonDetailDao().insertAll(lessonDetails)

        // === REAL VOCABULARY FOR STAGE 1 ===
        val vocabStage1 = listOf(
            VocabularyEntity(101, "selamat pagi", "selamat pagi", "سلا مات با جي", "صباح الخير", "Selamat pagi, Pak.", "صباح الخير يا سيدي.", "تحيات", 0, true),
            VocabularyEntity(103, "aku", "aku", "أكو", "أنا (يومي)", "Aku lapar.", "أنا جوعان.", "ضمائر", 0, false),
            VocabularyEntity(104, "kamu", "kamu", "كامو", "أنت", "Kamu dari mana?", "من أين أنت؟", "ضمائر", 0, true),
            VocabularyEntity(107, "tidak", "tidak", "تيداك", "لا", "Saya tidak tahu.", "أنا لا أعرف.", "نفي", 0, true),
            VocabularyEntity(108, "bukan", "bukan", "بوكان", "ليس", "Ini bukan buku saya.", "هذا ليس كتابي.", "نفي", 0, true),
            VocabularyEntity(109, "apa", "apa", "أبا", "ماذا", "Apa ini?", "ما هذا؟", "سؤال", 0, true),
            VocabularyEntity(110, "siapa", "siapa", "سيابا", "من", "Siapa nama kamu?", "ما اسمك؟", "سؤال", 0, true),
            VocabularyEntity(111, "berapa", "berapa", "بيرابا", "كم", "Berapa harganya?", "كم سعره؟", "سؤال", 0, true),
            VocabularyEntity(112, "satu", "satu", "ساتو", "واحد", "Satu, dua, tiga.", "واحد، اثنان، ثلاثة.", "أرقام", 0, true),
            VocabularyEntity(113, "dua", "dua", "دوا", "اثنان", "Dua orang.", "شخصان.", "أرقام", 0, true),
            VocabularyEntity(114, "rumah", "rumah", "روماه", "بيت", "Rumah saya besar.", "بيتي كبير.", "أسماء", 0, true),
            VocabularyEntity(115, "besar", "besar", "بسار", "كبير", "Rumah ini besar.", "هذا البيت كبير.", "صفات", 0, true),
            VocabularyEntity(116, "kecil", "kecil", "كيتشيل", "صغير", "Buku kecil.", "كتاب صغير.", "صفات", 0, true),
            VocabularyEntity(117, "pergi", "pergi", "بيرجي", "يذهب", "Saya pergi ke pasar.", "أنا أذهب إلى السوق.", "أفعال", 0, true),
            VocabularyEntity(118, "pulang", "pulang", "بولانج", "يعود", "Saya pulang jam lima.", "أعود في الساعة الخامسة.", "أفعال", 0, true),
            VocabularyEntity(120, "duduk", "duduk", "دودوك", "يجلس", "Silakan duduk.", "تفضل بالجلوس.", "أفعال", 0, true),
            VocabularyEntity(121, "bicara", "bicara", "بيتشرا", "يتكلم", "Saya bisa bicara bahasa Indonesia.", "أستطيع التحدث بالإندونيسية.", "أفعال", 0, true),
            VocabularyEntity(122, "beli", "beli", "بيلي", "يشتري", "Saya mau beli ini.", "أريد أن أشتري هذا.", "أفعال", 0, true),
            VocabularyEntity(123, "jual", "jual", "جوال", "يبيع", "Dia jual buah.", "هو يبيع فواكه.", "أفعال", 0, true),
            VocabularyEntity(124, "tahu", "tahu", "تاهو", "يعرف", "Saya tidak tahu.", "أنا لا أعرف.", "أفعال", 0, true),
            VocabularyEntity(125, "suka", "suka", "سوكا", "يحب", "Saya suka kopi.", "أنا أحب القهوة.", "أفعال", 0, true),
            VocabularyEntity(126, "bisa", "bisa", "بيسا", "يستطيع", "Saya bisa bahasa Arab.", "أستطيع التحدث بالعربية.", "أفعال", 0, true),
            VocabularyEntity(127, "tinggi", "tinggi", "تينجي", "طويل", "Dia tinggi.", "هو طويل.", "صفات", 0, true),
            VocabularyEntity(128, "baru", "baru", "بارو", "جديد", "Buku baru.", "كتاب جديد.", "صفات", 0, true),
            VocabularyEntity(129, "panas", "panas", "باناس", "حار", "Cuaca panas hari ini.", "الطقس حار اليوم.", "صفات", 0, true),
            VocabularyEntity(130, "dingin", "dingin", "دينجين", "بارد", "Airnya dingin.", "الماء بارد.", "صفات", 0, true),
            VocabularyEntity(131, "ayah", "ayah", "آياه", "الأب", "Ayah saya bekerja.", "والدي يعمل.", "عائلة", 0, true),
            VocabularyEntity(132, "ibu", "ibu", "إيبو", "الأم", "Ibu saya di rumah.", "والدتي في البيت.", "عائلة", 0, true),
            VocabularyEntity(133, "kakak", "kakak", "كاكاك", "الأخ/الأخت الأكبر", "Kakak saya guru.", "أخي/أختي الأكبر مدرس/ة.", "عائلة", 0, true),
            VocabularyEntity(134, "adik", "adik", "أديك", "الأخ/الأخت الأصغر", "Adik saya masih kecil.", "أخي/أختي الأصغر لا يزال صغيراً.", "عائلة", 0, true),
            VocabularyEntity(135, "meja", "meja", "ميجا", "طاولة", "Buku di meja.", "الكتاب على الطاولة.", "أشياء", 0, true),
            VocabularyEntity(136, "kursi", "kursi", "كورسي", "كرسي", "Silakan duduk di kursi.", "تفضل بالجلوس على الكرسي.", "أشياء", 0, true),
            VocabularyEntity(137, "telepon", "telepon", "تيليبون", "هاتف", "Telepon saya baru.", "هاتفي جديد.", "أشياء", 0, true),
            VocabularyEntity(138, "kamar", "kamar", "كامار", "غرفة", "Kamar saya bersih.", "غرفتي نظيفة.", "أشياء", 0, true),
            VocabularyEntity(144, "kerja", "kerja", "كيرجا", "يعمل", "Saya kerja di kantor.", "أعمل في المكتب.", "أفعال", 0, true),
            VocabularyEntity(145, "belajar", "belajar", "بيلاجار", "يتعلم", "Saya belajar bahasa Indonesia.", "أنا أتعلم الإندونيسية.", "أفعال", 0, true),
            VocabularyEntity(150, "pasar", "pasar", "باسار", "سوق", "Saya pergi ke pasar.", "أنا أذهب إلى السوق.", "أماكن", 0, true),
            VocabularyEntity(151, "kantor", "kantor", "كانتور", "مكتب", "Dia kerja di kantor.", "هو يعمل في المكتب.", "أماكن", 0, true),
            VocabularyEntity(152, "sekolah", "sekolah", "سكولاه", "مدرسة", "Anak saya di sekolah.", "ابني في المدرسة.", "أماكن", 0, true),
            VocabularyEntity(153, "nasi", "nasi", "ناسي", "أرز", "Saya makan nasi.", "أنا آكل الأرز.", "طعام", 0, true),
            VocabularyEntity(154, "air", "air", "آير", "ماء", "Saya minum air.", "أنا أشرب الماء.", "طعام", 0, true),
            VocabularyEntity(155, "kopi", "kopi", "كوبي", "قهوة", "Saya suka kopi.", "أنا أحب القهوة.", "طعام", 0, true),
            VocabularyEntity(156, "teh", "teh", "تيه", "شاي", "Saya minum teh.", "أنا أشرب الشاي.", "طعام", 0, true),
            VocabularyEntity(160, "lama", "lama", "لاما", "قديم", "Buku lama.", "كتاب قديم.", "صفات", 0, true),
            VocabularyEntity(161, "murah", "murah", "موراه", "رخيص", "Ini murah.", "هذا رخيص.", "صفات", 0, true),
            VocabularyEntity(162, "mahal", "mahal", "ماهال", "غالي", "Harganya mahal.", "سعره غالي.", "صفات", 0, true),
            VocabularyEntity(163, "cepat", "cepat", "تشيبات", "سريع", "Dia cepat.", "هو سريع.", "صفات", 0, true),
            VocabularyEntity(164, "lambat", "lambat", "لامبات", "بطيء", "Dia lambat.", "هو بطيء.", "صفات", 0, true),
        )
        db.vocabularyDao().insertAll(vocabStage1)

        // Seed 100 Verbs
        val hundredVerbs = listOf(
            VocabularyEntity(1004, "bangun", "bangun", "بان-غون", "يستيقظ", "Saya bangun pagi.", "أستيقظ صباحاً.", "أفعال", 0, true),
            VocabularyEntity(1007, "datang", "datang", "دا-تانغ", "يأتي", "Teman saya datang.", "صديقي يأتي.", "أفعال", 0, true),
            VocabularyEntity(1008, "lihat", "lihat", "لي-هات", "يرى / ينظر", "Saya lihat burung.", "أرى طائرًا.", "أفعال", 0, true),
            VocabularyEntity(1009, "dengar", "dengar", "دين-غار", "يسمع", "Saya dengar radio.", "أستمع إلى الراديو.", "أفعال", 0, true),
            VocabularyEntity(1011, "baca", "baca", "با-تشا", "يقرأ", "Saya baca buku.", "أقرا كتابًا.", "أفعال", 0, true),
            VocabularyEntity(1012, "tulis", "tulis", "تو-ليس", "يكتب", "Saya tulis surat.", "أكتب رسالة.", "أفعال", 0, true),
            VocabularyEntity(1014, "mengajar", "mengajar", "مين-غا-جار", "يعلّم", "Guru mengajar murid.", "المعلم يعلّم تلميذًا.", "أفعال", 0, true),
            VocabularyEntity(1016, "main", "main", "ما-إين", "يلعب", "Anak-anak main bola.", "الأطفال يلعبون الكرة.", "أفعال", 0, true),
            VocabularyEntity(1019, "bayar", "bayar", "با-يار", "يدفع", "Saya bayar harga.", "أدفع الثمن.", "أفعال", 0, true),
            VocabularyEntity(1020, "terima", "terima", "تي-ري-ما", "يستلم / يقبل", "Saya terima hadiah.", "أستلم هدية.", "أفعال", 0, true),
            VocabularyEntity(1021, "beri", "beri", "بي-ري", "يعطي", "Saya beri kamu bunga.", "أعطيك زهرة.", "أفعال", 0, true),
            VocabularyEntity(1022, "ambil", "ambil", "أم-بيل", "يأخذ", "Tolong ambil buku itu.", "من فضلك خذ ذلك الكتاب.", "أفعال", 0, true),
            VocabularyEntity(1023, "bawa", "bawa", "با-وا", "يحضر / يحمل", "Dia bawa tas.", "هو يحمل حقيبة.", "أفعال", 0, true),
            VocabularyEntity(1024, "kirim", "kirim", "كي-ريم", "يرسل", "Saya kirim email.", "أرسل بريدًا إلكترونيًا.", "أفعال", 0, true),
            VocabularyEntity(1025, "tunggu", "tunggu", "تونغ-غو", "ينتظر", "Saya tunggu kamu.", "أنا أنتظرك.", "أفعال", 0, true),
            VocabularyEntity(1026, "cari", "cari", "تشا-ري", "يبحث عن", "Saya cari kunci.", "أبحث عن المفتاح.", "أفعال", 0, true),
            VocabularyEntity(1027, "temukan", "temukan", "تي-مو-كان", "يجد", "Saya temukan dompet.", "أجد محفظة.", "أفعال", 0, true),
            VocabularyEntity(1029, "berdiri", "berdiri", "بير-دي-ري", "يقف", "Murid berdiri.", "التلميذ يقف.", "أفعال", 0, true),
            VocabularyEntity(1030, "jalan", "jalan", "جا-لان", "يمشي / طريق", "Saya jalan ke pasar.", "أمشي إلى السوق.", "أفعال", 0, true),
            VocabularyEntity(1031, "lari", "lari", "لا-ري", "يركض", "Dia lari cepat.", "هو يركض بسرعة.", "أفعال", 0, true),
            VocabularyEntity(1032, "berenang", "berenang", "بي-ري-نانغ", "يسبح", "Mereka berenang di laut.", "هم يسبحون في البحر.", "أفعال", 1, true),
            VocabularyEntity(1033, "terbang", "terbang", "تير-بانغ", "يطير", "Burung itu terbang.", "ذلك الطائر يطير.", "أفعال", 1, true),
            VocabularyEntity(1034, "nyanyi", "nyanyi", "نيا-ني", "يغني", "Saya bisa nyanyi.", "أستطيع الغناء.", "أفعال", 0, true),
            VocabularyEntity(1035, "menari", "menari", "مي-نا-ري", "يرقص", "Gadis itu menari.", "تلك الفتاة ترقص.", "أفعال", 1, true),
            VocabularyEntity(1036, "masak", "masak", "ما-ساك", "يطبخ", "Ibu masak ikan.", "أمي تطبخ سمكًا.", "أفعال", 0, true),
            VocabularyEntity(1037, "cuci", "cuci", "تشو-تشي", "يغسل", "Saya cuci tangan.", "أغسل يدي.", "أفعال", 0, true),
            VocabularyEntity(1038, "bersihkan", "bersihkan", "بير-سيه-كان", "ينظف", "Tolong bersihkan meja.", "من فضلك نظف الطاولة.", "أفعال", 0, true),
            VocabularyEntity(1039, "setrika", "setrika", "سي-تري-كا", "يكوي", "Dia setrika baju.", "هو يكوي الملابس.", "أفعال", 1, true),
            VocabularyEntity(1040, "ganti", "ganti", "غان-تي", "يغيّر / يستبدل", "Saya ganti baju.", "أغير ثيابي.", "أفعال", 0, true),
            VocabularyEntity(1041, "pakai", "pakai", "با-كاي", "يرتدي / يستعمل", "Dia pakai topi.", "هو يرتدي قبعة.", "أفعال", 0, true),
            VocabularyEntity(1042, "lepas", "lepas", "لي-باس", "يخلع / يزيل", "Saya lepas sepatu.", "أخلع حذائي.", "أفعال", 1, true),
            VocabularyEntity(1043, "simpan", "simpan", "سيم-بان", "يحفظ / يضع", "Simpan uang di dompet.", "ضع النقود في المحفظة.", "أفعال", 0, true),
            VocabularyEntity(1044, "buka", "buka", "بو-كا", "يفتح", "Buka pintu!", "افتح الباب!", "أفعال", 0, true),
            VocabularyEntity(1045, "tutup", "tutup", "تو-توب", "يغلق", "Tutup jendela.", "أغلق النافذة.", "أفعال", 0, true),
            VocabularyEntity(1046, "hidup", "hidup", "هي-دوب", "يعيش", "Kakek masih hidup.", "جدي لا يزال يعيش.", "أفعال", 1, true),
            VocabularyEntity(1047, "mati", "mati", "ما-تي", "يموت", "Tanaman itu mati.", "ذلك النبات يموت.", "أفعال", 1, true),
            VocabularyEntity(1048, "nyalakan", "nyalakan", "نيا-لا-كان", "يشغّل (جهازًا)", "Nyalakan lampu.", "أشعل الضوء.", "أفعال", 1, true),
            VocabularyEntity(1049, "matikan", "matikan", "ما-تي-كان", "يطفئ", "Matikan televisi.", "أطفئ التلفاز.", "أفعال", 1, true),
            VocabularyEntity(1050, "panggil", "panggil", "بان-غيل", "ينادي / يستدعي", "Panggil polisi!", "استدعِ الشرطة!", "أفعال", 0, true),
            VocabularyEntity(1051, "jawab", "jawab", "جا-واب", "يجيب", "Dia jawab pertanyaan.", "يجيب على السؤال.", "أفعال", 0, true),
            VocabularyEntity(1052, "tanya", "tanya", "تا-نيا", "يسأل", "Saya tanya alamat.", "أسأل عن العنوان.", "أفعال", 0, true),
            VocabularyEntity(1053, "minta", "minta", "مين-تا", "يطلب", "Saya minta air.", "أطلب ماءً.", "أفعال", 0, true),
            VocabularyEntity(1054, "tolong", "tolong", "تو-لونغ", "يساعد", "Tolong saya!", "ساعدني!", "أفعال", 0, true),
            VocabularyEntity(1055, "berhenti", "berhenti", "بير-هين-تي", "يتوقف", "Hujan berhenti.", "المطر يتوقف.", "أفعال", 1, true),
            VocabularyEntity(1056, "mulai", "mulai", "مو-لاي", "يبدأ", "Pelajaran mulai jam 8.", "الدرس يبدأ الساعة الثامنة.", "أفعال", 0, true),
            VocabularyEntity(1057, "lanjutkan", "lanjutkan", "لان-جوت-كان", "يواصل", "Lanjutkan membaca.", "واصل القراءة.", "أفعال", 1, true),
            VocabularyEntity(1058, "ubah", "ubah", "أو-باه", "يغيّر / يعدّل", "Ubah kata itu.", "غيّر تلك الكلمة.", "أفعال", 1, true),
            VocabularyEntity(1059, "perbaiki", "perbaiki", "بير-با-ي-كي", "يصلح", "Perbaiki mesin.", "أصلح المحرك.", "أفعال", 1, true),
            VocabularyEntity(1060, "merusak", "merusak", "مي-رو-ساك", "يكسر / يتلف", "Jangan merusak mainan.", "لا تتلف اللعبة.", "أفعال", 1, true),
            VocabularyEntity(1061, "kehilangan", "kehilangan", "كي-هي-لان-غان", "يفقد", "Saya kehilangan kunci.", "أفقد المفتاح.", "أفعال", 1, true),
            VocabularyEntity(1062, "dapat", "dapat", "دا-بات", "يحصل على", "Saya dapat nilai bagus.", "أحصل على درجة جيدة.", "أفعال", 0, true),
            VocabularyEntity(1063, "kalah", "kalah", "كا-لاه", "يخسر (في لعبة)", "Tim kami kalah.", "فريقنا يخسر.", "أفعال", 1, true),
            VocabularyEntity(1064, "menang", "menang", "مي-نانغ", "يفوز", "Dia menang lomba.", "يفوز بالمسابقة.", "أفعال", 1, true),
            VocabularyEntity(1065, "ikut", "ikut", "إي-كوت", "يتبع / يشارك", "Saya ikut acara.", "أشارك في الحدث.", "أفعال", 0, true),
            VocabularyEntity(1066, "tinggal", "tinggal", "تينغ-غال", "يسكن / يبقى", "Saya tinggal di Jakarta.", "أسكن في جاكرتا.", "أفعال", 0, true),
            VocabularyEntity(1067, "pindah", "pindah", "بين-داه", "ينتقل", "Mereka pindah rumah.", "ينتقلون إلى منزل آخر.", "أفعال", 1, true),
            VocabularyEntity(1068, "kenal", "kenal", "كي-نال", "يعرف (شخصًا)", "Saya kenal dia.", "أعرفه.", "أفعال", 0, true),
            VocabularyEntity(1069, "ingat", "ingat", "إين-غات", "يتذكر", "Saya ingat nama itu.", "أتذكر ذلك الاسم.", "أفعال", 0, true),
            VocabularyEntity(1070, "lupa", "lupa", "لو-با", "ينسى", "Jangan lupa bawa kunci.", "لا تنسَ إحضار المفتاح.", "أفعال", 0, true),
            VocabularyEntity(1071, "pikir", "pikir", "بي-كير", "يفكر", "Saya pikir baik-baik.", "أفكر جيدًا.", "أفعال", 0, true),
            VocabularyEntity(1072, "merasa", "merasa", "مي-را-سا", "يشعر", "Saya merasa senang.", "أشعر بالسعادة.", "أفعال", 1, true),
            VocabularyEntity(1073, "harap", "harap", "ها-راب", "يأمل", "Saya harap kamu datang.", "آمل أن تأتي.", "أفعال", 1, true),
            VocabularyEntity(1074, "khawatir", "khawatir", "خا-وا-تير", "يقلق", "Jangan khawatir.", "لا تقلق.", "أفعال", 1, true),
            VocabularyEntity(1076, "benci", "benci", "بين-تشي", "يكره", "Dia benci kebohongan.", "يكره الكذب.", "أفعال", 1, true),
            VocabularyEntity(1077, "cinta", "cinta", "تشين-تا", "يحب (عاطفياً)", "Aku cinta kamu.", "أنا أحبك.", "أفعال", 0, true),
            VocabularyEntity(1078, "sayang", "sayang", "سا-يانغ", "يحب / يهتم بـ", "Ibu sayang anak.", "الأم تحب ولدها.", "أفعال", 1, true),
            VocabularyEntity(1079, "butuh", "butuh", "بو-توه", "يحتاج", "Saya butuh istirahat.", "أحتاج إلى راحة.", "أفعال", 0, true),
            VocabularyEntity(1080, "punya", "punya", "بو-نيا", "يملك", "Saya punya mobil.", "أملك سيارة.", "أفعال", 0, true),
            VocabularyEntity(1081, "mau", "mau", "ماو", "يريد", "Saya mau makan.", "أريد أن آكل.", "أفعال", 0, true),
            VocabularyEntity(1083, "boleh", "boleh", "بو-ليه", "يُسمح له", "Boleh saya masuk?", "هل يُسمح لي بالدخول؟", "أفعال", 0, true),
            VocabularyEntity(1084, "harus", "harus", "ها-روس", "يجب", "Kamu harus belajar.", "يجب أن تتعلم.", "أفعال", 0, true),
            VocabularyEntity(1085, "perhatikan", "perhatikan", "بير-ها-تي-كان", "ينتبه إلى", "Perhatikan guru!", "انتبه إلى المعلم!", "أفعال", 1, true),
            VocabularyEntity(1086, "tawar", "tawar", "تا-وار", "يفاصل / يساوم", "Boleh tawar harganya?", "هل يجوز مساومة السعر؟", "أفعال", 1, true),
            VocabularyEntity(1087, "nego", "nego", "ني-غو", "يفاوض", "Harga bisa nego.", "السعر قابل للتفاوض.", "أفعال", 1, true),
            VocabularyEntity(1088, "hitung", "hitung", "هي-تونغ", "يحسب", "Hitung total belanja.", "احسب إجمالي المشتريات.", "أفعال", 1, true),
            VocabularyEntity(1089, "pesan", "pesan", "بي-سان", "يطلب / يحجز", "Saya pesan kopi satu.", "أطلب قهوة واحدة.", "أفعال", 0, true),
            VocabularyEntity(1090, "cek", "cek", "تشيك", "يتحقق / يفحص", "Cek kualitas barang dulu.", "افحص جودة السلعة أولًا.", "أفعال", 1, true),
            VocabularyEntity(1091, "catat", "catat", "تشا-تات", "يسجل", "Catat pesanan pelanggan.", "سجل طلب الزبون.", "أفعال", 1, true),
            VocabularyEntity(1092, "untung", "untung", "أون-تونغ", "يربح", "Saya untung banyak hari ini.", "أربح كثيرًا اليوم.", "أفعال", 1, true),
            VocabularyEntity(1093, "rugi", "rugi", "رو-غي", "يخسر (مالياً)", "Jangan rugi, jual lebih tinggi.", "لا تخسر، بع بسعر أعلى.", "أفعال", 1, true),
            VocabularyEntity(1094, "ajak", "ajak", "أ-جاك", "يدعو / يقترح", "Aku ajak kamu nonton film.", "أدعوك لمشاهدة فيلم.", "أفعال", 1, true),
            VocabularyEntity(1095, "yakinkan", "yakinkan", "يا-كين-كان", "يقنع / يؤكد", "Coba yakinkan dia.", "حاول أن تقنعه.", "أفعال", 2, true),
            VocabularyEntity(1096, "percaya", "percaya", "بير-تشا-يا", "يصدق / يثق", "Saya percaya kamu.", "أصدقك / أثق بك.", "أفعال", 1, true),
            VocabularyEntity(1097, "buktikan", "buktikan", "بوك-تي-كان", "يثبت", "Kalau berani, buktikan!", "إذا كنت شجاعًا، أثبت ذلك!", "أفعال", 2, true),
            VocabularyEntity(1098, "tolak", "tolak", "تو-لاك", "يرفض", "Jangan tolak tawaran ini.", "لا ترفض هذا العرض.", "أفعال", 1, true),
            VocabularyEntity(1099, "setuju", "setuju", "سي-تو-جو", "يوافق", "Saya setuju dengan ide kamu.", "أوافق مع فكرتك.", "أفعال", 1, true),
            VocabularyEntity(1100, "pilih", "pilih", "بي-ليه", "يختار", "Pilih mana yang kamu suka.", "اختر أي شيء تفضله.", "أفعال", 0, true)
        )
        db.vocabularyDao().insertAll(hundredVerbs)

        // === TURKISH CURRICULUM SEEDING ===

        // 1. Turkish Stages
        val turkishStages = listOf(
            StageEntity(2001, "المرحلة 1 — الصفر", "Tahap 1 - Nol", "الأبجدية، الأرقام، الألوان، فصول السنة، أيام الأسبوع", 0, true, "TR"),
            StageEntity(2002, "المرحلة 2 — المبتدئ", "Tahap 2 - Pemula", "الضمائر الشخصية والملكية، لاحقة الجمع، وتركيب الجملة", 1, false, "TR")
        )
        db.stageDao().insertAll(turkishStages)

        // 2. Turkish Lessons (Fully Expanded 18-Lesson Curriculum - Zeynep Masri Standard)
        val turkishLessons = listOf(
            LessonEntity(201, 0, "الدرس الأول: الأبجدية التركية", "Türk Alfabesi", "تعلم الحروف الصوتية والساكنة بالتفصيل", "content201", false, "TR"),
            LessonEntity(202, 0, "الدرس الثاني: الأرقام التركية", "Sayılar", "الأرقام من 0 إلى 100 وكيفية صياغتها", "content202", false, "TR"),
            LessonEntity(203, 0, "الدرس الثالث: الأعداد الترتيبية", "Sıra Sayıları", "ترتيب الأشياء واللواحق الصوتية بالتوافق", "content203", false, "TR"),
            LessonEntity(204, 0, "الدرس الرابع: الألوان الأساسية", "Renkler", "الألوان وتصنيفاتها وصياغتها النعتية", "content204", false, "TR"),
            LessonEntity(205, 1, "الدرس الخامس: الضمائر الشخصية", "Şahıs Zamirleri", "الضمائر الشخصية الستة وقاعدة غياب الجنس", "content205", false, "TR"),
            LessonEntity(206, 1, "الدرس السادس: لاحقة الجمع", "Çoğul Eki Kuralları", "قاعدة الجمع الثنائي بالتوافق الصوتي", "content206", false, "TR"),
            LessonEntity(207, 0, "الدرس السابع: التحيات واللقاء", "Selamlaşma", "كيف تحيي الأشخاص وتسأل عن حالهم", "content207", false, "TR"),
            LessonEntity(208, 0, "الدرس الثامن: أيام الأسبوع والفصول", "Günler & Mevsimler", "الأيام وفصول السنة بالتوافق الصوتي", "content208", false, "TR"),
            LessonEntity(209, 1, "الدرس التاسع: أداة السؤال بهل", "Soru Eki", "قاعدة السؤال بهل الرباعية التوافق", "content209", false, "TR"),
            LessonEntity(210, 1, "الدرس العاشر: الملكية الأساسية", "İyelik Zamirleri", "صياغة الملكية بواسطة اللواحق الطرفية", "content210", false, "TR"),
            LessonEntity(211, 1, "الدرس الحادي عشر: الجملة الاسمية", "İsim Cümlesi", "كيف تصيغ خبر المبتدأ باللواحق الشخصية", "content211", false, "TR"),
            LessonEntity(212, 1, "الدرس الثاني عشر: حروف الجر والاتجاه", "Durum Ekleri", "حالات الجر والصدور والاقامة في الأسماء", "content212", false, "TR"),
            LessonEntity(213, 1, "الدرس الثالث عشر: الأفعال والمصدر", "Fiiller & Mastar", "جذور الأفعال ومفهوم المصدر والنهي", "content213", false, "TR"),
            LessonEntity(214, 1, "الدرس الرابع عشر: الحاضر المستمر", "Şimdiki Zaman", "صياغة الفعل في الحاضر المستمر والحدث الجاري", "content214", false, "TR"),
            LessonEntity(215, 1, "الدرس الخامس عشر: نفي الحاضر", "Şimdiki Zaman Olumsuz", "كيف تنفي حدوث الفعل في الزمن الحاضر", "content215", false, "TR"),
            LessonEntity(216, 1, "الدرس السادس عشر: سؤال الحاضر", "Şimdiki Zaman Soru", "كيف تسأل في الزمن الحاضر المستمر", "content216", false, "TR"),
            LessonEntity(217, 1, "الدرس السابع عشر: الماضي الشهودي", "Belirli Geçmiş Zaman", "صياغة الأفعال في الماضي المحقق ولواحقها", "content217", false, "TR"),
            LessonEntity(218, 1, "الدرس الثامن عشر: المراجعة الشاملة", "Genel Tekrar", "مراجعة وتلخيص كامل لقواعد المستويين", "content218", false, "TR")
        )
        db.lessonDao().insertAll(turkishLessons)

        // 3. Turkish Lesson Details (Complete 18 Details matches parent)
        val turkishLessonDetails = listOf(
            LessonDetailEntity(201, 201, "ستتعلم الأبجدية التركية المكونة من 29 حرفاً ونطقها السليم.",
                "الحروف الصوتية الـ 8 هي أساس نطق اللغة وتقسم لثقيلة (a, ı, o, u) وخفيفة (e, i, ö, ü).",
                "A = ا • B = ب • C = ج • Ç = تش • D = د • E = اِ • F = ف • G = غ • Ğ = غ خفيفة صامتة • H = هـ",
                "قاعدة التوافق الصوتي تعتمد بالكامل على آخر حرف صوتي في الكلمة.", "الحرف Ğ لا ينطق بل يمد الحرف الذي قبله.", "Formal & Casual: sama"),
            
            LessonDetailEntity(202, 202, "ستتعلم الأرقام التركية وكيف تصيغها بسهولة.",
                "1 = Bir\n2 = İki\n3 = Üç\n4 = Dört\n5 = Beş\n6 = Altı\n7 = Yedi\n8 = Sekiz\n9 = Dokuz\n10 = On",
                "10 = On • 20 = Yirmi • 30 = Otuz • 40 = Kırk • 50 = Elli • 60 = Altmış • 70 = Yetmiş • 80 = Seksen • 90 = Doksan • 100 = Yüz",
                "لصياغة أي رقم، نضع العشرات أولاً ثم الآحاد: 21 = Yirmi Bir.", "الأرقام تستخدم نفس الترتيب كالعربية.", "Formal & Casual: sama"),

            LessonDetailEntity(203, 203, "ستتعلم صياغة الأعداد الترتيبية (الأول، الثاني...) باللواحق.",
                "تضاف اللاحقة (-ıncı, -inci, -uncu, -üncü) بناءً على قاعدة التوافق الصوتي الرباعي.",
                "1. = Birinci\n2. = İkinci\n3. = Üçüncü\n4. = Dördüncü\n5. = Beşinci",
                "إذا انتهى الرقم بحرف صوتي، نحذف الحرف الأول من اللاحقة: iki + nci = ikinci.", "تستخدم بكثرة لقراءة العناوين والتواريخ.", "Formal & Casual: sama"),

            // ملاحظة: كان هذا الصف يمرر 7 وسائط لبانٍ يتطلب 8 — خطأ تجميع فعلي
            // موجود في الكود المُسلَّم. أُضيف حقل commonMistakes المفقود.
            LessonDetailEntity(204, 204, "ستتعلم الألوان الأساسية باللغة التركية.",
                "Mavi = أزرق\nKırmızı = أحمر\nYeşil = أخضر\nSarı = أصفر\nSiyah = أسود\nBeyaz = أبيض\nTuruncu = برتقالي",
                "صفة اللون تأتي دائماً قبل الاسم الموصوف: kırmızı araba = السيارة الحمراء.",
                "تأتي الصفة قبل الاسم كما في الإنجليزية والفرنسية.",
                "خطأ شائع: قول 'araba kırmızı' بترتيب عربي. الصحيح 'kırmızı araba'.",
                "Formal & Casual: sama"),

            // ملاحظة: كان هذا الصف أيضاً يمرر 7 وسائط بدل 8 — خطأ تجميع فعلي.
            LessonDetailEntity(205, 205, "ستتعلم الضمائر الشخصية الستة في اللغة التركية.",
                "Ben = أنا\nSen = أنت/أنتِ\nO = هو/هي\nBiz = نحن\nSiz = أنتم/أنتن\nOnlar = هم/هن",
                "لا يوجد جنس تذكير وتأنيث في اللغة التركية، الضمير O يصلح للغائب مطلقاً.",
                "الضمير O يمثل أيضاً اسم الإشارة 'ذلك للبعيد جداً'.",
                "خطأ شائع: البحث عن ضمير مؤنث مقابل 'هي'. لا وجود له — O تكفي.",
                "Formal & Casual: sama"),

            LessonDetailEntity(206, 206, "ستتعلم كيفية جمع الأسماء التركية باستخدام قاعدة التوافق الثنائي.",
                "نضيف اللاحقة -lar للأحرف الثقيلة (a, ı, o, u) ونضيف -ler للأحرف الخفيفة (e, i, ö, ü).",
                "Kitap -> Kitaplar (كتب)\nEv -> Evler (بيوت)\nAraba -> Arabalar (سيارات)",
                "آخر حرف صوتي في الكلمة يحدد شكل الجمع تماماً: e(خفيف) -> ler، a(ثقيل) -> lar.", "هذه هي القاعدة الذهبية في الصرف التركي.", "Formal & Casual: sama"),

            LessonDetailEntity(207, 207, "ستتعلم كيفية إلقاء التحية ومصطلحات اللقاء بالتركية.",
                "Merhaba = مرحباً\nNasılsın? = كيف حالك؟\nGünaydın = صباح الخير\nİyi günler = نهارك سعيد\nHoşça kal = وداعاً\nGüle güle = مع السلامة",
                "Hoş geldin = أهلاً بك • Hoş bulduk = أهلاً بك (رد الزائر)", "تستخدم هذه التحيات يومياً وفي جميع المناسبات الرسمية والودية.", "لا تخلط بين Hoşça kal للمغادر و Güle güle للمستقبل.", "Formal & Casual: sama"),

            LessonDetailEntity(208, 208, "ستتعلم أيام الأسبوع وفصول السنة بالتوافق الصوتي.",
                "أيام الأسبوع السبعة وفصول السنة الأربعة بالتركية ولواحقها المريحة.",
                "İlkbahar = الربيع • Yaz = الصيف • Sonbahar = الخريف • Kış = الشتاء", "gün = يوم • hafta = أسبوع • mevsim = فصل", "Pazartesi هو أول أيام الأسبوع في تركيا.", "Formal & Casual: sama"),

            LessonDetailEntity(209, 209, "ستتعلم قاعدة السؤال بهل الرباعية التوافق.",
                "نستخدم اللاحقة (mı, mi, mu, mü) للسؤال بهل بناءً على آخر حرف صوتي في الخبر.",
                "Bu okul mu? = هل هذه مدرسة؟\nBu ev mi? = هل هذا بيت؟\nBu kitap mı? = هل هذا كتاب؟",
                "a, ı -> mı • e, i -> mi • o, u -> mu • ö, ü -> mü", "أداة السؤال تكتب منفصلة دائماً عن الاسم وتتبع قاعدة التوافق الصوتي.", "Formal & Casual: sama"),

            LessonDetailEntity(210, 210, "ستتعلم صياغة الملكية عبر اللواحق الملتصقة بالأسماء.",
                "ضمائر الملكية واللواحق التابعة لها (Benim evim = بيتي).",
                "Benim evim • Senin evin • Onun evi • Bizim evimiz • Sizin eviniz • Onların evleri",
                "إذا انتهى الاسم بحرف علة تضاف لواحق مختصرة: araba -> arabam (سيارتي).", "الملكية الطبيعية في التركية تحتاج اللاحقة الطرفية دائماً.", "Formal & Casual: sama"),

            LessonDetailEntity(211, 211, "ستتعلم صياغة الخبر والجملة الاسمية بالضمائر واللواحق الشخصية.",
                "كيف تخبر عن حالتك بالضمائر الشخصية (أنا طالب = öğrenciyim).",
                "Ben öğrenciyim • Sen öğrencisin • O öğrenci • Biz öğrenciyiz • Siz öğrencisiniz • Onlar öğrenciler",
                "تضاف حرف الوصل y عند التقاء حرفين صوتيين: öğrenci + im = öğrenciyim.", "الخبر يتبع التوافق الصوتي الرباعي باللواحق الشخصية.", "Formal & Casual: sama"),

            LessonDetailEntity(212, 212, "ستتعلم حالات الاسم الأربعة الأساسية لربط الاتجاه والجر والاقامة.",
                "حالات الجر والصدور والاقامة والمفعولية به بالتوافق الصوتي.",
                "Okula gidiyorum (إلى المدرسة) • Evde kalıyorum (في البيت) • Okuldan geliyorum (من المدرسة) • Kitabı okuyorum (أقرأ الكتاب)",
                "إلى: -e/-a • في: -de/-da • من: -den/-dan • المفعول به: -i/-ı/-u/-ü", "يتأثر حرف d ويتحول لـ t بعد الأحرف الصامتة الشديدة (Fıstıkçı Şahap).", "Formal & Casual: sama"),

            LessonDetailEntity(213, 213, "ستتعلم مفهوم الفعل وجذر الفعل والصياغة المصدرية والنهي.",
                "المصدر ينتهي بـ -mak / -mek وجذر الفعل يمثل صيغة الأمر المباشرة.",
                "Gelmek = المجيء • Gel = تعال (جذر) • Gitmek = الذهاب • Git = اذهب (جذر)",
                "للنهي تضاف لاحقة النفي -ma / -me بعد الجذر مباشرة: Gelme = لا تأتِ • Gitme = لا تذهب.", "جذر الفعل هو أساس تصريف الأزمنة جميعها.", "Formal & Casual: sama"),

            LessonDetailEntity(214, 214, "ستتعلم صياغة الزمن الحاضر المستمر للحدث الجاري حالياً.",
                "تضاف لاحقة الزمن الحاضر -iyor / -ıyor / -uyor / -üyor بعد الجذر مباشرة تليها لاحقة الفاعل الشخصية.",
                "Ben geliyorum = أنا آتٍ • Sen geliyorsun = أنت آتٍ • O geliyor = هو آتٍ",
                "gel (جذر) + iyor (زمن) + um (فاعل أنا) = geliyorum.", "الفعل يتبع التوافق الصوتي الرباعي بدقة.", "Formal & Casual: sama"),

            LessonDetailEntity(215, 215, "ستتعلم نفي الفعل في الزمن الحاضر المستمر بسهولة.",
                "ينفى الفعل بوضع لاحقة النفي الضيقة (mı, mi, mu, mü) بعد الجذر وقبل لاحقة الزمن -iyor.",
                "Gelmiyorum = أنا لا آتي • Gitmiyorsun = أنت لا تذهب • Okumuyor = هو لا يقرأ",
                "gel (جذر) + mi (نفي) + yor (زمن) + um (فاعل) = gelmiyorum.", "أداة النفي تمنع التقاء الأحرف الصوتية العريضة.", "Formal & Casual: sama"),

            LessonDetailEntity(216, 216, "ستتعلم صياغة السؤال بهل بداخل الزمن الحاضر المستمر.",
                "يصاغ السؤال بوضع أداة السؤال mu منفصلة بعد الفعل وتحمل اللاحقة الشخصية للفاعل.",
                "Gidiyor musun? = هل أنت ذاهب؟ • Geliyor musunuz? = هل أنتم آتون؟ • Okuyor mu? = هل هو يقرأ؟",
                "gidiyor (فعل مستمر) + mu (سؤال) + sun (أنت) = gidiyor musun?.", "الضمير الشخصي للفاعل يلتصق دائماً بأداة السؤال المكتوبة منفصلة.", "Formal & Casual: sama"),

            LessonDetailEntity(217, 217, "ستتعلم صياغة الفعل في الزمن الماضي الشهودي المحقق والمثبت.",
                "الماضي الشهودي يصاغ بإضافة اللاحقة -di / -dı / -du / -dü بعد الجذر مباشرة تليها اللاحقة الشخصية المختصرة.",
                "Gittim = ذهبتُ • Gittin = ذهبتَ • Gitti = ذهب/ذهبت • Gittik = ذهبنا • Gittiniz = ذهبتم • Gittiler = ذهبوا",
                "git (جذر) + ti (زمن ماضي) + m (أنا) = gittim. (تحول d إلى t للتوافق مع t الشديدة).", "الماضي الشهودي يفيد بحدوث الفعل ومعاينته شخصياً.", "Formal & Casual: sama"),

            LessonDetailEntity(218, 218, "مراجعة شاملة وتلخيص مكثف لكافة القواعد والأنماط التأسيسية التي تعلمتها.",
                "مراجعة عامة للأبجدية، الأرقام، الجمع، السؤال، الملكية، الحالات النحوية، تصريف الأفعال والأزمنة.",
                "Ben öğrenciyim • Benim evim • Okula gidiyorum • Geliyorum • Gittim",
                "الطلاقة تنشأ من الاسترجاع المتباعد والتطبيق العملي للجمل والقوالب التأسيسية.", "استمر في ممارسة الألعاب والتدوين لترسيخ اللغة.", "Formal & Casual: mixed")
        )
        db.lessonDetailDao().insertAll(turkishLessonDetails)

        // 4. Turkish Vocabulary (Expanded with turk_duzenlenmis.md)
        val turkishVocab = listOf(
            VocabularyEntity(2001, "olmak", "olmak", "أول-ماك", "يكون / يصبح", "Şimdi hasta oluyorum.", "الآن أنا أصبح مريضاً.", "أفعال", 0, true, false, "TR"),
            VocabularyEntity(2002, "yapmak", "yapmak", "ياب-ماك", "يفعل / يصنع", "Ödev yapıyorum.", "أنا أفعل الواجب.", "أفعال", 0, true, false, "TR"),
            VocabularyEntity(2003, "gitmek", "gitmek", "غيت-ميك", "يذهب", "Okula gidiyorum.", "أنا أذهب إلى المدرسة.", "أفعال", 0, true, false, "TR"),
            VocabularyEntity(2004, "almak", "almak", "آل-ماك", "يأخذ / يشتري", "Ekmek alıyorum.", "أنا أشتري خبزاً.", "أفعال", 0, true, false, "TR"),
            VocabularyEntity(2005, "vermek", "vermek", "فير-ميك", "يعطي", "Sana kalem veriyorum.", "أنا أعطيك قلماً.", "أفعال", 0, true, false, "TR"),
            VocabularyEntity(2006, "sevmek", "sevmek", "سيف-ميك", "يحب", "Seni çok seviyorum.", "أنا أحبك كثيراً.", "أفعال", 0, true, false, "TR"),
            VocabularyEntity(2007, "istemek", "istemek", "إيس-تي-ميك", "يريد", "Su istiyorum.", "أنا أريد ماءً.", "أفعال", 0, true, false, "TR"),
            
            // Days of the week
            VocabularyEntity(2008, "pazartesi", "pazartesi", "با-زار-تي-سي", "الاثنين", "Bugün pazartesi.", "اليوم هو الاثنين.", "أيام", 0, true, false, "TR"),
            VocabularyEntity(2009, "salı", "salı", "سا-لي", "الثلاثاء", "Bugün salı.", "اليوم هو الثلاثاء.", "أيام", 0, true, false, "TR"),
            VocabularyEntity(2010, "çarşamba", "çarşamba", "تشار-شام-با", "الأربعاء", "Bugün çarşamba.", "اليوم هو الأربعاء.", "أيام", 0, true, false, "TR"),
            
            // Colors
            VocabularyEntity(2011, "mavi", "mavi", "ما-في", "أزرق", "Mavi deniz.", "البحر الأزرق.", "ألوان", 0, true, false, "TR"),
            VocabularyEntity(2012, "kırmızı", "kırmızı", "كير-مي-زي", "أحمر", "Kırmızı araba.", "السيارة الحمراء.", "ألوان", 0, true, false, "TR"),
            VocabularyEntity(2013, "yeşil", "yeşil", "يي-شيل", "أخضر", "Yeşil elma.", "التفاحة الخضراء.", "ألوان", 0, true, false, "TR"),
            
            // Family (New from turk_duzenlenmis.md)
            VocabularyEntity(2014, "anne", "anne", "أن-نيه", "أم", "Benim annem çok iyi.", "أمي طيبة جداً.", "عائلة", 0, true, false, "TR"),
            VocabularyEntity(2015, "baba", "baba", "با-با", "أب", "Benim babam öğretmen.", "أبي معلم.", "عائلة", 0, true, false, "TR"),
            VocabularyEntity(2016, "kardeş", "kardeş", "كار-ديش", "أخ / أخت", "Benim bir kardeşim var.", "لدي أخ واحد.", "عائلة", 0, true, false, "TR"),
            
            // House (New from turk_duzenlenmis.md)
            VocabularyEntity(2017, "ev", "ev", "إيف", "بيت / منزل", "Bu ev çok büyük.", "هذا البيت كبير جداً.", "أماكن", 0, true, false, "TR"),
            VocabularyEntity(2018, "kapı", "kapı", "كا-بي", "باب", "Kapıyı kapat lütfen.", "أغلق الباب من فضلك.", "أشياء", 0, true, false, "TR"),
            VocabularyEntity(2019, "masa", "masa", "ما-سا", "طاولة", "Kitap masada.", "الكتاب على الطاولة.", "أشياء", 0, true, false, "TR"),
            
            // Food & Drink (New from turk_duzenlenmis.md)
            VocabularyEntity(2020, "su", "su", "سو", "ماء", "Bir su lütfen.", "ماء من فضلك.", "طعام", 0, true, false, "TR"),
            VocabularyEntity(2021, "ekmek", "ekmek", "إيك-ميك", "خبز", "Sıcak ekmek.", "خبز ساخن.", "طعام", 0, true, false, "TR"),
            VocabularyEntity(2022, "çay", "çay", "تشاي", "شاي", "Çay istiyorum.", "أريد شاياً.", "طعام", 0, true, false, "TR"),
            
            // Adjectives (New from turk_duzenlenmis.md)
            VocabularyEntity(2023, "iyi", "iyi", "إي-يي", "جيد / بخير", "Ben iyiyim.", "أنا بخير.", "صفات", 0, true, false, "TR"),
            VocabularyEntity(2024, "büyük", "büyük", "بو-يوك", "كبير", "Bu araba büyük.", "هذه السيارة كبيرة.", "صفات", 0, true, false, "TR"),
            VocabularyEntity(2025, "ucuz", "ucuz", "أو-جوز", "رخيص", "Bu ucuz bir kitap.", "هذا كتاب رخيص.", "صفات", 0, true, false, "TR"),
            VocabularyEntity(2026, "pahalı", "pahalı", "با-ها-لي", "غالي", "Bu çok pahalı.", "هذا غالي جداً.", "صفات", 0, true, false, "TR")
        )
        db.vocabularyDao().insertAll(turkishVocab)

        // 5. Turkish Grammar Rules (Expanded with turk_duzenlenmis.md)
        val turkishGrammar = listOf(
            GrammarEntity(2001, "بنية الجملة التركية (SOV)", "Cümle Yapısı", "ترتيب الجملة: فاعل + مفعول به + فعل. الفعل يأتي دائماً في نهاية الجملة خلافاً للإندونيسية.", "S + O + V", "Ben kitap okuyorum. (أنا أقرأ كتاباً)", 0, "TR"),
            GrammarEntity(2002, "قاعدة الجمع الثنائي", "Çoğul Eki Kuralları", "لاحقة الجمع تكون -lar للأحرف الصوتية الثقيلة (a, ı, o, u) وتكون -ler للأحرف الصوتية الخفيفة (e, i, ö, ü).", "lar / ler", "Arabalar (السيارات) / Evler (البيوت)", 1, "TR"),
            
            // New from turk_duzenlenmis.md
            GrammarEntity(2003, "الجملة الاسمية (ضمير الفاعل والخبر)", "İsim Cümlesi", "تصاغ الجملة الاسمية بإضافة لاحقة الضمير في نهاية الصفة أو الخبر (مثال: أنا طالب -> öğrenciyim).", "ım / sin / yim", "Ben öğrenciyim (أنا طالب) • Sen öğrencisin (أنت طالب)", 0, "TR"),
            GrammarEntity(2004, "ضمائر الملكية التركية", "İyelik Zamirleri", "الملكية الطبيعية في التركية تحتاج إلى صفة ملكية قبل الاسم ملحوقة بلاحقة ملكية مطابقة في نهاية الاسم.", "benim ... -im / senin ... -in", "Benim evim (بيتي) • Senin evin (بيتك)", 1, "TR"),
            GrammarEntity(2005, "حالات الاسم الأربعة (الجر والمفعولية)", "İsmin Halleri", "تتغير نهايات الأسماء التركية عند الجر: إلى (-e/-a)، في (-de/-da)، من (-den/-dan)، والمفعول المحدد (-i/-ı).", "e / de / den / i", "Okula gidiyorum (أذهب إلى المدرسة) • Evde kalıyorum (أقيم في البيت)", 1, "TR")
        )
        db.grammarDao().insertAll(turkishGrammar)

        // 6. Turkish Dialogues
        val turkishDialogues = listOf(
            DialogueEntity(2001, "التعارف بالتركية", "Tanışma", "A: Merhaba, benim adım Ahmet. Senin adın ne?\nB: Merhaba Ahmet, benim adım Zeynep. Memnun oldum.\nA: Ben de memnun oldum. Nasılsın?\nB: İyiyim, teşekkür ederim. Sen nasılsın?\nA: Ben de iyiyim, sağ ol.", 0, "TR")
        )
        db.dialogueDao().insertAll(turkishDialogues)

        // ==========================================================
        // 7. Turkish practice content
        //
        // سبب الإضافة: كانت التركية تحتوي على دروس ومفردات فقط،
        // بلا أي تعبيرات يومية أو سيناريوهات أو أسئلة تدريب.
        // فكان متعلم التركية يفتح "اللغة اليومية" و"اختبار سريع"
        // فيحصل على محتوى إندونيسي (بعد إضافة الترشيح باللغة كان سيحصل على فراغ).
        //
        // المحتوى تركي طبيعي، وليس ترجمة حرفية للمحتوى الإندونيسي
        // (مطلب صريح: التركية تحتاج منهجاً يناسب طبيعتها).
        // ==========================================================

        val turkishCasual = listOf(
            // تحية وتعارف
            CasualExpressionEntity(3001, "Merhaba", "ميرحابا", "مرحباً", "🟢 رسمي", "تحية عامة في أي وقت", null, "تحيات", 0, "TR"),
            CasualExpressionEntity(3002, "Selam", "سيلام", "سلام / أهلاً", "🔵 يومي", "تحية بين الأصدقاء والزملاء", "Merhaba", "تحيات", 0, "TR"),
            CasualExpressionEntity(3003, "Naber?", "نابر", "ما الأخبار؟", "🟠 عامي", "بين الأصدقاء المقربين فقط", "Nasılsın?", "أصدقاء", 0, "TR"),
            CasualExpressionEntity(3004, "Nasılsın?", "ناسيلسين", "كيف حالك؟", "🔵 يومي", "سؤال عن الحال (مفرد)", "Nasılsınız?", "تحيات", 0, "TR"),
            CasualExpressionEntity(3005, "İyiyim, sağ ol", "إييييم، ساغ أول", "بخير، شكراً", "🔵 يومي", "رد على السؤال عن الحال", "İyiyim, teşekkür ederim", "تحيات", 0, "TR"),
            CasualExpressionEntity(3006, "Görüşürüz", "غوروشوروز", "إلى اللقاء", "🔵 يومي", "عند الوداع", null, "تحيات", 0, "TR"),
            CasualExpressionEntity(3007, "Hoş geldiniz", "هوش غيلدينيز", "أهلاً وسهلاً", "🟢 رسمي", "ترحيب بالضيف", null, "تحيات", 0, "TR"),
            CasualExpressionEntity(3008, "Hoş bulduk", "هوش بولدوك", "أهلاً بك (رد)", "🟢 رسمي", "الرد الثابت على Hoş geldiniz", null, "تحيات", 0, "TR"),

            // شكر واعتذار
            CasualExpressionEntity(3009, "Teşekkür ederim", "تيشيككور إيديريم", "شكراً لك", "🟢 رسمي", "شكر مهذب", null, "تعبيرات ودية", 0, "TR"),
            CasualExpressionEntity(3010, "Sağ ol", "ساغ أول", "شكراً", "🔵 يومي", "شكر سريع بين الأصدقاء", "Teşekkür ederim", "تعبيرات ودية", 0, "TR"),
            CasualExpressionEntity(3011, "Rica ederim", "ريجا إيديريم", "العفو", "🟢 رسمي", "الرد على الشكر", null, "تعبيرات ودية", 0, "TR"),
            CasualExpressionEntity(3012, "Özür dilerim", "أوزور ديليريم", "أعتذر", "🟢 رسمي", "اعتذار عن خطأ", null, "تعبيرات ودية", 0, "TR"),
            CasualExpressionEntity(3013, "Pardon", "باردون", "عفواً / معذرة", "🔵 يومي", "للمرور أو لفت الانتباه", null, "شارع", 0, "TR"),
            CasualExpressionEntity(3014, "Önemli değil", "أونيملي ديّيل", "لا يهم / لا بأس", "🔵 يومي", "رد على الاعتذار", null, "تعبيرات ودية", 0, "TR"),

            // السوق والتسوق — الأهم عملياً
            CasualExpressionEntity(3015, "Bu ne kadar?", "بو نه قدر", "كم سعر هذا؟", "🔵 يومي", "السؤال عن السعر", null, "سوق", 0, "TR"),
            CasualExpressionEntity(3016, "Çok pahalı!", "تشوك باهالي", "غالٍ جداً!", "🔵 يومي", "بداية التفاوض", null, "سوق", 0, "TR"),
            CasualExpressionEntity(3017, "Biraz indirim yapar mısınız?", "بيراز إينديريم يابار مِسينيز", "هل تعمل لي خصماً قليلاً؟", "🟢 رسمي", "طلب خصم بأدب", null, "سوق", 0, "TR"),
            CasualExpressionEntity(3018, "Son fiyat ne?", "سون فييات نه", "ما آخر سعر؟", "🔵 يومي", "إنهاء التفاوض", null, "سوق", 0, "TR"),
            CasualExpressionEntity(3019, "Bunu alıyorum", "بونو آلييوروم", "سآخذ هذا", "🔵 يومي", "قرار الشراء", null, "سوق", 0, "TR"),
            CasualExpressionEntity(3020, "Sadece bakıyorum", "ساديجه باكييوروم", "أنا أتفرج فقط", "🔵 يومي", "رد مهذب على البائع", null, "سوق", 0, "TR"),
            CasualExpressionEntity(3021, "Kartla ödeyebilir miyim?", "كارتلا أوديه‌بيلير مييم", "هل أستطيع الدفع بالبطاقة؟", "🟢 رسمي", "عند الدفع", null, "سوق", 0, "TR"),
            CasualExpressionEntity(3022, "Poşet alabilir miyim?", "بوشيت آلابيلير مييم", "هل يمكنني أخذ كيس؟", "🔵 يومي", "عند الدفع", null, "سوق", 0, "TR"),

            // المطعم
            CasualExpressionEntity(3023, "Hesap lütfen", "حساب لوتفن", "الحساب من فضلك", "🔵 يومي", "طلب الفاتورة", null, "مطعم", 0, "TR"),
            CasualExpressionEntity(3024, "Afiyet olsun", "عافيت أولسون", "بالهناء والشفاء", "🔵 يومي", "تقال قبل/بعد الأكل", null, "مطعم", 0, "TR"),
            CasualExpressionEntity(3025, "Bir çay lütfen", "بير تشاي لوتفن", "شاي واحد من فضلك", "🔵 يومي", "طلب مشروب", null, "مطعم", 0, "TR"),
            CasualExpressionEntity(3026, "Menüye bakabilir miyim?", "مينويه باكابيلير مييم", "هل يمكنني رؤية القائمة؟", "🟢 رسمي", "بداية الطلب", null, "مطعم", 0, "TR"),

            // الشارع والاتجاهات
            CasualExpressionEntity(3027, "Nerede?", "نيريده", "أين؟", "🔵 يومي", "سؤال عن مكان", null, "شارع", 0, "TR"),
            CasualExpressionEntity(3028, "Nasıl gidebilirim?", "ناسيل غيدبيليريم", "كيف أذهب؟", "🟢 رسمي", "طلب إرشاد", null, "شارع", 0, "TR"),
            CasualExpressionEntity(3029, "Sağa dön", "ساغا دون", "انعطف يميناً", "🔵 يومي", "إرشاد", null, "شارع", 0, "TR"),
            CasualExpressionEntity(3030, "Sola dön", "سولا دون", "انعطف يساراً", "🔵 يومي", "إرشاد", null, "شارع", 0, "TR"),
            CasualExpressionEntity(3031, "Düz git", "دوز غيت", "امشِ مستقيماً", "🔵 يومي", "إرشاد", null, "شارع", 0, "TR"),
            CasualExpressionEntity(3032, "Yardım eder misiniz?", "ياردم إيدير ميسينيز", "هل تساعدني؟", "🟢 رسمي", "طلب مساعدة", null, "شارع", 0, "TR"),

            // ردود وتفاعل يومي
            CasualExpressionEntity(3033, "Tamam", "تمام", "حسناً", "🔵 يومي", "موافقة", null, "عام", 0, "TR"),
            CasualExpressionEntity(3034, "Olur", "أولور", "يصير / ممكن", "🔵 يومي", "قبول اقتراح", null, "عام", 0, "TR"),
            CasualExpressionEntity(3035, "Tabii ki", "طبيعي كي", "بالتأكيد", "🔵 يومي", "تأكيد قوي", null, "عام", 0, "TR"),
            CasualExpressionEntity(3036, "Bilmiyorum", "بيلمييوروم", "لا أعرف", "🔵 يومي", "رد صادق", null, "عام", 0, "TR"),
            CasualExpressionEntity(3037, "Anlamadım", "آنلامادم", "لم أفهم", "🔵 يومي", "مهمة جداً للمبتدئ", null, "عام", 0, "TR"),
            CasualExpressionEntity(3038, "Yavaş konuşur musunuz?", "ياواش كونوشور موسونوز", "هل تتكلم ببطء؟", "🟢 رسمي", "أهم جملة للمبتدئ", null, "عام", 0, "TR"),
            CasualExpressionEntity(3039, "Gerçekten mi?", "غيرتشكتن مي", "حقاً؟", "🔵 يومي", "تعجب", null, "أصدقاء", 0, "TR"),
            CasualExpressionEntity(3040, "Maalesef", "معاليسف", "للأسف", "🟢 رسمي", "رفض مهذب", null, "عام", 0, "TR")
        )
        db.casualDao().insertAll(turkishCasual)

        val turkishScenarios = listOf(
            DailyScenarioEntity(
                3001, "At the Market", "في السوق",
                "Satıcı: Buyurun, hoş geldiniz!\n" +
                    "Müşteri: Merhaba. Bu kaç lira?\n" +
                    "Satıcı: Elli lira.\n" +
                    "Müşteri: Çok pahalı. Biraz indirim yapar mısınız?\n" +
                    "Satıcı: Kırk lira, son fiyat.\n" +
                    "Müşteri: Tamam, bunu alıyorum.",
                "البائع: تفضل، أهلاً وسهلاً!\n" +
                    "الزبون: مرحباً. كم سعر هذا؟\n" +
                    "البائع: خمسون ليرة.\n" +
                    "الزبون: غالٍ جداً. هل تعمل خصماً قليلاً؟\n" +
                    "البائع: أربعون ليرة، آخر سعر.\n" +
                    "الزبون: حسناً، سآخذ هذا.",
                0, "سوق", "TR"
            ),
            DailyScenarioEntity(
                3002, "At the Restaurant", "في المطعم",
                "Garson: Hoş geldiniz. Ne alırsınız?\n" +
                    "Müşteri: Menüye bakabilir miyim?\n" +
                    "Garson: Tabii ki, buyurun.\n" +
                    "Müşteri: Bir çorba ve bir çay lütfen.\n" +
                    "Garson: Afiyet olsun.\n" +
                    "Müşteri: Teşekkür ederim. Hesap lütfen.",
                "النادل: أهلاً وسهلاً. ماذا تطلب؟\n" +
                    "الزبون: هل يمكنني رؤية القائمة؟\n" +
                    "النادل: بالتأكيد، تفضل.\n" +
                    "الزبون: شوربة وشاي من فضلك.\n" +
                    "النادل: بالهناء والشفاء.\n" +
                    "الزبون: شكراً لك. الحساب من فضلك.",
                0, "مطعم", "TR"
            ),
            DailyScenarioEntity(
                3003, "Asking Directions", "السؤال عن الطريق",
                "Ahmet: Pardon, yardım eder misiniz?\n" +
                    "Yerli: Tabii, buyurun.\n" +
                    "Ahmet: Metro istasyonu nerede?\n" +
                    "Yerli: Düz git, sonra sağa dön. Beş dakika.\n" +
                    "Ahmet: Yavaş konuşur musunuz? Anlamadım.\n" +
                    "Yerli: Düz... sonra... sağa. Tamam mı?\n" +
                    "Ahmet: Şimdi anladım. Çok teşekkür ederim!",
                "أحمد: عفواً، هل تساعدني؟\n" +
                    "أحد السكان: بالتأكيد، تفضل.\n" +
                    "أحمد: أين محطة المترو؟\n" +
                    "أحد السكان: امشِ مستقيماً، ثم انعطف يميناً. خمس دقائق.\n" +
                    "أحمد: هل تتكلم ببطء؟ لم أفهم.\n" +
                    "أحد السكان: مستقيم... ثم... يمين. حسناً؟\n" +
                    "أحمد: فهمت الآن. شكراً جزيلاً!",
                0, "شارع", "TR"
            ),
            DailyScenarioEntity(
                3004, "Meeting Someone", "التعارف",
                "Zeynep: Merhaba! Ben Zeynep. Siz?\n" +
                    "Ahmet: Merhaba, benim adım Ahmet. Memnun oldum.\n" +
                    "Zeynep: Ben de memnun oldum. Nerelisiniz?\n" +
                    "Ahmet: Yemenliyim. Ama şimdi İstanbul'da yaşıyorum.\n" +
                    "Zeynep: Türkçeniz çok güzel!\n" +
                    "Ahmet: Sağ olun, hâlâ öğreniyorum.",
                "زينب: مرحباً! أنا زينب. وأنت؟\n" +
                    "أحمد: مرحباً، اسمي أحمد. تشرفنا.\n" +
                    "زينب: تشرفنا أيضاً. من أين أنت؟\n" +
                    "أحمد: أنا يمني. لكنني أعيش الآن في إسطنبول.\n" +
                    "زينب: تركيتك جميلة جداً!\n" +
                    "أحمد: شكراً لك، ما زلت أتعلم.",
                0, "تعارف", "TR"
            )
        )
        db.casualDao().insertScenarios(turkishScenarios)

        val turkishTraining = listOf(
            // التوافق الصوتي — حجر الأساس في التركية
            TrainingItemEntity(3001, "MULTIPLE_CHOICE", "جمع kitap (كتاب) هو:", "kitaplar",
                "kitapler,kitaplar,kitablar,kitaplari",
                "a حرف صوتي ثقيل ⇒ لاحقة الجمع -lar", "قواعد", "TR"),
            TrainingItemEntity(3002, "MULTIPLE_CHOICE", "جمع ev (بيت) هو:", "evler",
                "evlar,evler,evleri,evlerler",
                "e حرف صوتي خفيف ⇒ لاحقة الجمع -ler", "قواعد", "TR"),
            TrainingItemEntity(3003, "MULTIPLE_CHOICE", "ترتيب الجملة التركية هو:", "فاعل + مفعول + فعل",
                "فاعل + فعل + مفعول,فاعل + مفعول + فعل,فعل + فاعل + مفعول,مفعول + فعل + فاعل",
                "التركية SOV — الفعل في نهاية الجملة دائماً، بعكس الإندونيسية", "قواعد", "TR"),
            TrainingItemEntity(3004, "TRANSLATE", "أنا أذهب إلى المدرسة", "Okula gidiyorum", "",
                "okul + a (إلى) + gid + iyor (حاضر مستمر) + um (أنا)", "قواعد", "TR"),
            TrainingItemEntity(3005, "MULTIPLE_CHOICE", "'في البيت' بالتركية:", "evde",
                "eve,evde,evden,evi",
                "-de/-da = في (الإقامة) • -e/-a = إلى • -den/-dan = من", "قواعد", "TR"),
            TrainingItemEntity(3006, "MULTIPLE_CHOICE", "نفي geliyorum (أنا آتٍ):", "gelmiyorum",
                "gelmiyorum,gelmeyorum,gelmiyor,gelmezim",
                "gel + mi (نفي) + yor (زمن) + um (فاعل)", "قواعد", "TR"),
            TrainingItemEntity(3007, "ORDER_WORDS", "Ben / okuyorum / kitap", "Ben kitap okuyorum", "",
                "الفعل يأتي أخيراً في التركية (SOV)", "قواعد", "TR"),
            TrainingItemEntity(3008, "MULTIPLE_CHOICE", "'بيتي' بالتركية:", "benim evim",
                "benim ev,benim evim,ev benim,evim benim",
                "الملكية تحتاج طرفين: benim (صفة ملكية) + ev-im (لاحقة ملكية)", "قواعد", "TR"),

            // اللغة اليومية العملية
            TrainingItemEntity(3009, "SITUATION", "أنت في السوق وتريد معرفة السعر", "Bu ne kadar?",
                "Bu ne kadar?,Nasılsın?,Hesap lütfen,Görüşürüz",
                "Bu ne kadar? = كم سعر هذا؟", "سوق", "TR"),
            TrainingItemEntity(3010, "SITUATION", "لم تفهم ما قاله المتحدث — ماذا تقول؟", "Anlamadım",
                "Anlamadım,Teşekkür ederim,Afiyet olsun,Hoş geldiniz",
                "Anlamadım = لم أفهم. من أهم الجمل للمبتدئ", "عام", "TR"),
            TrainingItemEntity(3011, "SITUATION", "تريد من المتحدث أن يبطئ", "Yavaş konuşur musunuz?",
                "Yavaş konuşur musunuz?,Nerede?,Sağ ol,Olur",
                "أهم جملة عملية في أول شهر من التعلم", "عام", "TR"),
            TrainingItemEntity(3012, "TRANSLATE", "الحساب من فضلك", "Hesap lütfen", "",
                "تُستخدم في كل المطاعم والمقاهي", "مطعم", "TR"),
            TrainingItemEntity(3013, "MULTIPLE_CHOICE", "الرد الثابت على 'Hoş geldiniz':", "Hoş bulduk",
                "Teşekkürler,Hoş bulduk,Merhaba,Görüşürüz",
                "زوج ثابت في الثقافة التركية — لا يُترجم حرفياً", "تحيات", "TR"),
            TrainingItemEntity(3014, "MULTIPLE_CHOICE", "'Naber?' مناسبة مع:", "صديق مقرب",
                "مديرك في العمل,شخص كبير في السن,صديق مقرب,موظف رسمي",
                "Naber? عامية جداً — استخدم Nasılsınız? في المواقف الرسمية", "أصدقاء", "TR"),
            TrainingItemEntity(3015, "TRANSLATE", "كيف أذهب؟", "Nasıl gidebilirim?", "",
                "جملة أساسية للسفر والتنقل", "شارع", "TR"),
            TrainingItemEntity(3016, "MULTIPLE_CHOICE", "'أنا طالب' بالتركية:", "Ben öğrenciyim",
                "Ben öğrenci,Ben öğrenciyim,Ben öğrencisin,Öğrenci ben",
                "لا يوجد فعل 'يكون' منفصل — يُلحق باللاحقة -yim", "قواعد", "TR")
        )
        db.trainingDao().insertAll(turkishTraining)
    }
}