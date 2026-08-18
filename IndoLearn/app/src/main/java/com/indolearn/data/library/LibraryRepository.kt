package com.indolearn.data.library

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** ملف مرجعي واحد داخل المكتبة. */
data class LibraryDoc(
    val fileName: String,
    val emoji: String,
    val title: String,
    val summary: String
)

/** كتلة محتوى بعد تحليل Markdown. */
sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullet(val text: String) : MdBlock
    data class Table(val header: List<String>, val rows: List<List<String>>) : MdBlock
    data object Divider : MdBlock
}

/**
 * مصدر المكتبة المرجعية.
 *
 * يقرأ ملفات موسوعة indolang من `assets/encyclopedia/`.
 * كل شيء محلي — لا شبكة، لا قاعدة بيانات. التطبيق أوفلاين بالكامل.
 *
 * القراءة تتم على [Dispatchers.IO] لأن قراءة الأصول عمل قرصي
 * ولا يجوز تنفيذه على الخيط الرئيسي (سبب شائع لتجمّد الواجهة).
 */
class LibraryRepository(context: Context, languageCode: String) {

    // نحتفظ بسياق التطبيق لا بسياق الـ Activity، تفادياً لتسريب الـ Activity
    // إذا عاش هذا الكائن أطول من الشاشة.
    private val appContext: Context = context.applicationContext

    /**
     * مجلد المكتبة يتبع اللغة المختارة:
     *   library/id  ← موسوعة indolang  (27 ملفاً)
     *   library/tr  ← موسوعة turklang  (6 ملفات)
     * بدون هذا الفصل كان متعلم التركية يرى مرجعاً إندونيسياً.
     */
    private val dir = "library/" + if (languageCode.equals("TR", true)) "tr" else "id"

    /** رموز الملفات التركية (أسماؤها ليست مُرقَّمة). */
    private val turkishEmojis = mapOf(
        "Turkish_Mastery_Encyclopedia" to "📘",
        "pazar_dukkan_musteri" to "🛒",
        "satici_kartlar_ve_cevaplar" to "🏪",
        "bank_diyaloglar" to "🎭",
        "bank_cumleler_ve_almak" to "💬",
        "12_hafta_A0_A1" to "📅"
    )

    /** عناوين لطيفة بدل أسماء الملفات المُرقَّمة (الإندونيسية). */
    private val emojis = mapOf(
        "00" to "🧭", "01" to "🗺️", "02" to "📐", "03" to "🔊", "04" to "👤",
        "05" to "📘", "06" to "🧩", "07" to "🏃", "08" to "🎨", "09" to "📚",
        "10" to "👋", "11" to "💬", "12" to "🛒", "13" to "🍜", "14" to "🗣️",
        "15" to "⚖️", "16" to "🎭", "17" to "🎧", "18" to "🌏", "19" to "🔗",
        "20" to "🔁", "21" to "💡", "22" to "📑", "23" to "🔢", "24" to "🗂️",
        "25" to "⚡", "26" to "🏪", "27" to "🌱"
    )

    suspend fun listDocs(): List<LibraryDoc> = withContext(Dispatchers.IO) {
        val names = runCatching {
            appContext.assets.list(dir)?.filter { it.endsWith(".md") } ?: emptyList()
        }.getOrDefault(emptyList())

        names.sorted().map { name ->
            val head = runCatching { readHead(name) }.getOrDefault("" to "")
            LibraryDoc(
                fileName = name,
                emoji = turkishEmojis[name.removeSuffix(".md")]
                    ?: emojis[name.take(2)] ?: "📄",
                title = head.first.ifBlank { prettyName(name) },
                summary = head.second
            )
        }
    }

    /** يقرأ العنوان الأول وأول فقرة وصفية بلا تحميل الملف كاملاً في الذاكرة. */
    private fun readHead(name: String): Pair<String, String> {
        var title = ""
        var summary = ""
        appContext.assets.open("$dir/$name").bufferedReader().useLines { seq ->
            for (raw in seq) {
                val line = raw.trim()
                if (line.isEmpty()) continue
                if (title.isEmpty() && line.startsWith("#")) {
                    title = line.trimStart('#').trim()
                    continue
                }
                if (title.isNotEmpty() && !line.startsWith("#") && !line.startsWith("|")) {
                    summary = stripInline(line)
                    break
                }
            }
        }
        return title to summary
    }

    private fun prettyName(name: String) =
        name.removeSuffix(".md").replace('-', ' ')

    suspend fun readBlocks(name: String): List<MdBlock> = withContext(Dispatchers.IO) {
        val text = runCatching {
            appContext.assets.open("$dir/$name").bufferedReader().use { it.readText() }
        }.getOrElse { return@withContext listOf(MdBlock.Paragraph("تعذّر فتح الملف.")) }
        parseMarkdown(text)
    }

    /** بحث نصي عبر كل الموسوعة. يُعيد (الملف, السطر المطابق). */
    suspend fun search(query: String, limit: Int = 60): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            val out = mutableListOf<Pair<String, String>>()
            val names = appContext.assets.list(dir)?.filter { it.endsWith(".md") } ?: emptyList()
            for (n in names.sorted()) {
                appContext.assets.open("$dir/$n").bufferedReader().useLines { seq ->
                    for (line in seq) {
                        if (line.contains(query, ignoreCase = true)) {
                            out += n to stripInline(line.trim().trim('|').trim())
                            if (out.size >= limit) return@useLines
                        }
                    }
                }
                if (out.size >= limit) break
            }
            out
        }

    private fun stripInline(s: String) =
        s.replace("**", "").replace("`", "").replace("*", "").trim()

    /**
     * محلل Markdown مبسّط يغطي ما تستخدمه الموسوعة فعلاً:
     * العناوين، الفقرات، النقاط، الفواصل، والجداول.
     * لم نُدخل مكتبة خارجية لأن التطبيق يجب أن يبقى خفيفاً وأوفلاين.
     */
    private fun parseMarkdown(text: String): List<MdBlock> {
        val lines = text.split("\n")
        val blocks = mutableListOf<MdBlock>()
        val para = StringBuilder()

        fun flush() {
            if (para.isNotBlank()) blocks += MdBlock.Paragraph(stripInline(para.toString().trim()))
            para.clear()
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            // جدول: سطر ترويسة يليه سطر فاصل
            if (line.startsWith("|") && i + 1 < lines.size &&
                lines[i + 1].trim().let { it.contains("-") && it.all { c -> c in "|-: " } }
            ) {
                flush()
                val header = splitRow(line)
                val rows = mutableListOf<List<String>>()
                var j = i + 2
                while (j < lines.size && lines[j].trim().startsWith("|")) {
                    rows += splitRow(lines[j].trim())
                    j++
                }
                blocks += MdBlock.Table(header, rows)
                i = j
                continue
            }

            when {
                line.isEmpty() -> flush()

                line.startsWith("#") -> {
                    flush()
                    val level = line.takeWhile { it == '#' }.length
                    blocks += MdBlock.Heading(level, stripInline(line.trimStart('#').trim()))
                }

                line.startsWith("---") && line.all { it == '-' } -> {
                    flush()
                    blocks += MdBlock.Divider
                }

                line.startsWith("- ") || line.startsWith("* ") -> {
                    flush()
                    blocks += MdBlock.Bullet(stripInline(line.drop(2)))
                }

                Regex("""^\d+\.\s""").containsMatchIn(line) -> {
                    flush()
                    blocks += MdBlock.Bullet(stripInline(line))
                }

                else -> {
                    if (para.isNotEmpty()) para.append(' ')
                    para.append(line)
                }
            }
            i++
        }
        flush()
        return blocks
    }

    private fun splitRow(line: String): List<String> =
        line.trim().trim('|').split("|").map { stripInline(it.trim()) }
}
