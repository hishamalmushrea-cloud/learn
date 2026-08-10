package com.indolearn.data.repository

import com.indolearn.data.local.AppDatabase
import com.indolearn.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class LearnRepository(private val db: AppDatabase) {

    // Lessons
    fun getLessons(level: Int) = db.lessonDao().getLessonsByLevel(level)
    suspend fun markLessonCompleted(id: Int) = db.lessonDao().markCompleted(id)

    // Vocabulary
    fun getAllVocabulary() = db.vocabularyDao().getAllVocabulary()
    fun getFavorites() = db.vocabularyDao().getFavorites()
    suspend fun toggleFavorite(id: Int, fav: Boolean) = db.vocabularyDao().toggleFavorite(id, fav)

    // Grammar
    fun getGrammar(level: Int) = db.grammarDao().getGrammarByLevel(level)

    // Progress
    fun getUserProgress() = db.progressDao().getProgress()
    suspend fun updateProgress(progress: UserProgressEntity) = db.progressDao().updateProgress(progress)

    // Lesson details
    suspend fun getLessonById(id: Int) = db.lessonDao().getLessonById(id)
    suspend fun getLessonDetail(id: Int) = db.lessonDetailDao().getLessonDetail(id)

    // Quizzes
    suspend fun getRandomQuizzes(limit: Int) = db.trainingDao().getRandomQuizzes(limit)

    // Seed initial data (called once)
    suspend fun seedInitialData() {
        // Level 0 Lessons
        val lessons = listOf(
            LessonEntity(1, 0, "التحيات", "Salam", "تعلم التحيات الأساسية", "content1", false),
            LessonEntity(2, 0, "التعارف", "Perkenalan", "تقديم النفس", "content2", false),
            LessonEntity(3, 0, "الأرقام 1-10", "Angka 1-10", "تعلم الأرقام", "content3", false),
            LessonEntity(4, 0, "الأيام", "Hari", "أيام الأسبوع", "content4", false),
            LessonEntity(5, 0, "الضمائر", "Kata Ganti", "أنا، أنت، هو...", "content5", false),
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
            TrainingItemEntity(115, "MULTIPLE_CHOICE", "Bisa kurang? = ?", "هل يمكن تخفيض السعر؟", "كم السعر؟,هل يمكن تخفيض؟,هل تريد؟,رخيص", "Bisa kurang? = Can it be cheaper?", "سوق"),
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
            VocabularyEntity(100, "halo", "halo", "ها لو", "مرحبا", "Halo, apa kabar?", "مرحبا، كيف حالك؟", "تحيات", 0, true),
            VocabularyEntity(101, "selamat pagi", "selamat pagi", "سلا مات با جي", "صباح الخير", "Selamat pagi, Pak.", "صباح الخير يا سيدي.", "تحيات", 0, true),
            VocabularyEntity(102, "saya", "saya", "سايا", "أنا", "Saya dari Yaman.", "أنا من اليمن.", "ضمائر", 0, true),
            VocabularyEntity(103, "aku", "aku", "أكو", "أنا (يومي)", "Aku lapar.", "أنا جوعان.", "ضمائر", 0, false),
            VocabularyEntity(104, "kamu", "kamu", "كامو", "أنت", "Kamu dari mana?", "من أين أنت؟", "ضمائر", 0, true),
            VocabularyEntity(105, "makan", "makan", "ماكان", "يأكل", "Saya makan nasi.", "أنا آكل الأرز.", "أفعال", 0, true),
            VocabularyEntity(106, "minum", "minum", "مينوم", "يشرب", "Saya minum air.", "أنا أشرب الماء.", "أفعال", 0, true),
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
            VocabularyEntity(119, "tidur", "tidur", "تيدور", "ينام", "Saya mau tidur.", "أريد أن أنام.", "أفعال", 0, true),
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
            VocabularyEntity(139, "makan", "makan", "ماكان", "يأكل", "Saya mau makan.", "أريد أن أأكل.", "أفعال", 0, true),
            VocabularyEntity(140, "minum", "minum", "مينوم", "يشرب", "Saya minum teh.", "أنا أشرب الشاي.", "أفعال", 0, true),
            VocabularyEntity(141, "pergi", "pergi", "بيرجي", "يذهب", "Saya pergi ke pasar.", "أنا أذهب إلى السوق.", "أفعال", 0, true),
            VocabularyEntity(142, "pulang", "pulang", "بولانج", "يعود", "Saya pulang jam enam.", "أعود في الساعة السادسة.", "أفعال", 0, true),
            VocabularyEntity(143, "tidur", "tidur", "تيدور", "ينام", "Saya mau tidur.", "أريد أن أنام.", "أفعال", 0, true),
            VocabularyEntity(144, "kerja", "kerja", "كيرجا", "يعمل", "Saya kerja di kantor.", "أعمل في المكتب.", "أفعال", 0, true),
            VocabularyEntity(145, "belajar", "belajar", "بيلاجار", "يتعلم", "Saya belajar bahasa Indonesia.", "أنا أتعلم الإندونيسية.", "أفعال", 0, true),
            VocabularyEntity(146, "tahu", "tahu", "تاهو", "يعرف", "Saya tidak tahu.", "أنا لا أعرف.", "أفعال", 0, true),
            VocabularyEntity(147, "suka", "suka", "سوكا", "يحب", "Saya suka nasi goreng.", "أنا أحب الناسي غورينغ.", "أفعال", 0, true),
            VocabularyEntity(148, "bisa", "bisa", "بيسا", "يستطيع", "Saya bisa bahasa Arab.", "أستطيع التحدث بالعربية.", "أفعال", 0, true),
            VocabularyEntity(149, "rumah", "rumah", "روماه", "بيت", "Rumah saya besar.", "بيتي كبير.", "أماكن", 0, true),
            VocabularyEntity(150, "pasar", "pasar", "باسار", "سوق", "Saya pergi ke pasar.", "أنا أذهب إلى السوق.", "أماكن", 0, true),
            VocabularyEntity(151, "kantor", "kantor", "كانتور", "مكتب", "Dia kerja di kantor.", "هو يعمل في المكتب.", "أماكن", 0, true),
            VocabularyEntity(152, "sekolah", "sekolah", "سكولاه", "مدرسة", "Anak saya di sekolah.", "ابني في المدرسة.", "أماكن", 0, true),
            VocabularyEntity(153, "nasi", "nasi", "ناسي", "أرز", "Saya makan nasi.", "أنا آكل الأرز.", "طعام", 0, true),
            VocabularyEntity(154, "air", "air", "آير", "ماء", "Saya minum air.", "أنا أشرب الماء.", "طعام", 0, true),
            VocabularyEntity(155, "kopi", "kopi", "كوبي", "قهوة", "Saya suka kopi.", "أنا أحب القهوة.", "طعام", 0, true),
            VocabularyEntity(156, "teh", "teh", "تيه", "شاي", "Saya minum teh.", "أنا أشرب الشاي.", "طعام", 0, true),
            VocabularyEntity(157, "besar", "besar", "بسار", "كبير", "Rumah ini besar.", "هذا البيت كبير.", "صفات", 0, true),
            VocabularyEntity(158, "kecil", "kecil", "كيتشيل", "صغير", "Buku kecil.", "كتاب صغير.", "صفات", 0, true),
            VocabularyEntity(159, "baru", "baru", "بارو", "جديد", "Telepon baru.", "هاتف جديد.", "صفات", 0, true),
            VocabularyEntity(160, "lama", "lama", "لاما", "قديم", "Buku lama.", "كتاب قديم.", "صفات", 0, true),
            VocabularyEntity(161, "murah", "murah", "موراه", "رخيص", "Ini murah.", "هذا رخيص.", "صفات", 0, true),
            VocabularyEntity(162, "mahal", "mahal", "ماهال", "غالي", "Harganya mahal.", "سعره غالي.", "صفات", 0, true),
            VocabularyEntity(163, "cepat", "cepat", "تشيبات", "سريع", "Dia cepat.", "هو سريع.", "صفات", 0, true),
            VocabularyEntity(164, "lambat", "lambat", "لامبات", "بطيء", "Dia lambat.", "هو بطيء.", "صفات", 0, true),
        )
        db.vocabularyDao().insertAll(vocabStage1)
    }
}