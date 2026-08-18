package com.indolearn.data.repository

import com.indolearn.data.local.entity.LessonDetailEntity
import com.indolearn.data.local.entity.LessonEntity
import com.indolearn.data.local.entity.StageEntity
import com.indolearn.data.local.entity.TrainingItemEntity
import com.indolearn.data.local.entity.UnitEntity

/**
 * مرحلة A0 فعلية: نطق وقراءة قصيرة، بلا وعود طلاقة وبلا مراحل فارغة.
 *
 * ملاحظات صوتية مقصودة (لا تُكسر):
 * - g = /g/ كما في go الإنجليزية، ليست قافاً.
 * - j = /dʒ/ كما في judge، ليست «جيماً مصرية» كوصف ثابت.
 * - k النهائية ليست همزة دائماً؛ قد تُخفَّف أو تُهمز في بعض الكلمات واللهجات.
 */
object A0PronunciationContent {
    const val LEVEL = 10
    const val EXIT_QUIZ_LESSON_ID = -3
    const val EXIT_CATEGORY = "امتحان A0"
    const val PASS_PERCENT = 80

    val stage = StageEntity(
        0,
        "المرحلة A0 — النطق والقراءة",
        "Tahap A0 - Pelafalan",
        "كتابة لاتينية، صوائت، سواكن أساسية، ثم قراءة موجهة قصيرة. لا جمل سوق ولا قواعد بعد.",
        10,
        true
    )

    val units = listOf(
        UnitEntity(101, 0, "الوحدة 1: الكتابة والنطق", "Tulisan & bunyi", "كيف تُكتب الإندونيسية", false),
        UnitEntity(102, 0, "الوحدة 2: الصوائت", "Vokal", "a i u e o و /ə/", false),
        UnitEntity(103, 0, "الوحدة 3: السواكن الأساسية", "Konsonan dasar", "p b t d k g m n l r s h w y", false),
        UnitEntity(104, 0, "الوحدة 4: c و j و g", "c, j, g", "ثلاثة أصوات يختلط وصفها كثيراً", false),
        UnitEntity(105, 0, "الوحدة 5: ng و ny", "ng & ny", "صوتان أنفيان مستقلان", false),
        UnitEntity(106, 0, "الوحدة 6: sy و kh", "sy & kh", "في كلمات دخيلة غالباً", false),
        UnitEntity(107, 0, "الوحدة 7: k النهائية والنبر", "k akhir & tekanan", "تنوع إقليمي بلا تعميم", false),
        UnitEntity(108, 0, "الوحدة 8: قراءة موجهة", "Membaca terarah", "نصوص قصيرة جداً", false),
        UnitEntity(109, 0, "الوحدة 9: اختبار الانتقال", "Ujian A0", "نجاح 80٪ للانتقال بثقة", false)
    )

    val lessons = listOf(
        LessonEntity(401, 10, "مقدمة الكتابة والنطق", "Tulisan Latin", "تعرف الحروف اللاتينية وأن كل مقطع يُنطق", "a0-1", false),
        LessonEntity(402, 10, "الصوائت a i u e o", "Vokal a i u e o", "تفرّق بين /e/ و /ə/ بالأذن لا بالتخمين", "a0-2", false),
        LessonEntity(403, 10, "السواكن الأساسية", "Konsonan dasar", "تنطق p و b و r و h بوضوح دون تفخيم عربي", "a0-3", false),
        LessonEntity(404, 10, "الأصوات c و j و g", "Bunyi c j g", "تصف c و j و g وصفاً صوتياً صحيحاً", "a0-4", false),
        LessonEntity(405, 10, "الصوتان ng و ny", "ng dan ny", "تميز /ŋ/ و /ɲ/ عن النون والغين", "a0-5", false),
        LessonEntity(406, 10, "الصوتان sy و kh", "sy dan kh", "تعرف متى يظهران وأنهما ليسا القاعدة اليومية كلها", "a0-6", false),
        LessonEntity(407, 10, "k النهائية والنبر والتنوع", "k akhir & aksen", "لا تعمم همزة النهاية ولا لكنة جزيرة واحدة", "a0-7", false),
        LessonEntity(408, 10, "قراءة موجهة", "Membaca terarah", "تقرأ جملاً قصيرة ببطء مع الاستماع", "a0-8", false),
        LessonEntity(409, 10, "اختبار انتقال A0", "Ujian pindah A0", "تحقق 80٪ قبل الانتقال للأساس", "a0-9", false)
    )

    val details = listOf(
        LessonDetailEntity(
            401, 101,
            "الإندونيسية تُكتب باللاتينية الحديثة. القاعدة الأولى: انطق كل مقطع؛ لا توجد حروف صامتة كالإنجليزية. النطق التقريبي بالعربية عكّاز فقط؛ الأذن الإندونيسية هي المرجع.",
            "saya ||| أنا\nkamu ||| أنت\nbuku ||| كتاب\nIndonesia ||| إندونيسيا",
            "استرجاع: أغلق الشاشة وتهجَّ: sa-ya، ka-mu، bu-ku. لا تختلس المقطع الأخير.",
            "تمييز: أي كلمة تُقرأ بمقطعين واضحين؟ saya أم «سيا» كمقطع واحد؟ الصحيح sa-ya.",
            "خطأ شائع: قراءة الكلمة كأنها إنجليزية (Indonesia بـ sh). الصحيح: إِنْ-دو-ني-سي-ا بمقاطع مفتوحة.",
            "اسمع: saya, kamu, buku"
        ),
        LessonDetailEntity(
            402, 102,
            "ستة أصوات شائعة في الاستعمال: a /a/، i /i/، u /u/، o /o/ أو /ɔ/، ثم حرف e لصوتين: e taling /e/ أو /ɛ/، و e pepet /ə/. لا توجد علامة ثابتة في الكتابة اليومية؛ تتعلمها بالسمع والقاموس عند الشك.",
            "makan ||| يأكل — a واضحة\nini ||| هذا — i قصيرة\nsusu ||| حليب — u قصيرة\ntoko ||| دكان — o غير ممدودة كثيراً\nenak ||| لذيذ — e أقرب إلى /e/\nsenang ||| مسرور — e الأولى /ə/ قصيرة جداً",
            "استرجاع: قل senang بلا مدّ في أول مقطع. إن سمعت «سينانغ» فأعدها.",
            "تمييز: enak (/e/) مقابل senang (أول e = /ə/). استمع ثم اختر أيهما فيه شوا قصيرة.",
            "خطأ العرب: مدّ pepet حتى تصير /i/ أو /e/ طويلة. selamat ليست «سيييلامت».",
            "اسمع: makan, enak, senang, selamat"
        ),
        LessonDetailEntity(
            403, 103,
            "السواكن الأساسية قريبة من أصوات تعرفها، لكن r لمسة لسان سريعة غير مفخمة، و p مهموسة (ليست باء)، و h تُنطق في البداية في النطق القياسي.",
            "pasar ||| سوق\nbagi ||| لـ / يعطي قسماً\nrumah ||| بيت\nhari ||| يوم\ntidak ||| لا",
            "استرجاع: ضع ورقة أمام فمك: pasar يجب أن تحرّك الورقة أكثر من bagi.",
            "تمييز: pagi (صباح) مقابل bagi. الفرق في p/b لا في الحركة.",
            "خطأ العرب: pasar → «بازار»، وتفخيم r كما في «رمضان». اجعل الراء خفيفة.",
            "اسمع: pasar, pagi, bagi, rumah, hari"
        ),
        LessonDetailEntity(
            404, 104,
            "c = /tʃ/ كـ ch في church. j = /dʒ/ كـ j في judge الإنجليزية. g = /g/ كـ g في go. لا نصف c سيناً، ولا g قافاً، ولا نثبت أن j «جيم مصرية» لأن ذلك يخلط لهجات عربية مختلفة.",
            "coba ||| جرّب\ncinta ||| حب\nkecil ||| صغير\njalan ||| طريق / يمشي\njam ||| ساعة\ntoko ||| دكان — لا قاف في g هنا أصلاً\nguru ||| معلّم — g = /g/",
            "استرجاع: coba = تشوبا. jalan = صوت /dʒ/ لا ياء. guru = گورو لا قورو.",
            "تمييز: coba (/tʃ/) مقابل soba إن سمعتها بسین. الصحيح يبدأ بـ /tʃ/.",
            "خطأ العرب: coba → سوبا؛ guru → قورو؛ المبالغة في تشبيه j بلهجة عربية واحدة.",
            "اسمع: coba, cinta, jalan, guru"
        ),
        LessonDetailEntity(
            405, 105,
            "ng = /ŋ/ صوت أنفي طبقي واحد، كـ ng في sing، ويظهر في أول الكلمة أيضاً. ny = /ɲ/ صوت حنكي واحد كـ ñ. ليسا نوناً ثم حرفاً منفصلاً، و ng ليست غيناً عربية.",
            "jangan ||| لا تفعل\nuang ||| نقود\nngantuk ||| نعسان\nnyanyi ||| يغني\nbanyak ||| كثير\npunya ||| يملك",
            "استرجاع: uang تنتهي بـ /ŋ/ لا بهمزة ولا بغين احتكاكية.",
            "تمييز: jangan (/ŋ/) مقابل «جانان» بنون فقط. استمع للنهاية.",
            "خطأ العرب: فصل ny إلى n+y (ban-yak)، وتحويل ng إلى ن أو غ أو همزة.",
            "اسمع: jangan, uang, nyanyi, banyak"
        ),
        LessonDetailEntity(
            406, 106,
            "sy ≈ /ʃ/ و kh ≈ /x/ يظهران غالباً في كلمات من أصل عربي أو فارسي. الناطق الإندونيسي قد يخفف kh حتى تقترب من k. لا تصحّح نطقهم بعربية فصحى.",
            "syarat ||| شرط\nsyukur ||| شكر\nmasyarakat ||| مجتمع\nkhusus ||| خاص\nakhir ||| آخر\nikhlas ||| إخلاص",
            "استرجاع: syarat بالشين لا بالسين. khusus قد تسمعها أقرب إلى «كوسوس» عند بعض المتكلمين.",
            "تمييز: syarat (/ʃ/) مقابل sarat إن نُطقت سيناً. المعنى والقائمة هنا للشين.",
            "خطأ العرب: نطق waktu و kabar و syarat كنطق عربي حرفي، أو الإصرار على خاء فصحى مشبعة.",
            "اسمع: syarat, syukur, khusus, akhir"
        ),
        LessonDetailEntity(
            407, 107,
            "k في آخر الكلمة تُنطق [k] في الكلام المتأنّي. في كلمات شائعة مثل tidak و bapak قد تُخفَّف أو تُسمع أقرب إلى [ʔ] عند كثير من المتكلمين. هذا تنوع، لا قاعدة «k النهائية = همزة دائماً». النبر خفيف وغالباً على المقطع قبل الأخير إن لم يكن /ə/. اللهجات الجزرية تختلف؛ لا تعمم جاكرتا على كل إندونيسيا.",
            "tidak ||| لا — قد تُخفَّف النهاية\nbapak ||| أب / سيد — تنوع في الإطلاق\nenak ||| لذيذ\nJakarta ||| جاكرتا — ليست prononce قاف",
            "استرجاع: اقرأ tidak مرة بكاف خفيفة ومرة كما تسمعها في كلام سريع. المعنيان نفس الكلمة.",
            "تمييز: هل يجب أن تكون نهاية tidak همزة دائماً؟ لا.",
            "خطأ العرب: تعميم الهمزة، أو إدخال قاف في Jakarta و toko، أو ادعاء أن لكنة جاوة هي الإندونيسية الوحيدة.",
            "اسمع: tidak, bapak, enak, Jakarta"
        ),
        LessonDetailEntity(
            408, 108,
            "اقرأ ببطء، مقطعًا مقطعًا، ثم استمع. الهدف الفهم السمعي لا السرعة. هذه جمل A0 للقراءة لا للحوار الكامل.",
            "Saya makan. ||| أنا آكل.\nIni buku. ||| هذا كتاب.\nJangan lari. ||| لا تركض.\nUang di sini. ||| النقود هنا.\nGuru itu baik. ||| ذلك المعلم طيب.",
            "استرجاع: أغلق النص وقل من الذاكرة: Saya makan. ثم Ini buku.",
            "تمييز: Jangan تبدأ بـ /dʒ/ وتنتهي بـ /ŋ/. إن سمعت «يانان» فأعد الاستماع.",
            "خطأ العرب: تقطيع عسكري لكل مقطع بلا ربط، أو ترجمة حرفية قبل السماع.",
            "اسمع: Saya makan, Ini buku, Jangan lari"
        ),
        LessonDetailEntity(
            409, 109,
            "اختبار انتقال A0: عشرة أسئلة في التمييز والقراءة. النجاح 80٪ (8 من 10). الإجابات الخاطئة تُحفظ في سجل الأسئلة وتُجدول للمراجعة التكيفية 1→3→7→14 يوماً. الرسوب يعني إعادة دروس النطق لا القفز للأساس.",
            "coba ||| ليست سينًا\nguru ||| /g/ ليست قافاً\nuang ||| نهاية /ŋ/\nsenang ||| أول e غالباً /ə/",
            "استرجاع: اذكر من دون نظر فرقاً واحداً بين /e/ و /ə/، وفرقاً بين ng و ny.",
            "تمييز: راجع أزواج الدروس السابقة قبل فتح الاختبار.",
            "خطأ شائع: اعتبار الاختبار «نجاحاً شكلياً». 80٪ هنا شرط انتقال حقيقي.",
            "اسمع: coba, guru, uang, senang"
        )
    )

    val quizzes = listOf(
        TrainingItemEntity(400, "MULTIPLE_CHOICE", "كم مقطعاً واضحاً في saya؟", "مقطعان: sa-ya", "مقطع واحد,مقطعان: sa-ya,ثلاثة مقاطع,لا تُنطق الياء", "كل حرف صوتي يُنطق", "A0 كتابة"),
        TrainingItemEntity(401, "MULTIPLE_CHOICE", "النطق العربي التقريبي هو:", "عكّاز بعد السماع", "بديل كافٍ عن الناطق,عكّاز بعد السماع,أدق من التسجيل,يُغني عن التكرار", "الأذن الإندونيسية هي الحكم", "A0 كتابة"),
        TrainingItemEntity(402, "MULTIPLE_CHOICE", "أول e في senang غالباً:", "/ə/ قصيرة جداً", "/e/ طويلة,/i/ ممالة,/ə/ قصيرة جداً,صامتة تماماً", "e pepet لا تُمدّ", "A0 صوائت"),
        TrainingItemEntity(403, "MULTIPLE_CHOICE", "enak مقابل senang يوضح:", "نفس الصوت مكتوب e لصوتين", "أن e دائماً /e/,نفس الصوت مكتوب e لصوتين,أن الكتابة تضع علامة دائماً,أن o تساوي u", "تعلّم بالسمع", "A0 صوائت"),
        TrainingItemEntity(404, "MULTIPLE_CHOICE", "pasar تبدأ بـ:", "p مهموسة", "باء عربية,p مهموسة,فاء,قاف", "ورقة أمام الفم تتحرك مع p", "A0 سواكن"),
        TrainingItemEntity(405, "MULTIPLE_CHOICE", "r الإندونيسية:", "لمسة لسان خفيفة", "راء مفخمة كرمضان,تكرار إسباني مشدد دائماً,لمسة لسان خفيفة,غين", "لا تفخّم", "A0 سواكن"),
        TrainingItemEntity(406, "MULTIPLE_CHOICE", "c في coba هو:", "/tʃ/ كـ church", "سين,/tʃ/ كـ church,كاف,ثاء", "ليست سيناً", "A0 c j g"),
        TrainingItemEntity(407, "MULTIPLE_CHOICE", "g في guru هو:", "/g/ كما في go", "قاف عربية,/g/ كما في go,غين احتكاكية,جيم فصحى", "لا تقل إنها قاف", "A0 c j g"),
        TrainingItemEntity(408, "MULTIPLE_CHOICE", "j في jalan أقرب إلى:", "/dʒ/ كما في judge", "ياء,/dʒ/ كما في judge,جيم فصحى معطشة ثقيلة حتماً,شين", "لا تثبّت وصفاً بلهجة عربية واحدة", "A0 c j g"),
        TrainingItemEntity(409, "MULTIPLE_CHOICE", "ng في uang:", "صوت /ŋ/ واحد", "نون ثم غين,صوت /ŋ/ واحد,همزة دائماً,قاف", "ليس غيناً عربية", "A0 ng ny"),
        TrainingItemEntity(410, "MULTIPLE_CHOICE", "ny في banyak:", "صوت /ɲ/ ملتحم", "n ثم y منفصلتان,صوت /ɲ/ ملتحم,نون فقط,ياء فقط", "لسان واحد على الحنك", "A0 ng ny"),
        TrainingItemEntity(411, "MULTIPLE_CHOICE", "sy في syarat:", "قريب من /ʃ/", "سين دائماً,قريب من /ʃ/,صاد,ثاء", "غالباً في دخيل عربي", "A0 sy kh"),
        TrainingItemEntity(412, "MULTIPLE_CHOICE", "kh عند الإندونيسيين:", "قد تُخفَّف نحو k", "خاء فصحى مشبعة دائماً,قد تُخفَّف نحو k,قاف,غين", "لا تصحّح نطقهم بالعربية", "A0 sy kh"),
        TrainingItemEntity(413, "MULTIPLE_CHOICE", "k في نهاية tidak:", "قد تُخفَّف أو تُهمز عند بعض المتكلمين", "همزة دائماً في كل إندونيسيا,كاف فصحى مشبعة دائماً,قد تُخفَّف أو تُهمز عند بعض المتكلمين,تُحذف كتابة", "تنوع لا قاعدة مطلقة", "A0 نبر"),
        TrainingItemEntity(414, "MULTIPLE_CHOICE", "نبر Jakarta ولهجات الجزر:", "النبر خفيف والتنوع الإقليمي حقيقي", "جاكرتا تمثل كل البلاد,النبر يغيّر المعنى كالصينية,النبر خفيف والتنوع الإقليمي حقيقي,يجب تقليد جاوة فقط", "لا تعميم مضلل", "A0 نبر"),
        TrainingItemEntity(415, "MULTIPLE_CHOICE", "Saya makan تعني:", "أنا آكل", "أنا أشرب,أنا آكل,هذا كتاب,لا تركض", "قراءة موجهة", "A0 قراءة"),
        TrainingItemEntity(416, "MULTIPLE_CHOICE", "Jangan lari تبدأ بـ:", "/dʒ/ وتنتهي بـ /ŋ/", "ياء ونون فقط,/dʒ/ وتنتهي بـ /ŋ/,سين وغين,قاف", "راجع الدرسين 4 و5", "A0 قراءة"),

        TrainingItemEntity(430, "MULTIPLE_CHOICE", "coba لا تبدأ بـ:", "سين", "سين,/tʃ/,تش,صوت احتكاكي-انفجاري", "c = /tʃ/", EXIT_CATEGORY),
        TrainingItemEntity(431, "MULTIPLE_CHOICE", "guru لا تُنطق بـ:", "قاف", "قاف,/g/,گ,صوت مجهور طبقي", "g ليست قافاً", EXIT_CATEGORY),
        TrainingItemEntity(432, "MULTIPLE_CHOICE", "نهاية uang:", "/ŋ/", "نون فقط,غين عربية,/ŋ/,همزة حتماً", "ng صوت واحد", EXIT_CATEGORY),
        TrainingItemEntity(433, "MULTIPLE_CHOICE", "أول e في senang:", "/ə/", "/e/ طويلة,/ə/,/i/,صامت", "pepet قصيرة", EXIT_CATEGORY),
        TrainingItemEntity(434, "MULTIPLE_CHOICE", "nyanyi فيها:", "/ɲ/ ملتحمة", "n + y منفصلتان,/ɲ/ ملتحمة,غين,قاف", "ny حرف صوتي واحد نطقاً", EXIT_CATEGORY),
        TrainingItemEntity(435, "MULTIPLE_CHOICE", "k نهاية tidak:", "ليست همزة في كل النطق دائماً", "همزة إلزامية في كل جزيرة,ليست همزة في كل النطق دائماً,تُحذف من الكتابة,تساوي q", "تنوع إقليمي ولفظي", EXIT_CATEGORY),
        TrainingItemEntity(436, "MULTIPLE_CHOICE", "pasar مقابل bagi يدرّب:", "الفرق بين p و b", "الفرق بين /e/ و /ə/,الفرق بين p و b,النبر الصيني,جمع الأسماء", "ورقة أمام الفم", EXIT_CATEGORY),
        TrainingItemEntity(437, "MULTIPLE_CHOICE", "syarat:", "شين تقريبية /ʃ/", "سين فقط,شين تقريبية /ʃ/,صاد مفخمة,ثاء", "دخيل غالباً", EXIT_CATEGORY),
        TrainingItemEntity(438, "MULTIPLE_CHOICE", "Ini buku تعني:", "هذا كتاب", "أنا آكل,هذا كتاب,لا تركض,النقود هنا", "قراءة A0", EXIT_CATEGORY),
        TrainingItemEntity(439, "MULTIPLE_CHOICE", "j في jalan تُوصف بأمان كـ:", "/dʒ/ كما في judge", "جيم مصرية حصراً,/dʒ/ كما في judge,ياء,قاف", "تجنّب لصق لهجة عربية واحدة", EXIT_CATEGORY)
    )
}
