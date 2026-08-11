package com.indolearn.utils

data class VerbDetail(
    val breakdown: String,
    val related: List<String>,
    val question: String,
    val options: List<String>,
    val answer: String
)

object VerbDetailsHelper {
    private val verbDetailsMap = mapOf(
        "makan" to VerbDetail(
            breakdown = "Saya = أنا • makan = آكل • nasi = أرز",
            related = listOf("minum (يشرب)", "lapar (جائع)", "nasi goreng (أرز مقلي)"),
            question = "Saya ___ nasi setiap hari.",
            options = listOf("makan", "minum"),
            answer = "makan"
        ),
        "minum" to VerbDetail(
            breakdown = "Saya = أنا • minum = أشرب • air = ماء",
            related = listOf("makan (يأكل)", "teh (شاي)", "kopi (قهوة)"),
            question = "Saya ___ kopi di pagi hari.",
            options = listOf("minum", "tidur"),
            answer = "minum"
        ),
        "tidur" to VerbDetail(
            breakdown = "Saya = أنا • mau = أريد • tidur = أنام",
            related = listOf("bangun (يستيقظ)", "malam (ليل)", "capek (متعب)"),
            question = "Saya capek, saya mau ___.",
            options = listOf("tidur", "lari"),
            answer = "tidur"
        ),
        "bangun" to VerbDetail(
            breakdown = "Saya = أنا • bangun = أستيقظ • pagi = صباحاً",
            related = listOf("tidur (ينام)", "pagi (صباح)", "jam (ساعة)"),
            question = "Saya ___ pagi untuk bekerja.",
            options = listOf("bangun", "tidur"),
            answer = "bangun"
        ),
        "pergi" to VerbDetail(
            breakdown = "Kami = نحن • pergi = نذهب • ke = إلى • sekolah = مدرسة",
            related = listOf("datang (يأتي)", "pulang (يعود)", "jalan (يمشي)"),
            question = "Kami ___ ke sekolah setiap hari.",
            options = listOf("pergi", "duduk"),
            answer = "pergi"
        ),
        "pulang" to VerbDetail(
            breakdown = "Saya = أنا • pulang = أعود للبيت • jam = ساعة • lima = خمسة",
            related = listOf("pergi (يذهب)", "rumah (منزل)", "sore (عصرًا)"),
            question = "Saya ___ ke rumah sekarang.",
            options = listOf("pulang", "datang"),
            answer = "pulang"
        ),
        "datang" to VerbDetail(
            breakdown = "Teman = صديق • saya = لي • datang = يأتي",
            related = listOf("pergi (يذهب)", "tunggu (ينتظر)", "jam (ساعة)"),
            question = "Dia ___ ke sini besok.",
            options = listOf("datang", "lari"),
            answer = "datang"
        ),
        "lihat" to VerbDetail(
            breakdown = "Saya = أنا • lihat = أرى • burung = طائر",
            related = listOf("dengar (يسمع)", "mata (عين)", "lihat-lihat (يتفرج)"),
            question = "Saya ___ burung terbang di langit.",
            options = listOf("lihat", "dengar"),
            answer = "lihat"
        ),
        "dengar" to VerbDetail(
            breakdown = "Saya = أنا • dengar = أسمع • radio = راديو",
            related = listOf("lihat (يرى)", "musik (موسيقى)", "telinga (أذن)"),
            question = "Saya ___ musik di kamar.",
            options = listOf("dengar", "masak"),
            answer = "dengar"
        ),
        "bicara" to VerbDetail(
            breakdown = "Saya = أنا • bicara = أتحدث • bahasa = لغة",
            related = listOf("ngobrol (يتحدث ودّيًا)", "tanya (يسأل)", "jawab (يجيب)"),
            question = "Saya bisa ___ bahasa Indonesia.",
            options = listOf("bicara", "tulis"),
            answer = "bicara"
        ),
        "baca" to VerbDetail(
            breakdown = "Saya = أنا • baca = أقرأ • buku = كتاب",
            related = listOf("tulis (يكتب)", "buku (كتاب)", "koran (جريدة)"),
            question = "Saya ___ buku di perpustakaan.",
            options = listOf("baca", "makan"),
            answer = "baca"
        ),
        "tulis" to VerbDetail(
            breakdown = "Saya = أنا • tulis = أكتب • surat = رسالة",
            related = listOf("baca (يقرأ)", "pensil (قلم)", "nama (اسم)"),
            question = "Tolong ___ nama kamu di sini.",
            options = listOf("tulis", "baca"),
            answer = "tulis"
        ),
        "belajar" to VerbDetail(
            breakdown = "Saya = أنا • belajar = أتعلم • bahasa = لغة",
            related = listOf("mengajar (يعلّم)", "sekolah (مدرسة)", "bahasa (لغة)"),
            question = "Saya ___ bahasa Indonesia setiap malam.",
            options = listOf("belajar", "tidur"),
            answer = "belajar"
        ),
        "mengajar" to VerbDetail(
            breakdown = "Guru = معلم • mengajar = يعلّم • murid = تلميذ",
            related = listOf("belajar (يتعلم)", "guru (معلم)", "sekolah (مدرسة)"),
            question = "Guru ___ murid-murid di kelas.",
            options = listOf("mengajar", "belajar"),
            answer = "mengajar"
        ),
        "kerja" to VerbDetail(
            breakdown = "Ayah = أب • kerja = يعمل • di = في • kantor = مكتب",
            related = listOf("bekerja (يعمل)", "kantor (مكتب)", "gaji (راتب)"),
            question = "Saya ___ di perusahaan besar.",
            options = listOf("kerja", "main"),
            answer = "kerja"
        ),
        "main" to VerbDetail(
            breakdown = "Anak-anak = أطفال • main = يلعبون • bola = كرة",
            related = listOf("bola (كرة)", "teman (صديق)", "bermain (يلعب)"),
            question = "Anak-anak ___ bola di lapangan.",
            options = listOf("main", "kerja"),
            answer = "main"
        ),
        "beli" to VerbDetail(
            breakdown = "Saya = أنا • mau = أريد • beli = أشتري • ini = هذا",
            related = listOf("jual (يبيع)", "harga (سعر)", "bayar (يدفع)", "toko (متجر)"),
            question = "Saya mau ___ buah segar di pasar.",
            options = listOf("beli", "jual"),
            answer = "beli"
        ),
        "jual" to VerbDetail(
            breakdown = "Pedagang = تاجر • jual = يبيع • buah = فاكهة",
            related = listOf("beli (يشتري)", "harga (سعر)", "untung (ربح)"),
            question = "Pedagang itu ___ buah di pasar tradisional.",
            options = listOf("jual", "beli"),
            answer = "jual"
        ),
        "bayar" to VerbDetail(
            breakdown = "Saya = أنا • bayar = أدفع • harga = الثمن",
            related = listOf("beli (يشتري)", "uang (نقود)", "tunai (نقدًا)"),
            question = "Saya ___ belanjaan di kasir.",
            options = listOf("bayar", "ambil"),
            answer = "bayar"
        ),
        "terima" to VerbDetail(
            breakdown = "Saya = أنا • terima = أستلم • hadiah = هدية",
            related = listOf("beri (يعطي)", "hadiah (هدية)", "terima kasih (شكرًا)"),
            question = "Saya ___ hadiah ulang tahun.",
            options = listOf("terima", "kirim"),
            answer = "terima"
        ),
        "beri" to VerbDetail(
            breakdown = "Saya = أنا • beri = أعطي • kamu = إياك • bunga = زهرة",
            related = listOf("terima (يستلم)", "kasih (يعطي)", "hadiah (هدية)"),
            question = "Saya ___ kamu uang jajan.",
            options = listOf("beri", "terima"),
            answer = "beri"
        ),
        "ambil" to VerbDetail(
            breakdown = "Tolong = من فضلك • ambil = خذ • buku = كتاب • itu = ذلك",
            related = listOf("bawa (يحمل)", "taruh (يضع)", "beri (يعطي)"),
            question = "Tolong ___ buku merah di atas meja.",
            options = listOf("ambil", "taruh"),
            answer = "ambil"
        ),
        "bawa" to VerbDetail(
            breakdown = "Dia = هو • bawa = يحمل • tas = حقيبة",
            related = listOf("ambil (يأخذ)", "tas (حقيبة)", "kirim (يرسل)"),
            question = "Saya ___ payung karena hujan.",
            options = listOf("bawa", "buang"),
            answer = "bawa"
        ),
        "kirim" to VerbDetail(
            breakdown = "Saya = أنا • kirim = أرسل • email = بريد إلكتروني",
            related = listOf("terima (يستلم)", "pesan (رسالة)", "alamat (عنوان)"),
            question = "Saya ___ surat ke keluarga di Yaman.",
            options = listOf("kirim", "bawa"),
            answer = "kirim"
        ),
        "tunggu" to VerbDetail(
            breakdown = "Saya = أنا • tunggu = أنتظر • kamu = إياك",
            related = listOf("sebentar (لحظة)", "nanti (لاحقًا)", "datang (يأتي)"),
            question = "Tolong ___ sebentar ya.",
            options = listOf("tunggu", "pergi"),
            answer = "tunggu"
        ),
        "cari" to VerbDetail(
            breakdown = "Saya = أنا • cari = أبحث عن • kunci = مفتاح",
            related = listOf("temukan (يجد)", "kunci (مفتاح)", "dompet (محفظة)"),
            question = "Saya ___ dompet saya yang hilang.",
            options = listOf("cari", "temukan"),
            answer = "cari"
        ),
        "temukan" to VerbDetail(
            breakdown = "Saya = أنا • temukan = أجد • dompet = محفظة",
            related = listOf("cari (يبحث)", "kehilangan (يفقد)", "dompet (محفظة)"),
            question = "Akhirnya saya ___ kunci di bawah meja.",
            options = listOf("temukan", "cari"),
            answer = "temukan"
        ),
        "duduk" to VerbDetail(
            breakdown = "Mari = هيا بنا • duduk = نجلس • di = في • sini = هنا",
            related = listOf("berdiri (يقف)", "kursi (كرسي)", "santai (يسترخي)"),
            question = "Silakan ___ di kursi ini.",
            options = listOf("duduk", "berdiri"),
            answer = "duduk"
        ),
        "berdiri" to VerbDetail(
            breakdown = "Murid = تلميذ • berdiri = يقف",
            related = listOf("duduk (يجلس)", "berjalan (يمشي)"),
            question = "Semua murid ___ ketika guru masuk.",
            options = listOf("berdiri", "duduk"),
            answer = "berdiri"
        ),
        "jalan" to VerbDetail(
            breakdown = "Saya = أنا • jalan = أمشي • ke = إلى • pasar = سوق",
            related = listOf("lari (يركض)", "pasar (سوق)", "jalan kaki (مشيًا)"),
            question = "Saya ___ kaki ke toko terdekat.",
            options = listOf("jalan", "lari"),
            answer = "jalan"
        )
    )

    fun getDetailFor(indonesianWord: String): VerbDetail? {
        return verbDetailsMap[indonesianWord.lowercase().trim()]
    }
}
