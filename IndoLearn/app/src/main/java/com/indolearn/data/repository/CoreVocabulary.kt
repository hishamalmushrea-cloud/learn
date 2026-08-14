package com.indolearn.data.repository

import com.indolearn.data.local.entity.CasualExpressionEntity
import com.indolearn.data.local.entity.GrammarEntity
import com.indolearn.data.local.entity.VocabularyEntity

/**
 * المفردات الأساسية التي لا يستطيع مبتدئ الاستغناء عنها.
 *
 * لماذا وُجد هذا الملف: كشف التدقيق (tools/audit_content.py) اختلالاً حاداً
 * في توزيع المفردات — 101 من أصل 141 كلمة إندونيسية كانت **أفعالاً** (72%)،
 * بينما لم يكن في التطبيق سوى **رقمين** (satu, dua) و**ضميرين** (aku, kamu)،
 * ولا يوم واحد من أيام الأسبوع، ولا لون واحد، ولا عضو من الجسم،
 * ولا كلمة عن المال أو الاتجاهات.
 *
 * أي متعلم يريد أن يسأل «كم السعر؟» أو «أين الحمام؟» أو يحدد موعداً
 * يوم الثلاثاء كان يصطدم بفراغ، رغم أن التطبيق يعرض له 101 فعلاً.
 *
 * مبدأ الاختيار: الدقة ثم الصلة ثم الفائدة — لا حشو لبلوغ عدد.
 * كل كلمة هنا مقترنة بمثال طبيعي وترجمته، ومصنّفة تصنيفاً صحيحاً،
 * ومربوطة بمستوى مناسب (0 = أساسي، 1 = توسّع).
 *
 * نطاق المعرّفات: 3000–3299 (كتلة فارغة تماماً — تحقق tools/validate_seed.py
 * من عدم التصادم مع 1–2026 المستخدمة سابقاً).
 */
object CoreVocabulary {

    /** أيام الأسبوع — إندونيسية. المصدر: Preply, PolyglotClub, ielanguages. */
    private val indonesianDays = listOf(
        VocabularyEntity(3000, "Senin", "Senin", "سِنِين", "الاثنين",
            "Saya kerja hari Senin.", "أعمل يوم الاثنين.", "أيام", 0, true, false, "ID"),
        VocabularyEntity(3001, "Selasa", "Selasa", "سِلاسا", "الثلاثاء",
            "Kita bertemu hari Selasa.", "نلتقي يوم الثلاثاء.", "أيام", 0, true, false, "ID"),
        VocabularyEntity(3002, "Rabu", "Rabu", "رابو", "الأربعاء",
            "Hari Rabu saya libur.", "يوم الأربعاء عطلتي.", "أيام", 0, true, false, "ID"),
        VocabularyEntity(3003, "Kamis", "Kamis", "كاميس", "الخميس",
            "Pasar buka hari Kamis.", "السوق يفتح يوم الخميس.", "أيام", 0, true, false, "ID"),
        VocabularyEntity(3004, "Jumat", "Jumat", "جوم-عات", "الجمعة",
            "Hari Jumat kami salat.", "يوم الجمعة نصلي.", "أيام", 0, true, false, "ID"),
        VocabularyEntity(3005, "Sabtu", "Sabtu", "سابْتو", "السبت",
            "Sabtu saya di rumah.", "السبت أكون في البيت.", "أيام", 0, true, false, "ID"),
        VocabularyEntity(3006, "Minggu", "Minggu", "مينْغغو", "الأحد / الأسبوع",
            "Hari Minggu kami istirahat.", "يوم الأحد نرتاح.", "أيام", 0, true, false, "ID"),
        VocabularyEntity(3007, "hari", "hari", "هاري", "يوم",
            "Hari ini panas.", "اليوم حار.", "وقت", 0, true, false, "ID"),
        VocabularyEntity(3008, "besok", "besok", "بيسوك", "غداً",
            "Besok saya pergi.", "غداً أذهب.", "وقت", 0, true, false, "ID"),
        VocabularyEntity(3009, "kemarin", "kemarin", "كِمارين", "أمس",
            "Kemarin saya sakit.", "أمس كنت مريضاً.", "وقت", 0, true, false, "ID"),
        VocabularyEntity(3010, "sekarang", "sekarang", "سِكارانغ", "الآن",
            "Sekarang jam lima.", "الآن الساعة الخامسة.", "وقت", 0, true, false, "ID"),
        VocabularyEntity(3011, "nanti", "nanti", "نانتي", "لاحقاً",
            "Nanti saya telepon.", "لاحقاً أتصل.", "وقت", 0, true, false, "ID"),
        VocabularyEntity(3012, "jam", "jam", "جام", "ساعة",
            "Jam berapa sekarang?", "كم الساعة الآن؟", "وقت", 0, true, false, "ID"),
    )

    /** الأرقام — كانت الفجوة الأفدح: رقمان فقط في التطبيق كله. */
    private val indonesianNumbers = listOf(
        VocabularyEntity(3020, "nol", "nol", "نول", "صفر",
            "Nol koma lima.", "صفر فاصلة خمسة.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3021, "tiga", "tiga", "تيغا", "ثلاثة",
            "Saya punya tiga anak.", "عندي ثلاثة أطفال.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3022, "empat", "empat", "أمْبات", "أربعة",
            "Empat orang di sini.", "أربعة أشخاص هنا.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3023, "lima", "lima", "ليما", "خمسة",
            "Lima ribu rupiah.", "خمسة آلاف روبية.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3024, "enam", "enam", "أنام", "ستة",
            "Enam jam lagi.", "بعد ست ساعات.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3025, "tujuh", "tujuh", "توجوه", "سبعة",
            "Tujuh hari seminggu.", "سبعة أيام في الأسبوع.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3026, "delapan", "delapan", "دِلابان", "ثمانية",
            "Delapan jam kerja.", "ثماني ساعات عمل.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3027, "sembilan", "sembilan", "سِمبيلان", "تسعة",
            "Sembilan orang datang.", "تسعة أشخاص جاؤوا.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3028, "sepuluh", "sepuluh", "سِپولوه", "عشرة",
            "Sepuluh menit lagi.", "بعد عشر دقائق.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3029, "sebelas", "sebelas", "سِبِلاس", "أحد عشر",
            "Umur saya sebelas.", "عمري أحد عشر.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3030, "dua puluh", "dua puluh", "دوا پولوه", "عشرون",
            "Dua puluh ribu.", "عشرون ألفاً.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3031, "seratus", "seratus", "سِراتوس", "مئة",
            "Seratus ribu rupiah.", "مئة ألف روبية.", "أرقام", 0, true, false, "ID"),
        VocabularyEntity(3032, "seribu", "seribu", "سِريبو", "ألف",
            "Harganya seribu.", "سعره ألف.", "أرقام", 0, true, false, "ID"),
    )

    /** الضمائر — كان في التطبيق ضميران فقط من أصل ما يلزم. */
    private val indonesianPronouns = listOf(
        // ملاحظة: saya موجودة أصلاً (id=3) وصُحّح تصنيفها إلى «ضمائر»
        // بدل «تعارف» — لا تُكرَّر هنا.
        VocabularyEntity(3041, "Anda", "Anda", "آندا", "أنت (رسمي)",
            "Anda tinggal di mana?", "أين تسكن؟", "ضمائر", 0, true, false, "ID"),
        VocabularyEntity(3042, "dia", "dia", "ديا", "هو / هي",
            "Dia teman saya.", "هو صديقي.", "ضمائر", 0, true, false, "ID"),
        VocabularyEntity(3043, "kami", "kami", "كامي", "نحن (دون المخاطَب)",
            "Kami dari Yaman.", "نحن من اليمن.", "ضمائر", 0, true, false, "ID"),
        VocabularyEntity(3044, "kita", "kita", "كيتا", "نحن (معك)",
            "Kita pergi bersama.", "نذهب معاً.", "ضمائر", 0, true, false, "ID"),
        VocabularyEntity(3045, "mereka", "mereka", "مِريكا", "هم",
            "Mereka sudah pulang.", "هم عادوا.", "ضمائر", 0, true, false, "ID"),
    )

    /** الألوان. */
    private val indonesianColors = listOf(
        VocabularyEntity(3050, "merah", "merah", "ميراه", "أحمر",
            "Baju merah itu bagus.", "ذلك القميص الأحمر جميل.", "ألوان", 0, true, false, "ID"),
        VocabularyEntity(3051, "biru", "biru", "بيرو", "أزرق",
            "Langit biru.", "السماء زرقاء.", "ألوان", 0, true, false, "ID"),
        VocabularyEntity(3052, "hijau", "hijau", "هيجاو", "أخضر",
            "Daun hijau.", "الورقة خضراء.", "ألوان", 0, true, false, "ID"),
        VocabularyEntity(3053, "kuning", "kuning", "كونينغ", "أصفر",
            "Bunga kuning.", "زهرة صفراء.", "ألوان", 0, true, false, "ID"),
        VocabularyEntity(3054, "hitam", "hitam", "هيتام", "أسود",
            "Kopi hitam.", "قهوة سوداء.", "ألوان", 0, true, false, "ID"),
        VocabularyEntity(3055, "putih", "putih", "پوتيه", "أبيض",
            "Nasi putih.", "أرز أبيض.", "ألوان", 0, true, false, "ID"),
    )

    /** المال والسوق — جوهري لمن يبيع ويشتري. */
    private val indonesianMoney = listOf(
        VocabularyEntity(3060, "uang", "uang", "أوانغ", "نقود",
            "Saya tidak punya uang.", "ليس معي نقود.", "مال", 0, true, false, "ID"),
        VocabularyEntity(3061, "harga", "harga", "هارغا", "سعر",
            "Berapa harganya?", "كم سعره؟", "مال", 0, true, false, "ID"),
        VocabularyEntity(3062, "gratis", "gratis", "غراتيس", "مجاني",
            "Ini gratis.", "هذا مجاني.", "مال", 0, true, false, "ID"),
        VocabularyEntity(3063, "kembalian", "kembalian", "كِمباليان", "الباقي (من النقود)",
            "Ini kembaliannya.", "هذا الباقي.", "مال", 1, true, false, "ID"),
        VocabularyEntity(3064, "diskon", "diskon", "ديسكون", "خصم",
            "Ada diskon hari ini?", "هل يوجد خصم اليوم؟", "مال", 1, true, false, "ID"),
    )

    /** الاتجاهات والمكان — لا يمكن السؤال عن الطريق بدونها. */
    private val indonesianDirections = listOf(
        VocabularyEntity(3070, "kanan", "kanan", "كانان", "يمين",
            "Belok kanan.", "انعطف يميناً.", "اتجاهات", 0, true, false, "ID"),
        VocabularyEntity(3071, "kiri", "kiri", "كيري", "يسار",
            "Belok kiri di sana.", "انعطف يساراً هناك.", "اتجاهات", 0, true, false, "ID"),
        VocabularyEntity(3072, "lurus", "lurus", "لوروس", "مستقيم / إلى الأمام",
            "Jalan lurus saja.", "امشِ مستقيماً فقط.", "اتجاهات", 0, true, false, "ID"),
        VocabularyEntity(3073, "dekat", "dekat", "دِكات", "قريب",
            "Pasar dekat dari sini.", "السوق قريب من هنا.", "اتجاهات", 0, true, false, "ID"),
        VocabularyEntity(3074, "jauh", "jauh", "جاوه", "بعيد",
            "Rumahnya jauh.", "بيته بعيد.", "اتجاهات", 0, true, false, "ID"),
        VocabularyEntity(3075, "depan", "depan", "دِپان", "أمام",
            "Di depan masjid.", "أمام المسجد.", "اتجاهات", 0, true, false, "ID"),
        VocabularyEntity(3076, "belakang", "belakang", "بِلاكانغ", "خلف",
            "Di belakang rumah.", "خلف البيت.", "اتجاهات", 0, true, false, "ID"),
    )

    /** الجسم والصحة — يلزم عند الطبيب والصيدلية. */
    private val indonesianBody = listOf(
        VocabularyEntity(3080, "kepala", "kepala", "كِپالا", "رأس",
            "Kepala saya sakit.", "رأسي يؤلمني.", "جسم", 0, true, false, "ID"),
        VocabularyEntity(3081, "perut", "perut", "پيروت", "بطن",
            "Perut saya sakit.", "بطني تؤلمني.", "جسم", 0, true, false, "ID"),
        VocabularyEntity(3082, "tangan", "tangan", "تانغان", "يد",
            "Cuci tangan dulu.", "اغسل يديك أولاً.", "جسم", 0, true, false, "ID"),
        VocabularyEntity(3083, "kaki", "kaki", "كاكي", "قدم / رِجل",
            "Kaki saya lelah.", "قدماي متعبتان.", "جسم", 0, true, false, "ID"),
        VocabularyEntity(3084, "mata", "mata", "ماتا", "عين",
            "Mata saya merah.", "عيني حمراء.", "جسم", 0, true, false, "ID"),
        VocabularyEntity(3085, "sakit", "sakit", "ساكيت", "مريض / يؤلم",
            "Saya sakit hari ini.", "أنا مريض اليوم.", "صحة", 0, true, false, "ID"),
        VocabularyEntity(3086, "obat", "obat", "أوبات", "دواء",
            "Saya butuh obat.", "أحتاج دواءً.", "صحة", 0, true, false, "ID"),
        VocabularyEntity(3087, "dokter", "dokter", "دوكتير", "طبيب",
            "Saya mau ke dokter.", "أريد الذهاب إلى الطبيب.", "صحة", 0, true, false, "ID"),
    )

    /** الملابس. */
    private val indonesianClothes = listOf(
        VocabularyEntity(3090, "baju", "baju", "باجو", "قميص / ثوب",
            "Baju ini bagus.", "هذا القميص جميل.", "ملابس", 0, true, false, "ID"),
        VocabularyEntity(3091, "celana", "celana", "تشِلانا", "بنطال",
            "Celana saya baru.", "بنطالي جديد.", "ملابس", 0, true, false, "ID"),
        VocabularyEntity(3092, "sepatu", "sepatu", "سِپاتو", "حذاء",
            "Sepatu ini mahal.", "هذا الحذاء غالٍ.", "ملابس", 0, true, false, "ID"),
        VocabularyEntity(3093, "ukuran", "ukuran", "أوكورَان", "مقاس",
            "Ada ukuran lain?", "هل يوجد مقاس آخر؟", "ملابس", 1, true, false, "ID"),
    )

    // ================= التركية =================

    /** التركية: كانت 26 كلمة فقط بلا ضمائر ولا أرقام ولا تحيات. */
    private val turkishCore = listOf(
        // ضمائر
        VocabularyEntity(3100, "ben", "ben", "بن", "أنا",
            "Ben öğrenciyim.", "أنا طالب.", "ضمائر", 0, true, false, "TR"),
        VocabularyEntity(3101, "sen", "sen", "سن", "أنت",
            "Sen nerelisin?", "من أين أنت؟", "ضمائر", 0, true, false, "TR"),
        VocabularyEntity(3102, "o", "o", "أو", "هو / هي",
            "O benim arkadaşım.", "هو صديقي.", "ضمائر", 0, true, false, "TR"),
        VocabularyEntity(3103, "biz", "biz", "بيز", "نحن",
            "Biz Yemenliyiz.", "نحن يمنيون.", "ضمائر", 0, true, false, "TR"),
        VocabularyEntity(3104, "siz", "siz", "سيز", "أنتم (ورسمي للمفرد)",
            "Siz nerede oturuyorsunuz?", "أين تسكنون؟", "ضمائر", 0, true, false, "TR"),
        VocabularyEntity(3105, "onlar", "onlar", "أونلار", "هم",
            "Onlar geldiler.", "هم جاؤوا.", "ضمائر", 0, true, false, "TR"),
        // أرقام
        VocabularyEntity(3110, "bir", "bir", "بير", "واحد",
            "Bir çay lütfen.", "شاي واحد من فضلك.", "أرقام", 0, true, false, "TR"),
        VocabularyEntity(3111, "iki", "iki", "إيكي", "اثنان",
            "İki ekmek aldım.", "اشتريت رغيفين.", "أرقام", 0, true, false, "TR"),
        VocabularyEntity(3112, "üç", "üç", "أوتش", "ثلاثة",
            "Üç kardeşim var.", "لدي ثلاثة إخوة.", "أرقام", 0, true, false, "TR"),
        VocabularyEntity(3113, "dört", "dört", "دورت", "أربعة",
            "Dört kişiyiz.", "نحن أربعة أشخاص.", "أرقام", 0, true, false, "TR"),
        VocabularyEntity(3114, "beş", "beş", "بش", "خمسة",
            "Beş lira.", "خمس ليرات.", "أرقام", 0, true, false, "TR"),
        VocabularyEntity(3115, "on", "on", "أون", "عشرة",
            "On dakika sonra.", "بعد عشر دقائق.", "أرقام", 0, true, false, "TR"),
        VocabularyEntity(3116, "yüz", "yüz", "يوز", "مئة",
            "Yüz lira verdim.", "أعطيت مئة ليرة.", "أرقام", 0, true, false, "TR"),
        // تحيات وتعارف
        VocabularyEntity(3120, "merhaba", "merhaba", "مرحبا", "مرحباً",
            "Merhaba, nasılsın?", "مرحباً، كيف حالك؟", "تحيات", 0, true, false, "TR"),
        VocabularyEntity(3121, "günaydın", "günaydın", "غون-آيدن", "صباح الخير",
            "Günaydın hocam.", "صباح الخير يا أستاذ.", "تحيات", 0, true, false, "TR"),
        VocabularyEntity(3122, "teşekkür ederim", "teşekkür ederim", "تشكّور إدريم", "شكراً لك",
            "Çok teşekkür ederim.", "شكراً جزيلاً لك.", "تحيات", 0, true, false, "TR"),
        VocabularyEntity(3123, "lütfen", "lütfen", "لوتفن", "من فضلك",
            "Bir su lütfen.", "ماء من فضلك.", "تحيات", 0, true, false, "TR"),
        VocabularyEntity(3124, "hoşça kal", "hoşça kal", "هوشتشا كال", "مع السلامة",
            "Hoşça kal, görüşürüz.", "مع السلامة، نراك لاحقاً.", "تحيات", 0, true, false, "TR"),
        // سؤال ونفي
        VocabularyEntity(3130, "ne", "ne", "نه", "ماذا",
            "Bu ne?", "ما هذا؟", "سؤال", 0, true, false, "TR"),
        VocabularyEntity(3131, "kim", "kim", "كيم", "من",
            "O kim?", "من هو؟", "سؤال", 0, true, false, "TR"),
        VocabularyEntity(3132, "nerede", "nerede", "نرده", "أين",
            "Tuvalet nerede?", "أين الحمام؟", "سؤال", 0, true, false, "TR"),
        VocabularyEntity(3133, "ne kadar", "ne kadar", "نه قدر", "كم (السعر)",
            "Bu ne kadar?", "كم سعر هذا؟", "سؤال", 0, true, false, "TR"),
        VocabularyEntity(3134, "neden", "neden", "نِدن", "لماذا",
            "Neden geldin?", "لماذا أتيت؟", "سؤال", 0, true, false, "TR"),
        VocabularyEntity(3135, "değil", "değil", "دَيل", "ليس",
            "Bu doğru değil.", "هذا ليس صحيحاً.", "نفي", 0, true, false, "TR"),
        VocabularyEntity(3136, "yok", "yok", "يوك", "لا يوجد",
            "Param yok.", "ليس معي نقود.", "نفي", 0, true, false, "TR"),
        // var نقيض yok لا نوع منه؛ يُصنَّف «وجود» لا «نفي».
        VocabularyEntity(3137, "var", "var", "فار", "يوجد",
            "Odanız var mı?", "هل لديكم غرفة؟", "وجود", 0, true, false, "TR"),
        // مال واتجاهات
        VocabularyEntity(3140, "para", "para", "پارا", "نقود",
            "Param yok.", "ليس معي نقود.", "مال", 0, true, false, "TR"),
        VocabularyEntity(3141, "fiyat", "fiyat", "فيات", "سعر",
            "Fiyatı ne kadar?", "كم سعره؟", "مال", 0, true, false, "TR"),
        VocabularyEntity(3142, "sağ", "sağ", "ساغ", "يمين",
            "Sağa dön.", "انعطف يميناً.", "اتجاهات", 0, true, false, "TR"),
        VocabularyEntity(3143, "sol", "sol", "سول", "يسار",
            "Sola dön.", "انعطف يساراً.", "اتجاهات", 0, true, false, "TR"),
        VocabularyEntity(3144, "yakın", "yakın", "ياكن", "قريب",
            "Otel yakın mı?", "هل الفندق قريب؟", "اتجاهات", 0, true, false, "TR"),
        VocabularyEntity(3145, "uzak", "uzak", "أوزاك", "بعيد",
            "Burası çok uzak.", "هذا المكان بعيد جداً.", "اتجاهات", 0, true, false, "TR"),
        // وقت وجسم
        VocabularyEntity(3150, "bugün", "bugün", "بوغون", "اليوم",
            "Bugün hava güzel.", "الطقس جميل اليوم.", "وقت", 0, true, false, "TR"),
        VocabularyEntity(3151, "yarın", "yarın", "يارن", "غداً",
            "Yarın geliyorum.", "سآتي غداً.", "وقت", 0, true, false, "TR"),
        VocabularyEntity(3152, "dün", "dün", "دون", "أمس",
            "Dün hastaydım.", "كنت مريضاً أمس.", "وقت", 0, true, false, "TR"),
        VocabularyEntity(3153, "saat", "saat", "سا-عت", "ساعة",
            "Saat kaç?", "كم الساعة؟", "وقت", 0, true, false, "TR"),
        VocabularyEntity(3154, "baş", "baş", "باش", "رأس",
            "Başım ağrıyor.", "رأسي يؤلمني.", "جسم", 0, true, false, "TR"),
        VocabularyEntity(3155, "el", "el", "إل", "يد",
            "Ellerini yıka.", "اغسل يديك.", "جسم", 0, true, false, "TR"),
        VocabularyEntity(3156, "hasta", "hasta", "هاستا", "مريض",
            "Ben hastayım.", "أنا مريض.", "صحة", 0, true, false, "TR"),
        VocabularyEntity(3157, "ilaç", "ilaç", "إيلاتش", "دواء",
            "İlaç lazım.", "أحتاج دواءً.", "صحة", 0, true, false, "TR"),
    )


    /**
     * عبارات التعارف التركية.
     *
     * كشف التدقيق أن التركية فيها 7 عبارات تحية لكن **صفر** عبارة تعارف:
     * المتعلم يستطيع أن يقول «مرحباً» ثم يصمت — لا يعرف أن يقول اسمه
     * ولا أن يسأل عن اسم محدثه.
     *
     * التمييز بين sen (غير رسمي) و siz (رسمي) مقصود ومعلَّم صراحةً،
     * لأن استعمال sen مع غريب أو كبير سن يُعدّ قلة أدب في تركيا.
     *
     * المصادر: Preply, TurkishFluent, The Delights of Learning Turkish.
     */
    val turkishIntroductions: List<CasualExpressionEntity> = listOf(
        CasualExpressionEntity(3200, "Adım ...", "آدم", "اسمي ...",
            "🔵 يومي", "أشيع صيغة لتقديم اسمك. يمكن أيضاً: Ben Ali (أنا علي).",
            "Benim adım ...", "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3201, "Adınız ne?", "آدنز نه", "ما اسمك؟ (رسمي)",
            "🟢 رسمي", "مع الغريب أو الأكبر سناً أو في العمل.",
            null, "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3202, "Adın ne?", "آدن نه", "ما اسمك؟ (غير رسمي)",
            "🟡 غير رسمي", "مع الصديق أو من هو في مثل سنك فقط.",
            "Adınız ne?", "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3203, "Memnun oldum", "ممنون أولدوم", "تشرفت بمعرفتك",
            "🔵 يومي", "تُقال عند التعارف. والرد: Ben de memnun oldum.",
            "Tanıştığıma memnun oldum", "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3204, "Ben de memnun oldum", "بن ده ممنون أولدوم",
            "وأنا أيضاً تشرفت", "🔵 يومي", "الرد القياسي على Memnun oldum.",
            null, "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3205, "Nerelisiniz?", "نرەليسينيز", "من أين أنت؟ (رسمي)",
            "🟢 رسمي", "سؤال عن البلد أو المدينة. الرد بلاحقة -li/-lı: Yemenliyim.",
            null, "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3206, "Nerelisin?", "نرەليسين", "من أين أنت؟ (غير رسمي)",
            "🟡 غير رسمي", "مع الأصدقاء فقط.",
            "Nerelisiniz?", "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3207, "Yemenliyim", "يمنلييم", "أنا يمني",
            "🔵 يومي", "اللاحقة -li/-lı/-lu/-lü تعني «من»: İstanbulluyum = أنا من إسطنبول.",
            null, "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3208, "Ne iş yapıyorsunuz?", "نه إيش ياپييورسونوز",
            "ما عملك؟ (رسمي)", "🟢 رسمي", "سؤال مهذب عن المهنة عند التعارف.",
            null, "تحيات وتعارف", 0, "TR"),
        CasualExpressionEntity(3209, "Türkçe öğreniyorum", "توركتشه أورەنييوروم",
            "أنا أتعلم التركية", "🔵 يومي",
            "جملة مفيدة جداً: تشرح ضعف لغتك فيتلطف محدثك ويبطئ كلامه.",
            null, "تحيات وتعارف", 0, "TR"),
    )


    /**
     * قواعد الإندونيسية الأساسية.
     *
     * كشف التدقيق أن جدول القواعد كان يحوي **قاعدة واحدة** للإندونيسية
     * («ترتيب الجملة») مقابل خمس قواعد للتركية. أي أن متعلم الإندونيسية
     * يفتح قسم «القواعد» فيجد سطراً واحداً — رغم أن الدروس نفسها تشرح
     * النفي والملكية والأزمنة والبادئات. القواعد كانت محبوسة داخل
     * الدروس ولا يمكن الرجوع إليها كمرجع مستقل.
     *
     * كل قاعدة هنا مستخلصة مما تشرحه الدروس فعلاً (لا محتوى جديد
     * مخترع)، ومصاغة بالبنية: شرح ← قاعدة مختصرة ← أمثلة مفكَّكة.
     *
     * نطاق المعرّفات: 3300+ (كتلة فارغة، لا تصادم مع 1 أو 2001–2005).
     */
    val indonesianGrammar: List<GrammarEntity> = listOf(
        GrammarEntity(3300, "النفي: tidak و bukan", "Negasi",
            "الإندونيسية تفرّق بين أداتي نفي حسب المنفي: tidak تنفي الفعل والصفة، " +
            "وbukan تنفي الاسم. الخلط بينهما من أشيع أخطاء المبتدئين.",
            "tidak + فعل/صفة  •  bukan + اسم",
            "Saya tidak makan. = أنا لا آكل. (فعل)\n" +
            "Saya tidak lapar. = لست جائعاً. (صفة)\n" +
            "Ini bukan buku saya. = هذا ليس كتابي. (اسم)\n" +
            "Dia bukan guru. = هو ليس مدرساً. (اسم)", 0, "ID"),

        GrammarEntity(3301, "belum و jangan", "Belum & Jangan",
            "belum تعني «لم... بعد» وتفيد أن الفعل لم يحدث حتى الآن لكنه متوقع. " +
            "وjangan للنهي (لا تفعل)، ولا تُستعمل tidak للنهي إطلاقاً.",
            "belum = لم بعد  •  jangan + فعل = لا تفعل",
            "Saya belum makan. = لم آكل بعد.\n" +
            "Dia belum datang. = لم يأتِ بعد.\n" +
            "Jangan lari! = لا تركض!\n" +
            "Jangan lupa. = لا تنسَ.", 0, "ID"),

        GrammarEntity(3302, "الملكية", "Kepemilikan",
            "الملكية في الإندونيسية تأتي بوضع المالك **بعد** الشيء المملوك، " +
            "عكس العربية في الترتيب الذهني. وتوجد صيغة مختصرة بلواحق.",
            "اسم + ضمير المالك  •  المختصر: -ku, -mu, -nya",
            "rumah saya = بيتي\n" +
            "buku kamu = كتابك\n" +
            "mobil dia = سيارته\n" +
            "bukuku = كتابي (مختصر)\n" +
            "bukunya = كتابه (مختصر)", 0, "ID"),

        GrammarEntity(3303, "أدوات الاستفهام", "Kata Tanya",
            "أدوات السؤال تأتي غالباً في أول الجملة أو آخرها، والجملة تبقى " +
            "بترتيبها الطبيعي دون قلب كما في الإنجليزية.",
            "apa / siapa / di mana / ke mana / berapa / kapan / kenapa / bagaimana",
            "Apa ini? = ما هذا؟\n" +
            "Siapa nama kamu? = ما اسمك؟\n" +
            "Di mana rumah kamu? = أين بيتك؟\n" +
            "Berapa harganya? = كم سعره؟\n" +
            "Kenapa kamu terlambat? = لماذا تأخرت؟", 0, "ID"),

        GrammarEntity(3304, "الزمن: sudah / sedang / akan / belum", "Waktu",
            "الفعل الإندونيسي **لا يتصرف** حسب الزمن — لا ماضي ولا مضارع في " +
            "شكل الفعل. الزمن يُفهم من كلمة مساعدة قبل الفعل. هذه أسهل نقطة " +
            "في الإندونيسية للناطق بالعربية.",
            "sudah = قد فعل  •  sedang = يفعل الآن  •  akan = سيفعل  •  belum = لم بعد",
            "Saya sudah makan. = قد أكلت.\n" +
            "Saya sedang makan. = أنا آكل الآن.\n" +
            "Saya akan makan. = سآكل.\n" +
            "Saya belum makan. = لم آكل بعد.", 1, "ID"),

        GrammarEntity(3305, "الصفة بعد الاسم", "Kata Sifat",
            "الصفة تأتي **بعد** الموصوف كما في العربية تماماً، ولا يوجد " +
            "تطابق في الجنس أو العدد.",
            "اسم + صفة",
            "rumah besar = بيت كبير\n" +
            "buku baru = كتاب جديد\n" +
            "Harganya mahal. = سعره غالٍ.\n" +
            "Kopi panas. = قهوة ساخنة.", 0, "ID"),

        GrammarEntity(3306, "المقارنة والتفضيل", "Perbandingan",
            "المقارنة تُبنى بكلمات مساعدة قبل الصفة، ولا تتغير الصفة نفسها.",
            "lebih = أكثر  •  paling = الأكثر  •  sangat = جداً  •  terlalu = أكثر من اللازم",
            "Ini lebih murah. = هذا أرخص.\n" +
            "Ini paling murah. = هذا الأرخص.\n" +
            "Ini sangat murah. = هذا رخيص جداً.\n" +
            "Ini terlalu mahal. = هذا غالٍ أكثر من اللازم.", 1, "ID"),

        GrammarEntity(3307, "حروف الجر: di / ke / dari", "Preposisi",
            "ثلاثة حروف تحدد المكان والحركة: di للمكان الثابت، ke للاتجاه إليه، " +
            "dari للمصدر الذي جئت منه.",
            "di = في  •  ke = إلى  •  dari = من",
            "Saya di rumah. = أنا في البيت.\n" +
            "Saya pergi ke pasar. = أذهب إلى السوق.\n" +
            "Saya dari Yaman. = أنا من اليمن.", 0, "ID"),

        GrammarEntity(3308, "البادئة meN-", "Awalan meN-",
            "بادئة تُشتق منها أفعال من الجذور، ويتغيّر شكلها حسب أول حرف في " +
            "الجذر — وهذا سبب اختلاف الشكل بين membeli و menulis و menyapu.",
            "meN- + جذر، ويتبدّل الحرف الأول صوتياً",
            "beli → membeli = يشتري\n" +
            "tulis → menulis = يكتب\n" +
            "pakai → memakai = يستعمل\n" +
            "sapu → menyapu = يكنس", 1, "ID"),

        GrammarEntity(3309, "المبني للمجهول di-", "Kalimat Pasif",
            "تحويل الجملة من نشطة إلى مبنية للمجهول يتم باستبدال البادئة " +
            "meN- بالبادئة di-، وتقديم المفعول ليصبح مبتدأ.",
            "di- + جذر (+ oleh + الفاعل)",
            "Saya membeli buku. = أنا أشتري كتاباً. (نشطة)\n" +
            "Buku dibeli oleh saya. = الكتاب اشتُري بواسطتي. (مجهولة)", 1, "ID"),

        GrammarEntity(3310, "أدوات الربط", "Kata Hubung",
            "ربط الجمل يحوّل الكلام من جمل قصيرة مبعثرة إلى حديث طبيعي متصل.",
            "dan = و  •  tetapi/tapi = لكن  •  karena = لأن  •  jadi = لذلك  •  kalau = إذا",
            "Saya lapar, jadi saya makan. = أنا جائع، لذلك آكل.\n" +
            "Saya tidak lapar, tetapi saya makan. = لست جائعاً، لكني آكل.\n" +
            "Kalau hujan, saya di rumah. = إذا أمطرت، أبقى في البيت.", 1, "ID"),
    )

    /** كل المفردات الأساسية المضافة (إندونيسية + تركية). */
    val all: List<VocabularyEntity> =
        indonesianDays + indonesianNumbers + indonesianPronouns +
        indonesianColors + indonesianMoney + indonesianDirections +
        indonesianBody + indonesianClothes + turkishCore
}
