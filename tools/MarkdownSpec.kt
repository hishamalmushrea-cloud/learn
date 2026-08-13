package tools

import java.io.File

/**
 * اختبار فعلي لمحلل Markdown المستخدم في المكتبة المرجعية،
 * مُشغَّل على ملفات الموسوعة الحقيقية (27 ملفاً) لا على أمثلة مصطنعة.
 *
 * لماذا نسخة منفصلة: `LibraryRepository` يعتمد على `android.content.Context`
 * فلا يمكن تجميعه خارج Android في هذه البيئة. المنطق أدناه **مطابق نصياً**
 * لدالة `parseMarkdown` هناك؛ أي تعديل في أحدهما يجب أن ينعكس في الآخر.
 *
 * التشغيل: tools/run_markdown_spec.sh
 */

sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullet(val text: String) : MdBlock
    data class Table(val header: List<String>, val rows: List<List<String>>) : MdBlock
    data object Divider : MdBlock
}

private fun stripInline(s: String) =
    s.replace("**", "").replace("`", "").replace("*", "").trim()

private fun splitRow(line: String): List<String> =
    line.trim().trim('|').split("|").map { stripInline(it.trim()) }

fun parseMarkdown(text: String): List<MdBlock> {
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

private var passed = 0
private var failed = 0

private fun check(name: String, cond: Boolean, detail: String = "") {
    if (cond) {
        passed++; println("  ✓ $name")
    } else {
        failed++; println("  ✗ $name ${if (detail.isNotEmpty()) "→ $detail" else ""}")
    }
}

fun main(args: Array<String>) {
    val root = File(args.firstOrNull() ?: "IndoLearn/app/src/main/assets/library")
    println("=".repeat(66))
    println("Markdown parser spec — real encyclopedia files")
    println("=".repeat(66))

    // المكتبة صارت مقسّمة حسب اللغة: library/id و library/tr
    val langDirs = root.listFiles { f: File -> f.isDirectory }?.sortedBy { it.name }.orEmpty()
    val files = langDirs.flatMap { d ->
        d.listFiles { f: File -> f.name.endsWith(".md") }?.sortedBy { it.name }?.toList().orEmpty()
    }
    if (files.isEmpty()) {
        println("✗ no markdown files found under ${root.absolutePath}")
        kotlin.system.exitProcess(1)
    }

    println("\n[files] ${files.size} documents in ${langDirs.size} language folders")
    check("both language folders present", langDirs.size == 2,
        langDirs.joinToString { it.name })
    for (d in langDirs) {
        val n = d.listFiles { f: File -> f.name.endsWith(".md") }?.size ?: 0
        check("library/${d.name} is not empty", n > 0, "$n files")
    }
    var totalBlocks = 0
    var totalTables = 0
    var totalRows = 0
    var raggedTables = 0
    var emptyDocs = 0

    for (f in files) {
        val blocks = parseMarkdown(f.readText())
        totalBlocks += blocks.size
        if (blocks.isEmpty()) emptyDocs++
        val tables = blocks.filterIsInstance<MdBlock.Table>()
        totalTables += tables.size
        for (t in tables) {
            totalRows += t.rows.size
            // صف أعرض من الترويسة يعني أن التقسيم أخطأ
            if (t.rows.any { it.size > t.header.size }) raggedTables++
        }
    }

    check("every document parses into blocks", emptyDocs == 0, "$emptyDocs empty")
    check("tables were detected", totalTables > 110, "found $totalTables")
    check("table rows extracted", totalRows > 1200, "found $totalRows")
    check("no row wider than its header", raggedTables == 0, "$raggedTables ragged")

    // كل ملف يجب أن يبدأ بعنوان — نستخدمه كاسم في قائمة المكتبة
    val noHeading = files.filter { f ->
        parseMarkdown(f.readText()).firstOrNull() !is MdBlock.Heading
    }
    check("every document starts with a heading", noHeading.isEmpty(),
        noHeading.joinToString { it.name })

    // لا يجوز أن تتسرب علامات Markdown إلى النص المعروض
    var leaked = 0
    for (f in files) {
        for (b in parseMarkdown(f.readText())) {
            val texts = when (b) {
                is MdBlock.Heading -> listOf(b.text)
                is MdBlock.Paragraph -> listOf(b.text)
                is MdBlock.Bullet -> listOf(b.text)
                is MdBlock.Table -> b.header + b.rows.flatten()
                MdBlock.Divider -> emptyList()
            }
            if (texts.any { it.contains("**") || it.contains("`") }) leaked++
        }
    }
    check("no markdown markers leak into rendered text", leaked == 0, "$leaked blocks")

    // الفواصل الأفقية يجب ألا تُفسَّر كنص
    val dividerAsText = files.sumOf { f ->
        parseMarkdown(f.readText()).count {
            it is MdBlock.Paragraph && it.text.startsWith("---")
        }
    }
    check("horizontal rules are not paragraphs", dividerAsText == 0, "$dividerAsText")

    // فحص محدد: جدول التحيات يجب أن يُقرأ بدقة
    // فحص تركي محدد: بنك الحوارات يجب أن يُقرأ سليماً
    val tr = files.firstOrNull { it.name.startsWith("bank_diyaloglar") }
    if (tr != null) {
        val b = parseMarkdown(tr.readText())
        val titles = b.filterIsInstance<MdBlock.Heading>().filter { it.level == 2 }
        check("turkish dialogue bank has 50 dialogues", titles.size == 50, "${titles.size}")
    }
    val enc = files.firstOrNull { it.name.startsWith("Turkish_Mastery") }
    if (enc != null) {
        val t = parseMarkdown(enc.readText()).filterIsInstance<MdBlock.Table>()
        check("turkish encyclopedia has tables", t.size > 15, "${t.size}")
        check("turkish tables are well formed",
            t.all { tb -> tb.rows.all { it.size <= tb.header.size } })
    }

    val greet = files.firstOrNull { it.name.startsWith("10-") }
    if (greet != null) {
        val t = parseMarkdown(greet.readText()).filterIsInstance<MdBlock.Table>()
            .firstOrNull { it.header.firstOrNull() == "العبارة" }
        check("greetings table found with expected header", t != null)
        if (t != null) {
            check("greetings table has rows", t.rows.size >= 5, "${t.rows.size}")
            check(
                "greetings row keeps all 3 columns",
                t.rows.all { it.size == t.header.size },
                t.rows.firstOrNull { it.size != t.header.size }?.toString() ?: ""
            )
            check(
                "Selamat pagi present",
                t.rows.any { it.firstOrNull()?.contains("Selamat pagi") == true }
            )
        }
    }

    println("\nstats: blocks=$totalBlocks tables=$totalTables rows=$totalRows")
    println("\n" + "=".repeat(66))
    println("PASSED: $passed    FAILED: $failed")
    println("=".repeat(66))
    if (failed > 0) kotlin.system.exitProcess(1)
}
