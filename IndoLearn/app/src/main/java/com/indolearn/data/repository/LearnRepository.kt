package com.indolearn.data.repository

import com.indolearn.data.local.AppDatabase
import com.indolearn.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LearnRepository(private val db: AppDatabase) {

    // Lessons
    fun getLessons(level: Int) = db.lessonDao().getLessonsByLevel(level)
    suspend fun getLessonById(id: Int) = db.lessonDao().getLessonById(id)
    suspend fun getLessonDetail(id: Int) = db.lessonDetailDao().getLessonDetail(id)
    suspend fun markLessonCompleted(id: Int) = db.lessonDao().markCompleted(id)

    // Quizzes
    suspend fun getRandomQuizzes(limit: Int) = db.trainingDao().getRandomQuizzes(limit)

    // Vocabulary
    fun getAllVocabulary() = db.vocabularyDao().getAllVocabulary()
    fun getFavorites() = db.vocabularyDao().getFavorites()
    suspend fun toggleFavorite(id: Int, fav: Boolean) = db.vocabularyDao().toggleFavorite(id, fav)

    // Grammar
    fun getGrammar(level: Int) = db.grammarDao().getGrammarByLevel(level)

    // Progress
    fun getUserProgress() = db.progressDao().getProgress()
    suspend fun updateProgress(progress: UserProgressEntity) = db.progressDao().updateProgress(progress)

    // Seed initial data (called once)
    suspend fun seedInitialData() {
        val existingLessons = db.lessonDao().getLessonsByLevel(0).first()
        if (existingLessons.isNotEmpty()) return

        // === STAGE 0 LESSONS (ABSOLUTE BEGINNER) ===
        val lessons = listOf(
            LessonEntity(1, 0, "التحيات 1", "Salam 1", "كيف تقول مرحباً وصباح الخير", "content1", false),
            LessonEntity(2, 0, "التحيات 2 والتعارف", "Salam 2 & Perkenalan", "كيف حالك وما اسمك؟", "content2", false),
            LessonEntity(3, 0, "الضمائر (أنا وأنت)", "Kata Ganti", "saya, kamu, dia", "content3", false),
            LessonEntity(4, 0, "الأفعال الأساسية 1", "Kata Kerja Dasar 1", "makan, minum, mau", "content4", false),
            LessonEntity(5, 0, "تكوين أول جملة", "Kalimat Pertama", "أنا أريد أن آكل", "content5", false),
            LessonEntity(6, 0, "النفي (لا)", "Negasi (Tidak)", "كيف تقول لا (tidak, bukan)", "content6", false),
            LessonEntity(7, 0, "الأسئلة الأساسية", "Pertanyaan Dasar", "ماذا؟ ومن؟", "content7", false),
            LessonEntity(8, 0, "الصفات والألوان", "Kata Sifat & Warna", "كبير، صغير، أحمر، أزرق", "content8", false),
            LessonEntity(9, 0, "الأرقام 1 - 10", "Angka 1-10", "عد من 1 إلى 10", "content9", false),
            LessonEntity(10, 0, "الأرقام الكبيرة والأسعار", "Angka Besar & Harga", "عشرة، مئة، ألف", "content10", false),
            LessonEntity(11, 0, "السؤال عن السعر", "Tanya Harga", "كم سعر هذا؟", "content11", false),
            LessonEntity(12, 0, "العائلة", "Keluarga", "أب، أم، أخ، أخت", "content12", false),
            LessonEntity(13, 0, "الأيام والأوقات", "Hari & Waktu", "اليوم، غداً، الأحد، الإثنين", "content13", false),
            LessonEntity(14, 0, "مراجعة شاملة", "Review Total", "مراجعة كل ما سبق", "content14", false),
        )
        db.lessonDao().insertAll(lessons)

        // === STAGE 0 LESSON DETAILS (ABSOLUTE BEGINNER) ===
        val lessonDetails = listOf(
            LessonDetailEntity(1, 1, "ستتعلم اليوم كيف تلقي التحية بالإندونيسية.", 
                "Halo = مرحباً\nSelamat pagi = صباح الخير", 
                "تستخدم 'Selamat pagi' من الفجر حتى الساعة 10 صباحاً.", "هذه التحيات رسمية ومهذبة، يمكنك استخدام Halo دائماً.", "لا تستخدم Selamat pagi في المساء.", "Formal: Selamat pagi • Casual: Halo"),
            
            LessonDetailEntity(2, 2, "كيف تسأل شخصاً عن حاله واسمه.", 
                "Apa kabar? = كيف حالك؟\nSiapa nama kamu? = ما اسمك؟\nBaik = بخير", 
                "Apa + kabar? = ما + خبر؟", "Siapa تستخدم للأشخاص فقط.", "Apa kabar تستخدم يومياً.", "Formal: Siapa nama Anda? • Casual: Siapa namamu?"),
            
            LessonDetailEntity(3, 3, "الضمائر هي أساس التحدث عن نفسك والآخرين.", 
                "Saya = أنا\nKamu = أنت\nDia = هو / هي", 
                "لا يوجد تذكير وتأنيث في الإندونيسية. (Dia = هو أو هي).", "استخدم Saya دائماً فهي آمنة ومهذبة.", "استخدام Aku بدلاً من Saya شائع لكنه غير رسمي.", "Formal: Saya, Anda • Casual: Aku, Kamu"),
            
            LessonDetailEntity(4, 4, "تعلم أهم 3 أفعال في الإندونيسية.", 
                "Mau = يريد\nMakan = يأكل\nMinum = يشرب", 
                "الأفعال لا تتغير مع الزمن أو الضمير! (Saya makan = أنا آكل / Dia makan = هو يأكل).", "استخدم Mau كثيراً للتعبير عن رغبتك.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(5, 5, "اليوم سنركب أول جملة كاملة من كلمتين وثلاث كلمات.", 
                "Saya mau. = أنا أريد.\nSaya mau makan. = أنا أريد أن آكل.\nSaya makan nasi. = أنا آكل الأرز.", 
                "فاعل + فعل + مفعول\nSaya + makan + nasi", "ترتيب الجملة مثل العربية تماماً (مبتدأ وخبر/فعل ومفعول). لا توجد تعقيدات.", "لا تضف أدوات ربط، فقط ضع الكلمات بجانب بعضها.", "Formal: Saya mau makan • Casual: Aku mau makan"),
            
            LessonDetailEntity(6, 6, "الفرق بين (Tidak) و (Bukan). كلاهما يعني 'لا'.", 
                "Tidak = لا (تستخدم قبل الأفعال والصفات)\nBukan = ليس (تستخدم قبل الأسماء)", 
                "Saya tidak makan = أنا لا آكل.\nIni bukan buku = هذا ليس كتاباً.", "استخدم Tidak مع الأفعال (لا أريد، لا أحب).\nاستخدم Bukan مع الأسماء (ليس بيتي، ليس محمد).", "الخلط بين Tidak و Bukan خطأ شائع للمبتدئين.", "Casual: Nggak بدلاً من Tidak"),
            
            LessonDetailEntity(7, 7, "أهم كلمتين للسؤال.", 
                "Apa = ماذا / هل\nSiapa = من (للأشخاص)", 
                "Apa ini? = ماذا هذا؟\nSiapa dia? = من هو؟", "استخدم Apa لغير العاقل، و Siapa للعاقل.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(8, 8, "الصفات الأساسية وكيفية استخدامها مع الألوان.", 
                "Besar = كبير\nKecil = صغير\nMerah = أحمر\nHitam = أسود", 
                "Buku merah = كتاب أحمر\nRumah besar = بيت كبير", "الصفة تأتي دائماً (بعد) الاسم! تماماً مثل اللغة العربية.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(9, 9, "الأرقام الأساسية. احفظها جيداً لأنك ستحتاجها في كل مكان.", 
                "Satu = 1, Dua = 2, Tiga = 3, Empat = 4, Lima = 5\nEnam = 6, Tujuh = 7, Delapan = 8, Sembilan = 9, Sepuluh = 10", 
                "مجرد حفظ المفردات.", "كررها كثيراً حتى تحفظها عن ظهر قلب.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(10, 10, "الأرقام الأكبر للتعامل مع النقود.", 
                "Belas = عشر (مثل 11، 12)\nPuluh = عشرون، ثلاثون\nRatus = مئة\nRibu = ألف", 
                "Sebelas = 11, Dua belas = 12\nDua puluh = 20\nSeratus = 100\nSepuluh ribu = 10,000", "العملة الإندونيسية أرقامها كبيرة (10,000 روبية = أقل من دولار)، لذلك (Ribu - ألف) كلمة أساسية.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(11, 11, "كيف تسأل عن السعر في السوق أو المتجر.", 
                "Berapa? = كم؟\nHarga = سعر\nBerapa harganya? = كم سعره؟\nIni berapa? = هذا بكم؟", 
                "Ini berapa? = هذا بكم؟\nItu berapa? = ذاك بكم؟", "استخدم 'Ini berapa' مع الإشارة بإصبعك، سيفهمك الجميع.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(12, 12, "أفراد العائلة الأساسيين.", 
                "Ayah / Bapak = أب\nIbu = أم\nAnak = طفل / ابن\nKakak = أخ أو أخت أكبر\nAdik = أخ أو أخت أصغر", 
                "Ini ayah saya = هذا أبي.", "في إندونيسيا، يُستخدم (Bapak) و (Ibu) لاحترام كبار السن حتى لو لم يكونوا والديك.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(13, 13, "مفردات الوقت الأساسية.", 
                "Hari ini = اليوم\nBesok = غداً\nKemarin = أمس", 
                "Saya pergi besok = سأذهب غداً.", "أيام الأسبوع مأخوذة من العربية: Ahad, Senin, Selasa, Rabu, Kamis, Jumat, Sabtu.", "", "Formal & Casual: sama"),
            
            LessonDetailEntity(14, 14, "اختبار شامل لكل ما تعلمته في المستويات الـ 13 السابقة.", 
                "مراجعة: التحيات، الضمائر، الأفعال، الألوان، الأرقام، العائلة.", 
                "تدرب كثيراً في قسم 'الاختبارات' (Quizzes) لترسيخ هذه الكلمات.", "التكرار هو سر النجاح في اللغة.", "", "Review Stage")
        )
        db.lessonDetailDao().insertAll(lessonDetails)

        // === GRAMMAR ===
        val grammar = listOf(
            GrammarEntity(1, "لا يوجد فعل (To Be)", "Tanpa To Be", "في اللغة الإندونيسية، لا تحتاج لربط المبتدأ بالخبر بفعل مثل (is, am, are). تضع الكلمات بجوار بعضها.", "Saya + (لا شيء) + lapar = أنا جوعان", "Saya lapar. (أنا جوعان)\nDia guru. (هو معلم)\nIni buku. (هذا كتاب)", 0),
            GrammarEntity(2, "الصفة بعد الموصوف", "Kata Sifat", "كما في اللغة العربية، الصفة تأتي بعد الشيء الموصوف، وليس قبله كما في الإنجليزية.", "Buku (كتاب) + merah (أحمر) = كتاب أحمر", "Buku merah. (كتاب أحمر)\nRumah besar. (بيت كبير)", 0),
            GrammarEntity(3, "لا يوجد جمع معقد", "Jamak", "لجمع شيء، إما أن تكرر الكلمة مرتين، أو تضع قبلها رقماً.", "Orang-orang = أشخاص / Dua orang = شخصان", "Buku-buku (كتب)\nTiga buku (ثلاثة كتب)", 0)
        )
        db.grammarDao().insertAll(grammar)

        // === CASUAL & SCENARIOS (SIMPLE A1) ===
        val casual = listOf(
            CasualExpressionEntity(1, "Makasih", "ماكاسي", "شكراً", "🔵 يومي", "تقال دائماً", "Terima kasih", "عام", 0),
            CasualExpressionEntity(2, "Sama-sama", "ساما-ساما", "عفواً", "🔵 يومي", "الرد على شكراً", null, "عام", 0),
            CasualExpressionEntity(3, "Permisi", "بيرميسي", "عن إذنك / لو سمحت", "🔵 يومي", "عند المرور بين الناس", null, "عام", 0),
            CasualExpressionEntity(4, "Maaf", "ماآف", "آسف", "🔵 يومي", "للاعتذار", null, "عام", 0),
            CasualExpressionEntity(5, "Nggak apa-apa", "نجاك أبا-أبا", "لا بأس / لا مشكلة", "🔵 يومي", "الرد على الاعتذار", null, "عام", 0),
            CasualExpressionEntity(6, "Iya", "إيا", "نعم", "🔵 يومي", "الموافقة", null, "عام", 0),
            CasualExpressionEntity(7, "Bentar ya", "بينتار يا", "لحظة", "🔵 يومي", "طلب الانتظار", null, "عام", 0),
            CasualExpressionEntity(8, "Boleh", "بوليه", "ممكن / مسموح", "🔵 يومي", "السماح", null, "عام", 0),
            CasualExpressionEntity(9, "Ayo", "آيو", "هيا", "🔵 يومي", "للدعوة للذهاب", null, "عام", 0),
            CasualExpressionEntity(10, "Berapa?", "بيرابا؟", "كم؟", "🔵 يومي", "سؤال عن الكمية أو السعر", null, "سوق", 0),
            
            // 🛒 في السوق (Belanja / Pasar)
            CasualExpressionEntity(11, "Ke sini dong!", "كي سيني دونج!", "تعال هنا!", "🟠 عامي", "للمناداة في السوق أو الشارع", "Ke mari", "سوق", 0),
            CasualExpressionEntity(12, "Lihat-lihat dulu, gratis kok.", "ليهات-ليهات دولو، جراتيس كوك.", "تفقد/شاهد أولاً، مجاني عادي 😄", "🟡 غير رسمي", "يستخدمها البائع لجذب الزبائن", null, "سوق", 0),
            CasualExpressionEntity(13, "Murah banget ini!", "موراه بانجيت إيني!", "هذا رخيص جدًا!", "🟡 غير رسمي", "لإقناع المشتري بالسعر", "Ini sangat murah", "سوق", 0),
            CasualExpressionEntity(14, "Mau apa?", "ماو أبا؟", "ماذا تريد؟", "🔵 يومي", "لسؤال الزبون أو الصديق", "Ingin apa?", "سوق", 0),
            CasualExpressionEntity(15, "Ambil aja, santai.", "أمبيل أجا، سانتاي.", "خذه عادي / بدون توتر", "🟠 عامي", "لإشعار الشخص بالراحة", "Silakan ambil", "سوق", 0),
            CasualExpressionEntity(16, "Hari ini diskon besar!", "هاري إيني ديسكون بيسار!", "اليوم تخفيضات كبيرة!", "🔵 يومي", "لجذب الانتباه في السوق", null, "سوق", 0),
            CasualExpressionEntity(17, "Jangan mahal-mahal dong!", "جانجان ماھال-ماھال دونج!", "لا ترفع السعر كثير 😄", "🟠 عامي", "عند المماكسة في السعر", "Jangan terlalu mahal", "سوق", 0),

            // 🏠 في الشارع / تعامل يومي
            CasualExpressionEntity(18, "Lagi apa kamu?", "لاجي أبا كامو؟", "ماذا تفعل؟", "🔵 يومي", "سؤال عن الحال أو الفعل", "Sedang apa kamu?", "شارع", 0),
            CasualExpressionEntity(19, "Mau ke mana?", "ماو كي مانا؟", "إلى أين ذاهب؟", "🔵 يومي", "سؤال معتاد عند رؤية شخص", null, "شارع", 0),
            CasualExpressionEntity(20, "Santai aja bro/sis.", "سانتاي أجا برو/سيس.", "خذها بسهولة يا صديقي", "🟠 عامي", "لتهدئة شخص", "Tenang saja", "شارع", 0),
            CasualExpressionEntity(21, "Nanti saja, jangan buru-buru.", "نانتي ساجا، جانجان بورو-بورو.", "لاحقًا، لا تستعجل", "🔵 يومي", "للتأجيل دون استعجال", null, "شارع", 0),
            CasualExpressionEntity(22, "Ayo jalan!", "آيو جالان!", "هيا نذهب!", "🔵 يومي", "للانطلاق أو بدء المشي", "Mari pergi", "شارع", 0),
            CasualExpressionEntity(23, "Tunggu sebentar.", "تونجو سيبينتار.", "انتظر قليلًا", "🔵 يومي", "لطلب الانتظار", null, "شارع", 0),

            // 😄 مزاح خفيف بين الأصدقاء
            CasualExpressionEntity(24, "Kamu pelit banget ya!", "كامو بيليت بانجيت يا!", "أنت بخيل جدًا 😄", "🟠 عامي", "مزاح بين الأصدقاء المقربين", "Kamu sangat pelit", "أصدقاء", 0),
            CasualExpressionEntity(25, "Ah, kamu lebay!", "آه، كامو ليباي!", "أنت مبالغ فيه!", "🟠 عامي", "عندما يبالغ شخص في ردة فعله", "Kamu berlebihan", "أصدقاء", 0),
            CasualExpressionEntity(26, "Jangan bohong ah!", "جانجان بوهونج آه!", "لا تكذب!", "🟠 عامي", "عند الشك في كلام الصديق", "Jangan berbohong", "أصدقاء", 0),
            CasualExpressionEntity(27, "Kamu sok kaya ya?", "كامو سوك كايا يا؟", "تتظاهر أنك غني؟ 😄", "🟠 عامي", "مزاح عند رؤية صديق يصرف كثيراً", null, "أصدقاء", 0),
            CasualExpressionEntity(28, "Dasar malas!", "داسار مالاس!", "كسول فعلًا!", "🟠 عامي", "تقال بمزاح لمن لا يريد فعل شيء", "Kamu pemalas", "أصدقاء", 0),
            CasualExpressionEntity(29, "Santai, jangan baper.", "سانتاي، جانجان بابير.", "عادي، لا تزعل بسرعة", "🟠 عامي", "لتهدئة شخص حساس للمزاح", "Jangan bawa perasaan", "أصدقاء", 0),

            // 💬 تعبيرات ودّية
            CasualExpressionEntity(30, "Makasih ya!", "ماكاسي يا!", "شكرًا!", "🔵 يومي", "شكر ودي وعفوي", "Terima kasih", "عام", 0),
            CasualExpressionEntity(31, "Silakan.", "سيلاكان.", "تفضل", "🔵 يومي", "دعوة للدخول أو الجلوس أو أخذ شيء", null, "عام", 0),
            CasualExpressionEntity(32, "Gampang kok.", "جامبانج كوك.", "سهل عادي", "🟡 غير رسمي", "للتطمين بأن الأمر بسيط", "Mudah kok", "عام", 0),
            CasualExpressionEntity(33, "Tenang aja.", "تينانج أجا.", "لا تقلق", "🔵 يومي", "لبعث الطمأنينة", "Tenang saja", "عام", 0),

            // 🔥 جمل “حياة يومية مختلطة” (سوق + مزاح)
            CasualExpressionEntity(34, "Murah banget, kamu masih mikir?", "موراه بانجيت، كامو ماسيه ميكير؟", "رخيص جدًا، ما زلت تفكر؟", "🟠 عامي", "لإقناع الصديق بالشراء بسرعة", null, "سوق", 0),
            CasualExpressionEntity(35, "Ambil dua aja, biar kamu hemat… atau pelit 😄", "أمبيل دوا أجا، بيار كامو هيمات... أتاو بيليت 😄", "خذ اثنين عشان توفر… أو لأنك بخيل 😄", "🟠 عامي", "مزاح أثناء التسوق", null, "سوق", 0),
            CasualExpressionEntity(36, "Ke sini, aku kasih harga teman!", "كي سيني، أكو كاسيه هارجا تيمان!", "تعال، أعطيك سعر الصديق!", "🟠 عامي", "بائع ودود يعطي خصماً", "Ke sini, saya beri harga teman", "سوق", 0),
            CasualExpressionEntity(37, "Jangan banyak tanya, langsung ambil!", "جانجان بانياك تانيا، لانجسونج أمبيل!", "لا تسأل كثير، خذ مباشرة!", "🟠 عامي", "مزاح مع شخص متردد", null, "سوق", 0),
            CasualExpressionEntity(38, "Wah kamu negosiasi kayak pro!", "واه كامو نيجوسياسي كاياك برو!", "تفاوضك مثل المحترفين!", "🟠 عامي", "مدح لمهارة المماكسة", "Kamu bernegosiasi seperti ahli", "سوق", 0)
        )
        db.casualDao().insertAll(casual)

        val scenarios = listOf(
            DailyScenarioEntity(1, "التعارف الأول", "Perkenalan", "A: Halo, siapa nama kamu?\nB: Halo, nama saya Ahmad.\nA: Apa kabar Ahmad?\nB: Baik, terima kasih.", "أ: مرحباً، ما اسمك؟\nب: مرحباً، اسمي أحمد.\nأ: كيف حالك أحمد؟\nب: بخير، شكراً.", 0, "عام"),
            DailyScenarioEntity(2, "في البقالة", "Di Toko", "A: Permisi, ini berapa?\nB: Itu sepuluh ribu (10,000).\nA: Saya mau beli ini.\nB: Baik, terima kasih.", "أ: لو سمحت، هذا بكم؟\nب: ذاك بعشرة آلاف.\nأ: أريد أن أشتري هذا.\nب: حسناً، شكراً.", 0, "سوق")
        )
        db.casualDao().insertScenarios(scenarios)

        // === QUIZZES (MASSIVE A1 TRAINING) ===
        val training = listOf(
            // Translation
            TrainingItemEntity(1, "TRANSLATE", "أنا أريد أن آكل", "Saya mau makan.", "", "saya (أنا) + mau (أريد) + makan (يأكل)", "تكوين الجمل"),
            TrainingItemEntity(2, "TRANSLATE", "هذا كتاب أحمر", "Ini buku merah.", "", "ini (هذا) + buku (كتاب) + merah (أحمر). الصفة بعد الموصوف.", "تكوين الجمل"),
            TrainingItemEntity(3, "TRANSLATE", "كم سعر هذا؟", "Ini berapa?", "", "ini (هذا) + berapa (كم)", "سوق"),
            TrainingItemEntity(4, "TRANSLATE", "أنا لا أريد", "Saya tidak mau.", "", "tidak (لا) تنفي الفعل mau", "نفي"),
            
            // Multiple Choice
            TrainingItemEntity(5, "MULTIPLE_CHOICE", "ما معنى كلمة (Saya)؟", "أنا", "هو,أنت,أنا,نحن", "Saya = أنا", "مفردات"),
            TrainingItemEntity(6, "MULTIPLE_CHOICE", "أي جملة هي الصحيحة لقول (أنا لا آكل)؟", "Saya tidak makan", "Saya bukan makan,Saya makan tidak,Saya tidak makan,Tidak saya makan", "Tidak تستخدم قبل الفعل", "قواعد"),
            TrainingItemEntity(7, "MULTIPLE_CHOICE", "اختر الكلمة الصحيحة: Rumah ___ (بيت كبير)", "besar", "merah,kecil,besar,makan", "besar = كبير", "مفردات"),
            TrainingItemEntity(8, "MULTIPLE_CHOICE", "ما معنى (Berapa)؟", "كم", "ماذا,كم,أين,من", "Berapa تستخدم للكمية والسعر", "مفردات"),
            TrainingItemEntity(9, "MULTIPLE_CHOICE", "كيف تقول (صباح الخير)؟", "Selamat pagi", "Halo,Selamat malam,Selamat pagi,Terima kasih", "Selamat pagi = صباح الخير", "تحيات"),
            
            // Order words
            TrainingItemEntity(10, "ORDER_WORDS", "makan / mau / Saya", "Saya mau makan.", "", "فاعل + يريد + فعل", "ترتيب الكلمات"),
            TrainingItemEntity(11, "ORDER_WORDS", "bukan / Ini / buku / saya", "Ini bukan buku saya.", "", "هذا + ليس (للاسم) + كتابي", "ترتيب الكلمات"),
            TrainingItemEntity(12, "ORDER_WORDS", "merah / mobil / Dia / punya", "Dia punya mobil merah.", "", "punya (يملك) + mobil (سيارة) + merah (حمراء)", "ترتيب الكلمات"),
            
            // Situations
            TrainingItemEntity(13, "SITUATION", "أنت في مطعم وتريد طلب طعام وتقول: أنا جوعان.", "Saya lapar.", "Saya haus.,Saya lapar.,Saya kenyang.,Saya tidur.", "lapar = جوعان", "مواقف"),
            TrainingItemEntity(14, "SITUATION", "شخص يسألك: Apa kabar? ماذا ترد؟", "Baik", "Terima kasih,Halo,Baik,Berapa", "Baik = بخير", "مواقف"),
            TrainingItemEntity(15, "SITUATION", "أعطاك شخص هدية. ماذا تقول له؟", "Terima kasih", "Sama-sama,Maaf,Terima kasih,Halo", "Terima kasih = شكراً", "مواقف")
        )
        db.trainingDao().insertAll(training)

        // === VOCABULARY (A1 TOP 100) ===
        val vocabStage1 = listOf(
            // ضمائر
            VocabularyEntity(100, "saya", "saya", "سايا", "أنا", "Saya dari Yaman.", "أنا من اليمن.", "ضمائر", 0, true),
            VocabularyEntity(101, "kamu", "kamu", "كامو", "أنت", "Kamu siapa?", "من أنت؟", "ضمائر", 0, true),
            VocabularyEntity(102, "dia", "dia", "ديا", "هو / هي", "Dia teman saya.", "هو صديقي.", "ضمائر", 0, true),
            VocabularyEntity(103, "kita", "kita", "كيتا", "نحن (وأنت معنا)", "Kita pergi sekarang.", "سنذهب الآن.", "ضمائر", 0, false),
            VocabularyEntity(104, "mereka", "mereka", "ميريكا", "هم", "Mereka di sana.", "هم هناك.", "ضمائر", 0, false),
            
            // أسئلة
            VocabularyEntity(105, "apa", "apa", "أبا", "ماذا / هل", "Apa ini?", "ما هذا؟", "سؤال", 0, true),
            VocabularyEntity(106, "siapa", "siapa", "سيابا", "من (للعاقل)", "Siapa nama kamu?", "ما اسمك؟", "سؤال", 0, true),
            VocabularyEntity(107, "di mana", "di mana", "دي مانا", "أين", "Di mana rumah kamu?", "أين بيتك؟", "سؤال", 0, true),
            VocabularyEntity(108, "berapa", "berapa", "بيرابا", "كم", "Berapa harganya?", "كم سعره؟", "سؤال", 0, true),
            VocabularyEntity(109, "kapan", "kapan", "كابان", "متى", "Kapan kamu pergi?", "متى ستذهب؟", "سؤال", 0, true),
            
            // أفعال أساسية
            VocabularyEntity(110, "mau", "mau", "ماو", "يريد", "Saya mau makan.", "أريد أن آكل.", "أفعال", 0, true),
            VocabularyEntity(111, "ada", "ada", "آدا", "يوجد / لديه", "Ada air?", "هل يوجد ماء؟", "أفعال", 0, true),
            VocabularyEntity(112, "makan", "makan", "ماكان", "يأكل", "Saya makan nasi.", "أنا آكل الأرز.", "أفعال", 0, true),
            VocabularyEntity(113, "minum", "minum", "مينوم", "يشرب", "Saya minum air.", "أنا أشرب الماء.", "أفعال", 0, true),
            VocabularyEntity(114, "pergi", "pergi", "بيرجي", "يذهب", "Saya pergi ke pasar.", "أنا أذهب إلى السوق.", "أفعال", 0, true),
            VocabularyEntity(115, "beli", "beli", "بيلي", "يشتري", "Saya beli buku.", "أنا أشتري كتاباً.", "أفعال", 0, true),
            VocabularyEntity(116, "lihat", "lihat", "ليهات", "يرى / ينظر", "Lihat ini!", "انظر إلى هذا!", "أفعال", 0, false),
            VocabularyEntity(117, "suka", "suka", "سوكا", "يحب / يعجبه", "Saya suka kopi.", "أنا أحب القهوة.", "أفعال", 0, true),
            VocabularyEntity(118, "tahu", "tahu", "تاهو", "يعرف", "Saya tidak tahu.", "أنا لا أعرف.", "أفعال", 0, true),
            VocabularyEntity(119, "bisa", "bisa", "بيسا", "يستطيع", "Saya bisa.", "أنا أستطيع.", "أفعال", 0, true),
            VocabularyEntity(120, "punya", "punya", "بونيا", "يملك", "Saya punya mobil.", "أنا أملك سيارة.", "أفعال", 0, true),
            VocabularyEntity(121, "tidur", "tidur", "تيدور", "ينام", "Saya mau tidur.", "أريد أن أنام.", "أفعال", 0, true),
            
            // صفات
            VocabularyEntity(122, "bagus", "bagus", "باغوس", "جيد / جميل", "Buku ini bagus.", "هذا الكتاب جيد.", "صفات", 0, true),
            VocabularyEntity(123, "besar", "besar", "بيسار", "كبير", "Rumah besar.", "بيت كبير.", "صفات", 0, true),
            VocabularyEntity(124, "kecil", "kecil", "كيتشيل", "صغير", "Mobil kecil.", "سيارة صغيرة.", "صفات", 0, true),
            VocabularyEntity(125, "mahal", "mahal", "ماھال", "غالي", "Sangat mahal.", "غالي جداً.", "صفات", 0, true),
            VocabularyEntity(126, "murah", "murah", "موراه", "رخيص", "Murah sekali.", "رخيص جداً.", "صفات", 0, true),
            VocabularyEntity(127, "panas", "panas", "باناس", "حار", "Cuaca panas.", "الجو حار.", "صفات", 0, true),
            VocabularyEntity(128, "dingin", "dingin", "دينجين", "بارد", "Air dingin.", "ماء بارد.", "صفات", 0, true),
            VocabularyEntity(129, "lapar", "lapar", "لابار", "جوعان", "Saya lapar.", "أنا جوعان.", "صفات", 0, true),
            VocabularyEntity(130, "haus", "haus", "هاوس", "عطشان", "Saya haus.", "أنا عطشان.", "صفات", 0, true),
            VocabularyEntity(131, "baru", "baru", "بارو", "جديد", "Mobil baru.", "سيارة جديدة.", "صفات", 0, false),
            VocabularyEntity(132, "lama", "lama", "لاما", "قديم / طويل(للوقت)", "Sudah lama.", "منذ وقت طويل.", "صفات", 0, false),
            VocabularyEntity(133, "benar", "benar", "بينار", "صحيح", "Itu benar.", "هذا صحيح.", "صفات", 0, false),
            VocabularyEntity(134, "salah", "salah", "سالاه", "خطأ", "Itu salah.", "هذا خطأ.", "صفات", 0, false),
            
            // أسماء
            VocabularyEntity(135, "air", "air", "آير", "ماء", "Minum air.", "يشرب الماء.", "أسماء", 0, true),
            VocabularyEntity(136, "nasi", "nasi", "ناسي", "أرز", "Makan nasi.", "يأكل الأرز.", "أسماء", 0, true),
            VocabularyEntity(137, "buku", "buku", "بوكو", "كتاب", "Buku saya.", "كتابي.", "أسماء", 0, true),
            VocabularyEntity(138, "uang", "uang", "أوانج", "مال / نقود", "Saya tidak punya uang.", "لا أملك مالاً.", "أسماء", 0, true),
            VocabularyEntity(139, "hari", "hari", "هاري", "يوم", "Hari ini.", "اليوم.", "أوقات", 0, true),
            VocabularyEntity(140, "orang", "orang", "أورانج", "شخص / إنسان", "Dua orang.", "شخصان.", "أسماء", 0, true),
            VocabularyEntity(141, "teman", "teman", "تيمان", "صديق", "Dia teman saya.", "هو صديقي.", "عائلة", 0, true),
            VocabularyEntity(142, "rumah", "rumah", "روماه", "بيت", "Saya di rumah.", "أنا في البيت.", "أماكن", 0, true),
            VocabularyEntity(143, "pasar", "pasar", "باسار", "سوق", "Pergi ke pasar.", "يذهب للسوق.", "أماكن", 0, true),
            VocabularyEntity(144, "jalan", "jalan", "جالان", "شارع / يمشي", "Jalan-jalan.", "يتمشى.", "أماكن", 0, false),
            
            // كلمات ربط وأخرى
            VocabularyEntity(145, "dan", "dan", "دان", "و", "Saya dan kamu.", "أنا وأنت.", "روابط", 0, true),
            VocabularyEntity(146, "tapi", "tapi", "تابي", "لكن", "Murah tapi bagus.", "رخيص لكن جيد.", "روابط", 0, true),
            VocabularyEntity(147, "ini", "ini", "إيني", "هذا", "Ini apa?", "ما هذا؟", "إشارة", 0, true),
            VocabularyEntity(148, "itu", "itu", "إيتو", "ذلك", "Itu mobil saya.", "تلك سيارتي.", "إشارة", 0, true),
            VocabularyEntity(149, "di", "di", "دي", "في / على (للمكان)", "Di rumah.", "في البيت.", "حروف", 0, true),
            VocabularyEntity(150, "ke", "ke", "كي", "إلى (للاتجاه)", "Ke pasar.", "إلى السوق.", "حروف", 0, true),
            VocabularyEntity(151, "dari", "dari", "داري", "من", "Dari mana?", "من أين؟", "حروف", 0, true),
            VocabularyEntity(152, "sangat", "sangat", "سانجات", "جداً", "Sangat bagus.", "جيد جداً.", "تأكيد", 0, true)
        )
        db.vocabularyDao().insertAll(vocabStage1)
    }
}