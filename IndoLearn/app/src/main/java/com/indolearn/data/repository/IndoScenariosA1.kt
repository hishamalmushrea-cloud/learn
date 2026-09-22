package com.indolearn.data.repository

import com.indolearn.data.local.entity.DailyScenarioEntity
import com.indolearn.data.local.entity.TrainingItemEntity

/**
 * سيناريوهات إندونيسية عملية كانت ناقصة مقابل التركية:
 * صحة، صيدلية، سفر، هاتف، عمل، طوارئ.
 * حوارات قصيرة، ترجمة سطرية، بلا وعد طلاقة.
 */
object IndoScenariosA1 {
    val scenarios = listOf(
        DailyScenarioEntity(
            800, "Dokter", "عند الطبيب",
            "Dokter: Selamat pagi. Sakit apa?\nPasien: Kepala saya sakit sejak kemarin.\nDokter: Demam?\nPasien: Sedikit.\nDokter: Minum obat ini sesudah makan.",
            "الطبيب: صباح الخير. ما الذي يؤلمك؟\nالمريض: رأسي يؤلمني منذ أمس.\nالطبيب: ألديك حمى؟\nالمريض: قليلاً.\nالطبيب: اشرب هذا الدواء بعد الأكل.",
            0, "صحة"
        ),
        DailyScenarioEntity(
            801, "Apotek", "في الصيدلية",
            "Petugas: Selamat siang. Butuh apa?\nPembeli: Ada obat sakit perut?\nPetugas: Ini. Diminum sesudah makan.\nPembeli: Berapa harganya?\nPetugas: Dua puluh ribu.",
            "الموظف: مساء الخير. ماذا تحتاج؟\nالمشتري: هل يوجد دواء لألم المعدة؟\nالموظف: هذا. يُشرب بعد الأكل.\nالمشتري: كم سعره؟\nالموظف: عشرون ألفاً.",
            0, "صحة"
        ),
        DailyScenarioEntity(
            802, "Darurat", "طلب مساعدة عاجلة",
            "A: Tolong! Ada yang jatuh.\nB: Di mana?\nA: Di depan toko itu.\nB: Saya panggil bantuan.\nA: Terima kasih.",
            "أ: النجدة! شخص سقط.\nب: أين؟\nأ: أمام ذلك الدكان.\nب: سأطلب المساعدة.\nأ: شكراً.",
            0, "طوارئ"
        ),
        DailyScenarioEntity(
            803, "Tiket", "شراء تذكرة",
            "Pembeli: Satu tiket ke Surabaya, siang ini.\nPetugas: Kereta jam tiga. Nama?\nPembeli: Ahmad.\nPetugas: Bayar tunai atau kartu?\nPembeli: Kartu.",
            "المشتري: تذكرة واحدة إلى سورابايا، هذا الظهر.\nالموظف: القطار الساعة الثالثة. الاسم؟\nالمشتري: أحمد.\nالموظف: نقداً أم بطاقة؟\nالمشتري: بطاقة.",
            0, "سفر"
        ),
        DailyScenarioEntity(
            804, "Arah", "السؤال عن الطريق",
            "A: Permisi, stasiun di mana?\nB: Lurus, lalu belok kiri.\nA: Jauh?\nB: Lima menit jalan kaki.\nA: Terima kasih banyak.",
            "أ: عفواً، أين المحطة؟\nب: على طول، ثم انعطف يساراً.\nأ: أبعيد؟\nب: خمس دقائق مشياً.\nأ: شكراً جزيلاً.",
            0, "سفر"
        ),
        DailyScenarioEntity(
            805, "Hotel", "في الفندق",
            "Tamu: Saya sudah pesan kamar.\nResepsionis: Nama?\nTamu: Siti.\nResepsionis: Kamar dua satu. Sarapan jam tujuh.\nTamu: Terima kasih.",
            "الضيف: لقد حجزت غرفة.\nالاستقبال: الاسم؟\nالضيف: سيتي.\nالاستقبال: الغرفة واحد وعشرون. الإفطار الساعة السابعة.\nالضيف: شكراً.",
            0, "سفر"
        ),
        DailyScenarioEntity(
            806, "Telepon", "مكالمة لم تُفهم",
            "A: Halo, saya Ahmad.\nB: Halo Ahmad. Maaf, sinyal kurang bagus.\nA: Bisa diulang pelan?\nB: Saya dari kantor. Besok rapat jam sembilan.\nA: Baik, saya datang.",
            "أ: مرحباً، أنا أحمد.\nب: مرحباً أحمد. عذراً، الإشارة ضعيفة.\nأ: هل تعيد ببطء؟\nب: أنا من المكتب. غداً اجتماع الساعة التاسعة.\nأ: حسناً، سأحضر.",
            0, "هاتف"
        ),
        DailyScenarioEntity(
            807, "Kuota", "مشكلة بيانات",
            "Pembeli: Pulsa saya habis.\nPetugas: Mau isi berapa?\nPembeli: Lima puluh ribu.\nPetugas: Sudah masuk.\nPembeli: Internet masih tidak jalan.\nPetugas: Coba matikan data, lalu nyalakan lagi.",
            "المشتري: رصيدي انتهى.\nالموظف: كم تريد أن تشحن؟\nالمشتري: خمسون ألفاً.\nالموظف: دخل الرصيد.\nالمشتري: الإنترنت ما زال لا يعمل.\nالموظف: أوقف البيانات ثم شغّلها مرة أخرى.",
            0, "هاتف"
        ),
        DailyScenarioEntity(
            808, "Kantor", "تأخير عن العمل",
            "Pegawai: Selamat pagi. Maaf, saya terlambat.\nAtasan: Ada apa?\nPegawai: Macet parah.\nAtasan: Lain kali kabari dulu.\nPegawai: Baik.",
            "الموظف: صباح الخير. عذراً، تأخرت.\nالمسؤول: ماذا حدث؟\nالموظف: ازدحام شديد.\nالمسؤول: في المرة القادمة أخبرني أولاً.\nالموظف: حسناً.",
            0, "عمل"
        ),
        DailyScenarioEntity(
            809, "Janji", "تحديد موعد",
            "A: Bisa ketemu besok?\nB: Jam berapa?\nA: Jam empat sore, di kantor.\nB: Boleh. Saya datang.\nA: Terima kasih.",
            "أ: هل نلتقي غداً؟\nب: في أي ساعة؟\nأ: الساعة الرابعة عصراً، في المكتب.\nب: يمكن. سأحضر.\nأ: شكراً.",
            0, "عمل"
        ),
        DailyScenarioEntity(
            810, "Tidak paham", "لم أفهم الجملة",
            "A: Maaf, saya belum mengerti.\nB: Bagian mana?\nA: Kata itu. Artinya apa?\nB: Artinya 'resep'. Kertas dari dokter.\nA: Oh, mengerti. Tolong bicara pelan.",
            "أ: عذراً، لم أفهم بعد.\nب: أي جزء؟\nأ: تلك الكلمة. ما معناها؟\nب: معناها «وصفة». ورقة من الطبيب.\nأ: فهمت. تكلم ببطء من فضلك.",
            0, "عام"
        ),
        DailyScenarioEntity(
            811, "Ojek", "طلب توصيلة",
            "Penumpang: Ke pasar Senen, berapa?\nPengemudi: Dua puluh lima ribu.\nPenumpang: Bisa dua puluh?\nPengemudi: Baik, naik.\nPenumpang: Tolong pelan, ya.",
            "الراكب: إلى سوق سنين، بكم؟\nالسائق: خمسة وعشرون ألفاً.\nالراكب: هل يمكن عشرون؟\nالسائق: حسناً، اركب.\nالراكب: على مهل من فضلك.",
            0, "سفر"
        )
    )

    val quizzes = listOf(
        TrainingItemEntity(850, "SITUATION", "رأسك يؤلمك عند الطبيب. ماذا تقول؟", "Kepala saya sakit.",
            "Kepala saya sakit.,Saya lapar.,Ini mahal.,Saya pulang.",
            "sakit = يؤلم / مريض", "صحة"),
        TrainingItemEntity(851, "SITUATION", "تريد دواء ألم المعدة في الصيدلية.", "Ada obat sakit perut?",
            "Ada obat sakit perut?,Jam berapa?,Saya terlambat.,Satu tiket.",
            "perut = بطن / معدة", "صحة"),
        TrainingItemEntity(852, "SITUATION", "لم تفهم المكالمة بسبب الإشارة.", "Bisa diulang pelan?",
            "Bisa diulang pelan?,Saya sudah makan.,Ini kamar saya.,Belok kiri.",
            "diulang = يُعاد · pelan = ببطء", "هاتف"),
        TrainingItemEntity(853, "SITUATION", "تسأل عن المحطة في الشارع.", "Stasiun di mana?",
            "Stasiun di mana?,Saya terlambat.,Pulsa habis.,Minum obat.",
            "di mana للمكان الثابت", "سفر"),
        TrainingItemEntity(854, "MULTIPLE_CHOICE", "Minum obat ini sesudah makan =", "اشرب هذا الدواء بعد الأكل",
            "اشرب هذا الدواء قبل النوم,اشرب هذا الدواء بعد الأكل,لا تشرب الدواء,الدواء غالي",
            "sesudah = بعد", "صحة"),
        TrainingItemEntity(855, "MULTIPLE_CHOICE", "Saya terlambat تعني:", "تأخرت",
            "أنا مريض,تأخرت,أنا جائع,أنا فاهم",
            "terlambat = متأخر", "عمل"),
        TrainingItemEntity(856, "SITUATION", "شخص سقط وتطلب مساعدة.", "Tolong!",
            "Tolong!,Murah banget!,Selamat tidur.,Saya pesan kamar.",
            "Tolong = النجدة / ساعدني", "طوارئ"),
        TrainingItemEntity(857, "TRANSLATE", "هل نلتقي غداً؟", "Bisa ketemu besok?",
            "Bisa ketemu besok?,Saya terlambat.,Pulsa habis.,Belok kiri.",
            "ketemu = يلتقي · besok = غداً", "عمل")
    )
}
