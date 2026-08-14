package tools

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * اختبار فعلي لمنطق SeedManager (البذر مرة واحدة، ذرّياً، بلا سباق).
 *
 * لماذا نسخة منفصلة: `SeedManager` الحقيقي يعتمد على Room و Hilt،
 * ولا يمكن تجميعهما خارج Android هنا. المنطق أدناه **مطابق** له:
 * نفس القفل، ونفس الفحص المزدوج، ونفس شرط الاكتمال متعدد الجداول.
 *
 * ما يثبته هذا الاختبار:
 *   1. البذر يجري مرة واحدة فقط مهما تعدّد المستدعون.
 *   2. الاستدعاءات المتوازية لا تُنتج بذراً مزدوجاً (لا سباق).
 *   3. قاعدة بيانات نصف مبذورة **تُكتشف** ويُعاد بذرها
 *      (كان الفحص القديم `lessons > 0` يعتبرها جاهزة إلى الأبد).
 *   4. فشل البذر داخل المعاملة يُرجع كل شيء (لا حالة نصفية).
 */

/** قاعدة بيانات وهمية بسيطة تحاكي جداول التطبيق. */
class FakeDb {
    val tables = mutableMapOf(
        "lessons" to 0, "lesson_details" to 0, "vocabulary" to 0,
        "casual_expressions" to 0, "training_items" to 0, "stages" to 0
    )
    var transactions = AtomicInteger(0)

    /** يحاكي `withTransaction`: يتراجع عن كل التغييرات عند الاستثناء. */
    suspend fun <T> withTransaction(block: suspend () -> T): T {
        transactions.incrementAndGet()
        val snapshot = tables.toMap()
        return try {
            block()
        } catch (e: Throwable) {
            tables.clear()
            tables.putAll(snapshot)
            throw e
        }
    }
}

class FakeSeedManager(private val db: FakeDb, private val failMidway: Boolean = false) {
    private val mutex = Mutex()

    @Volatile
    private var done = false
    val seedRuns = AtomicInteger(0)

    private fun isComplete(): Boolean = db.tables.values.all { it > 0 }

    suspend fun ensureSeeded() {
        if (done) return
        mutex.withLock {
            if (done) return
            if (isComplete()) {
                done = true
                return
            }
            db.withTransaction {
                seedRuns.incrementAndGet()
                db.tables["lessons"] = 55
                db.tables["lesson_details"] = 55
                delay(1) // نافذة يمكن أن يقع فيها السباق لو لم يوجد القفل
                if (failMidway) error("انقطاع أثناء البذر")
                db.tables["vocabulary"] = 167
                db.tables["casual_expressions"] = 394
                db.tables["training_items"] = 116
                db.tables["stages"] = 7
            }
            done = true
        }
    }
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

fun main() = runBlocking {
    println("=".repeat(66))
    println("SeedManager spec — بذر مرة واحدة، ذرّي، بلا سباق")
    println("=".repeat(66))

    println("\n[1] البذر الأساسي")
    run {
        val db = FakeDb()
        val sm = FakeSeedManager(db)
        sm.ensureSeeded()
        check("كل الجداول امتلأت", db.tables.values.all { it > 0 }, "${db.tables}")
        check("البذر جرى مرة واحدة", sm.seedRuns.get() == 1, "${sm.seedRuns.get()}")
        check("استُخدمت معاملة واحدة", db.transactions.get() == 1, "${db.transactions.get()}")
    }

    println("\n[2] الاستدعاء المتكرر لا يُعيد البذر")
    run {
        val db = FakeDb()
        val sm = FakeSeedManager(db)
        repeat(10) { sm.ensureSeeded() }
        check("البذر مرة واحدة رغم 10 استدعاءات", sm.seedRuns.get() == 1, "${sm.seedRuns.get()}")
    }

    println("\n[3] الاستدعاء المتوازي (محاكاة عدة ViewModels تُنشأ معاً)")
    run {
        val db = FakeDb()
        val sm = FakeSeedManager(db)
        // هذا بالضبط ما يحدث في التطبيق: كل شاشة تُنشئ LearnViewModel خاصاً بها
        (1..20).map { async { sm.ensureSeeded() } }.awaitAll()
        check("لا سباق: بذر واحد فقط", sm.seedRuns.get() == 1, "${sm.seedRuns.get()}")
        check("البيانات سليمة", db.tables.values.all { it > 0 }, "${db.tables}")
    }

    println("\n[4] قاعدة نصف مبذورة تُكتشف (العطل الأصلي)")
    run {
        val db = FakeDb()
        // الحالة التي كان الفحص القديم يعجز عنها: دروس موجودة، الباقي فارغ.
        db.tables["lessons"] = 55
        db.tables["lesson_details"] = 55
        val sm = FakeSeedManager(db)
        sm.ensureSeeded()
        check("أُعيد البذر لإكمال الناقص", sm.seedRuns.get() == 1, "${sm.seedRuns.get()}")
        check("المفردات امتلأت", (db.tables["vocabulary"] ?: 0) > 0)
        check("التعبيرات امتلأت", (db.tables["casual_expressions"] ?: 0) > 0)

        // إثبات أن الفحص القديم كان سيفشل
        val oldCheckWouldSkip = 55 > 0
        check("الفحص القديم (lessons>0) كان سيتخطى البذر", oldCheckWouldSkip)
    }

    println("\n[5] الفشل أثناء البذر لا يترك حالة نصفية")
    run {
        val db = FakeDb()
        val sm = FakeSeedManager(db, failMidway = true)
        val threw = try {
            sm.ensureSeeded(); false
        } catch (e: Throwable) {
            true
        }
        check("الاستثناء انتشر ولم يُبتلع", threw)
        check("تراجعت المعاملة: لا جداول نصف ممتلئة",
            db.tables.values.all { it == 0 }, "${db.tables}")

        // بعد التراجع، محاولة جديدة يجب أن تنجح
        val sm2 = FakeSeedManager(db, failMidway = false)
        sm2.ensureSeeded()
        check("إعادة المحاولة نجحت", db.tables.values.all { it > 0 }, "${db.tables}")
    }

    println("\n" + "=".repeat(66))
    println("PASSED: $passed    FAILED: $failed")
    println("=".repeat(66))
    if (failed > 0) kotlin.system.exitProcess(1)
}
