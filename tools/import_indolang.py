#!/usr/bin/env python3
"""
مستورد محتوى موسوعة indolang إلى بيانات IndoLearn.

المصدر: https://github.com/hishamalmushrea-cloud/indolang (فرع arena/019ff99f-indolang)
27 ملف Markdown عربي يمثل منهجاً كاملاً للإندونيسية.

الاستراتيجية (طبقتان، لا فقدان للمحتوى):

  الطبقة 1 — مرجع كامل:
      تُنسخ كل ملفات .md كما هي إلى app/src/main/assets/encyclopedia/
      وتُعرض داخل التطبيق كمكتبة مرجعية قابلة للتصفح والبحث (أوفلاين).
      السبب: كثير من المحتوى نثر تعليمي وجداول غير متجانسة،
      وتحويله قسراً إلى صفوف قاعدة بيانات يُفقده معناه وسياقه.

  الطبقة 2 — محتوى قابل للتعلم:
      تُستخرج الصفوف **المنتظمة فقط** (جداول ذات ترويسة معروفة)
      وتتحول إلى CasualExpressionEntity + TrainingItemEntity،
      فتدخل محرك التكرار المتباعد والاختبارات.

لماذا لا نستخرج كل شيء؟ لأن 925 صفاً موزعة على 60+ شكل ترويسة مختلف،
واستخراجها الأعمى ينتج بيانات مشوّهة. الاستخراج هنا **محافظ ومُتحقَّق منه**:
كل صف مستخرج يجب أن يجتاز فحوص جودة قبل قبوله.

الاستخدام:
    python3 tools/import_indolang.py            # يطبع تقريراً فقط
    python3 tools/import_indolang.py --emit     # يولّد ملف Kotlin
"""

import os
import re
import sys
import glob
import unicodedata

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "IndoLearn", "app", "src", "main", "assets", "encyclopedia")
OUT_KT = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "IndoLangContent.kt",
)

REPO_KT = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "LearnRepository.kt",
)

ARABIC = re.compile(r"[\u0600-\u06FF]")


def existing_expressions():
    """العبارات المكتوبة يدوياً في البذر — لا نستوردها مرة أخرى."""
    if not os.path.exists(REPO_KT):
        return set()
    src = open(REPO_KT, encoding="utf-8").read()
    return {
        m.group(1).lower()
        for m in re.finditer(r'CasualExpressionEntity\(\d+,\s*"((?:[^"\\]|\\.)*)"', src)
    }
LATIN = re.compile(r"[A-Za-z]")

# ترويسات الجداول التي نثق بها ونعرف دلالة أعمدتها
PHRASE_HEADERS = {
    ("إندونيسية", "طبيعي", "سجل"): ("id", "ar", "reg"),
    ("إندونيسية", "طبيعي"): ("id", "ar", None),
    ("إندونيسية", "عربي", "ملاحظة"): ("id", "ar", "note"),
}
# جداول «رسمي مقابل يومي» — قيمة تعليمية عالية للسجل اللغوي
FORMAL_CASUAL_HEADERS = {
    ("رسمي", "يومي شائع", "ملاحظة"),
    ("قياسي", "يومي", "شيوع الفهم", "هل تنتجه؟"),
}


def parse_tables(text):
    """يُعيد (ترويسة, صفوف) لكل جدول Markdown في النص."""
    lines = text.split("\n")
    out, i = [], 0
    while i < len(lines):
        line = lines[i].strip()
        nxt = lines[i + 1].strip() if i + 1 < len(lines) else ""
        is_sep = bool(nxt) and set(nxt) <= set("|-: ") and "-" in nxt
        if line.startswith("|") and is_sep:
            header = tuple(c.strip() for c in line.strip("|").split("|"))
            j, rows = i + 2, []
            while j < len(lines) and lines[j].strip().startswith("|"):
                cells = [c.strip() for c in lines[j].strip().strip("|").split("|")]
                rows.append(cells)
                j += 1
            out.append((header, rows))
            i = j
        else:
            i += 1
    return out


def clean(s):
    """يزيل تشكيل Markdown والرموز الزخرفية."""
    s = re.sub(r"\*\*|\*|`", "", s)
    s = s.replace("★", "").replace("·", "·")
    return " ".join(s.split()).strip()


def is_indonesian(s):
    """جملة إندونيسية صالحة: حروف لاتينية، بلا عربية، طول معقول."""
    if not s or ARABIC.search(s):
        return False
    if not LATIN.search(s):
        return False
    if len(s) > 90:
        return False
    # نرفض ما يبدو رابطاً أو مساراً أو كوداً
    if any(t in s for t in ("http", ".md", "()", "{", "<")):
        return False
    return True


def is_arabic_text(s):
    return bool(s) and bool(ARABIC.search(s)) and len(s) <= 120


def register_of(token, source_file):
    """يحوّل رمز السجل المختصر في المصدر إلى تصنيف التطبيق."""
    t = (token or "").strip()
    if "ع" in t and "ي" not in t:
        return "🟠 عامي"
    if "أ" in t or "مهذب" in t or "رسمي" in t:
        return "🟢 رسمي"
    if "ت" in t:
        return "🔵 يومي"
    if "ي" in t or "يومي" in t:
        return "🔵 يومي"
    if t.upper().startswith("A"):
        return "🔵 يومي"
    return "🔵 يومي"


# تصنيف الفئة حسب الملف المصدر — أدق من تخمينها من النص
FILE_CATEGORY = {
    "02-al-manhaj": "عبارات أساسية",
    "10-al-tahiyyat-wa-taaruf": "تحيات وتعارف",
    "11-al-jumal-al-yawmiyya": "جمل يومية",
    "12-al-suq-wa-al-tijara": "سوق وتجارة",
    "13-al-taam-wa-al-usra": "طعام وعائلة",
    "14-kayfa-yatakallamun": "لغة الشارع",
    "16-al-sinariyuhat-wa-al-hiwarat": "حوارات",
    "23-al-arqam-al-mal-al-waqt": "أرقام ومال ووقت",
    "24-mufradat-hasab-al-majal": "مفردات المجال",
    "25-qawaid-taabir-jahiza": "تعبيرات جاهزة",
    "26-hiwarat-al-suq-wa-jadhb-al-zabun": "جذب الزبون",
}


# ترجمات مُعتمدة لعبارات أساسية يعرضها المصدر في جدول "متى" فقط
# (يشرح توقيت الاستخدام لا المعنى). أُضيفت يدوياً بعد التحقق من المصدر،
# لأن إسقاط التحيات الأساسية يُفقد المتعلم أهم ما يحتاجه في يومه الأول.
CURATED_MEANINGS = {
    "selamat pagi": "صباح الخير",
    "selamat siang": "طاب نهارك (ظهراً)",
    "selamat sore": "مساء الخير (العصر)",
    "selamat malam": "مساء الخير (بعد الغروب)",
    "selamat datang": "أهلاً وسهلاً",
    "selamat jalan": "مع السلامة (لمن يسافر)",
    "selamat tinggal": "وداعاً (لمن يبقى)",
}


def category_for(fname):
    stem = os.path.basename(fname)[:-3]
    for k, v in FILE_CATEGORY.items():
        if stem.startswith(k):
            return v
    return "عام"


# ---- الكتل الموسومة: **الإندونيسية:** ... **النطق:** ... **الطبيعي:** ... ----
# هذا أغنى شكل في المصدر لأنه يحمل النطق العربي وشرح "متى تقولها".
BLOCK_LABELS = {
    "الإندونيسية": "id",
    "النطق": "pron",
    "النطق بالعربية (تقريبي)": "pron",
    "الطبيعي": "ar",
    "الترجمة العربية الطبيعية": "ar",
    "متى": "when",
    "الاستخدام": "when",
    "شرح الاستخدام": "when",
    "سجل": "reg",
    "ملاحظة السجل": "reg",
    "السجل": "reg",
}
LABEL_RE = re.compile(r"\*\*([^:*]+):\*\*\s*(.*)")


def parse_labeled_blocks(text):
    """يستخرج الكتل ذات التسميات العريضة، ويُعيد قواميس مفاتيحها موحّدة."""
    blocks, cur = [], {}
    for raw in text.split("\n"):
        line = raw.strip()
        m = LABEL_RE.match(line)
        if m:
            label = m.group(1).strip()
            value = clean(m.group(2))
            key = BLOCK_LABELS.get(label)
            if key == "id":
                # بداية كتلة جديدة
                if cur.get("id"):
                    blocks.append(cur)
                cur = {"id": value}
            elif key and cur:
                cur.setdefault(key, value)
            continue
        # سطر فارغ أو عنوان يُنهي الكتلة الحالية
        if not line or line.startswith("#"):
            if cur.get("id"):
                blocks.append(cur)
                cur = {}
    if cur.get("id"):
        blocks.append(cur)
    return blocks


# جداول (عبارة | نطق | متى) و(المعنى | مهذب | يومي مع صديق)
# جداول تحمل النطق. العمود الثالث يختلف معناه حسب الترويسة،
# لذلك نسجّله صراحةً بدل افتراض أنه "الترجمة".
PRON_HEADERS = {
    # الترويسة: (فهرس العبارة, فهرس النطق, دور العمود الثالث)
    ("العبارة", "نطق تقريبي", "متى"): (0, 1, "when"),
    ("رقم", "إندونيسية", "نطق تقريبي"): (1, 2, None),
}
POLITE_CASUAL_HEADERS = {
    ("المعنى", "مهذب", "يومي مع صديق"),
    ("المعنى", "مهذب", "يومي"),
}



def kt_escape(s):
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


class _SeenView:
    """عرض للقراءة فقط على مفاتيح القاموس المدموج."""

    def __init__(self, d):
        self._d = d

    def __contains__(self, k):
        return k in self._d

    def add(self, k):  # لم يعد مستخدماً بعد اعتماد الدمج
        pass


def main():
    if not os.path.isdir(ASSETS):
        print(f"✗ مجلد المصدر غير موجود: {ASSETS}")
        return 1

    files = sorted(glob.glob(os.path.join(ASSETS, "*.md")))
    contrasts = []
    # قاموس مفتاحه العبارة الإندونيسية: يدمج المعلومات بدل رفض المكرر.
    # سبب الدمج: نفس العبارة قد ترد في جدول بسيط وفي كتلة موسومة تحمل
    # النطق و«متى تقولها». الاحتفاظ بالأولى فقط يُفقد النطق.
    merged = {}
    already = existing_expressions()
    seen_id = _SeenView(merged)
    stats = {"tables": 0, "rows": 0, "rejected": 0, "merged": 0}

    def add_phrase(rec):
        key = rec["id"].lower()
        if key in already:
            # موجودة أصلاً في البذر اليدوي — تجنّب التكرار في البطاقات والبحث
            stats["rejected"] += 1
            return
        old = merged.get(key)
        if old is None:
            merged[key] = rec
            return
        stats["merged"] += 1
        for field in ("pron", "note", "ar"):
            if not old.get(field) and rec.get(field):
                old[field] = rec[field]
        # الصيغة الرسمية أدق من الافتراضية اليومية
        if old.get("reg") == "🔵 يومي" and rec.get("reg") not in (None, "", "🔵 يومي"):
            old["reg"] = rec["reg"]

    for f in files:
        text = open(f, encoding="utf-8").read()
        cat = category_for(f)
        for header, rows in parse_tables(text):
            stats["tables"] += 1
            stats["rows"] += len(rows)

            if header in PHRASE_HEADERS:
                roles = PHRASE_HEADERS[header]
                for cells in rows:
                    if len(cells) < 2:
                        stats["rejected"] += 1
                        continue
                    ind, ar = clean(cells[0]), clean(cells[1])
                    extra = clean(cells[2]) if len(cells) > 2 and roles[2] else ""
                    if not (is_indonesian(ind) and is_arabic_text(ar)):
                        stats["rejected"] += 1
                        continue
                    key = ind.lower()
                    add_phrase({
                        "id": ind, "ar": ar, "pron": "",
                        "reg": register_of(extra if roles[2] == "reg" else "", f),
                        "note": extra if roles[2] in ("note",) else "",
                        "cat": cat,
                        "src": os.path.basename(f),
                    })

            elif header in PRON_HEADERS:
                # جدول يحمل النطق العربي — قيمة عالية للمبتدئ
                idx_id, idx_pr, third = PRON_HEADERS[header]
                for cells in rows:
                    if len(cells) <= idx_pr:
                        continue
                    ind = clean(cells[idx_id])
                    pron = clean(cells[idx_pr])
                    third_val = clean(cells[idx_pr + 1]) if len(cells) > idx_pr + 1 else ""
                    if not is_indonesian(ind):
                        continue
                    curated = CURATED_MEANINGS.get(ind.lower())
                    if curated:
                        add_phrase({
                            "id": ind, "ar": curated, "pron": pron,
                            "reg": "🟢 رسمي", "note": third_val, "cat": cat,
                            "src": os.path.basename(f),
                        })
                    elif third == "when":
                        # العمود يصف *متى تُقال* لا معناها.
                        # تخزينه كترجمة ينتج بطاقة كاذبة مثل
                        # «Selamat pagi = صباحًا حتى ~10–11».
                        # نضعه في حقل الاستخدام، وننتظر الترجمة من مصدر آخر.
                        if not third_val:
                            continue
                        add_phrase({
                            "id": ind, "ar": "", "pron": pron,
                            "reg": "🔵 يومي", "note": third_val, "cat": cat,
                            "src": os.path.basename(f),
                        })
                    elif is_arabic_text(third_val):
                        add_phrase({
                            "id": ind, "ar": third_val, "pron": pron,
                            "reg": "🔵 يومي", "note": "", "cat": cat,
                            "src": os.path.basename(f),
                        })

            elif header in POLITE_CASUAL_HEADERS:
                # (المعنى | مهذب | يومي) ⇒ عبارة مهذبة + سؤال سجل
                for cells in rows:
                    if len(cells) < 3:
                        continue
                    meaning, polite, casual = (clean(c) for c in cells[:3])
                    if not is_arabic_text(meaning):
                        continue
                    if is_indonesian(polite):
                        add_phrase({
                            "id": polite, "ar": meaning, "pron": "",
                            "reg": "🟢 رسمي", "note": "صيغة مهذبة",
                            "cat": cat, "src": os.path.basename(f),
                        })
                    if is_indonesian(casual):
                        add_phrase({
                            "id": casual, "ar": meaning, "pron": "",
                            "reg": "🔵 يومي", "note": "صيغة يومية مع المقربين",
                            "cat": cat, "src": os.path.basename(f),
                        })
                    if is_indonesian(polite) and is_indonesian(casual) and polite != casual:
                        contrasts.append({
                            "formal": polite, "casual": casual,
                            "note": f"المعنى: {meaning}", "cat": cat,
                            "src": os.path.basename(f),
                        })

            elif header in FORMAL_CASUAL_HEADERS:
                for cells in rows:
                    if len(cells) < 2:
                        continue
                    formal, casual = clean(cells[0]), clean(cells[1])
                    note = clean(cells[2]) if len(cells) > 2 else ""
                    if not (is_indonesian(formal) and is_indonesian(casual)):
                        continue
                    if formal.lower() == casual.lower():
                        continue
                    # نتجاهل "الملاحظات" التي هي مجرد رموز أولوية مثل A أو ★
                    if len(note) <= 3 or not ARABIC.search(note):
                        note = ""
                    contrasts.append({
                        "formal": formal, "casual": casual,
                        "note": note, "cat": cat,
                        "src": os.path.basename(f),
                    })

        # ---- الكتل الموسومة (أغنى شكل: يحمل النطق و«متى تقولها») ----
        for b in parse_labeled_blocks(text):
            ind = b.get("id", "")
            ar = b.get("ar", "")
            # قد يحمل السطر عدة بدائل مفصولة بـ / — نأخذ العبارة كاملة
            if not (is_indonesian(ind) and is_arabic_text(ar)):
                stats["rejected"] += 1
                continue

            add_phrase({
                "id": ind,
                "ar": ar,
                "pron": b.get("pron", ""),
                "reg": register_of(b.get("reg", ""), f),
                "note": b.get("when", ""),
                "cat": cat,
                "src": os.path.basename(f),
            })

    # نُسقط أي عبارة بقيت بلا ترجمة عربية بعد الدمج.
    # الأفضل ألا تظهر البطاقة أصلاً من أن تظهر بترجمة خاطئة.
    incomplete = [r for r in merged.values() if not r.get("ar")]
    stats["no_translation"] = len(incomplete)
    phrases = sorted(
        (r for r in merged.values() if r.get("ar")),
        key=lambda r: (r["cat"], r["id"].lower()),
    )

    print("=" * 64)
    print("استيراد محتوى indolang")
    print("=" * 64)
    print(f"ملفات المصدر           : {len(files)}")
    print(f"جداول مفحوصة           : {stats['tables']}")
    print(f"صفوف مفحوصة            : {stats['rows']}")
    print(f"عبارات مقبولة          : {len(phrases)}")
    print(f"أزواج رسمي/يومي مقبولة : {len(contrasts)}")
    print(f"صفوف مرفوضة (جودة)     : {stats['rejected']}")
    print(f"صفوف مدموجة (إثراء)    : {stats['merged']}")
    print(f"عبارات تحمل نطقاً      : {sum(1 for p in phrases if p.get('pron'))}")
    print(f"أُسقطت بلا ترجمة       : {stats.get('no_translation', 0)}")
    print()
    by_cat = {}
    for p in phrases:
        by_cat[p["cat"]] = by_cat.get(p["cat"], 0) + 1
    for k, v in sorted(by_cat.items(), key=lambda x: -x[1]):
        print(f"   {k:22s} {v}")

    if "--emit" not in sys.argv:
        print("\n(تشغيل تجريبي — استخدم --emit لتوليد ملف Kotlin)")
        return 0

    # ---------- توليد Kotlin ----------
    EXPR_BASE = 5000   # نطاق معرّفات مستقل لا يتعارض مع الموجود
    QUIZ_BASE = 5000

    L = []
    L.append("package com.indolearn.data.repository")
    L.append("")
    L.append("import com.indolearn.data.local.entity.CasualExpressionEntity")
    L.append("import com.indolearn.data.local.entity.TrainingItemEntity")
    L.append("")
    L.append("/**")
    L.append(" * محتوى مستورد من موسوعة indolang.")
    L.append(" *")
    L.append(" * المصدر: github.com/hishamalmushrea-cloud/indolang")
    L.append(" * (فرع arena/019ff99f-indolang) — منهج عربي شامل للإندونيسية.")
    L.append(" *")
    L.append(" * ⚠️ هذا الملف **مُولَّد آلياً** بواسطة tools/import_indolang.py")
    L.append(" *    لا تحرره يدوياً؛ عدّل المصدر في assets/encyclopedia/ ثم أعد التوليد.")
    L.append(" *")
    L.append(" * النص الكامل للموسوعة محفوظ في app/src/main/assets/encyclopedia/")
    L.append(" * ويُعرض في شاشة المكتبة المرجعية. هنا فقط الصفوف المنتظمة التي")
    L.append(" * أمكن تحويلها إلى محتوى قابل للتعلم والاختبار.")
    L.append(" */")
    L.append("object IndoLangContent {")
    L.append("")
    L.append(f"    /** عبارات يومية مستخرجة من جداول الموسوعة ({len(phrases)} عبارة). */")
    L.append("    val expressions: List<CasualExpressionEntity> = listOf(")
    for n, p in enumerate(phrases):
        eid = EXPR_BASE + n
        usage = p["note"] or f"من: {p['src']}"
        L.append(
            f'        CasualExpressionEntity({eid}, "{kt_escape(p["id"])}", '
            f'"{kt_escape(p.get("pron", ""))}", '
            f'"{kt_escape(p["ar"])}", "{p["reg"]}", "{kt_escape(usage)}", null, '
            f'"{kt_escape(p["cat"])}", 0, "ID"),'
        )
    L.append("    )")
    L.append("")
    # أسئلة بأربعة خيارات. الخياران فقط يجعلان التخمين 50%،
    # فنضيف مشتتات حقيقية من الصيغ اليومية الأخرى (لا كلمات مخترعة).
    # المشتتات يجب أن تكون من نفس نوع الإجابة (صيغة قصيرة)،
    # وإلا صار السؤال سهلاً بالاستبعاد الشكلي (جملة كاملة وسط كلمات).
    pool = [
        c["casual"] for c in contrasts
        if "," not in c["casual"] and "?" not in c["casual"]
        and len(c["casual"].split()) <= 2
    ]
    kept = 0
    L.append("    /** أسئلة سجل لغوي: رسمي مقابل يومي. */")
    L.append("    val registerQuizzes: List<TrainingItemEntity> = listOf(")
    for n, c in enumerate(contrasts):
        distractors = [
            d for d in pool
            if d != c["casual"] and d.lower() != c["formal"].lower() and "," not in d
        ]
        if "," in c["casual"] or "?" in c["casual"] or len(c["casual"].split()) > 2:
            continue
        if len(distractors) < 2:
            # بلا مشتتات كافية يصبح السؤال تخميناً — نتخطاه بدل إنتاج سؤال رديء
            continue
        picks = [distractors[(n * 7 + 3) % len(distractors)],
                 distractors[(n * 13 + 11) % len(distractors)]]
        if picks[0] == picks[1]:
            picks[1] = distractors[(n * 13 + 12) % len(distractors)]
        if picks[0] == picks[1]:
            continue
        options = [c["casual"], c["formal"]] + picks
        # ترتيب ثابت (لا عشوائية) حتى يكون التوليد قابلاً لإعادة الإنتاج
        options = sorted(set(options), key=lambda x: x.lower())
        if len(options) < 4 or c["casual"] not in options:
            continue
        qid = QUIZ_BASE + kept
        kept += 1
        expl = c["note"] or f'{c["formal"]} = الصيغة الرسمية • {c["casual"]} = الصيغة اليومية'
        L.append(
            f'        TrainingItemEntity({qid}, "MULTIPLE_CHOICE", '
            f'"ما الصيغة اليومية الشائعة لـ \\"{kt_escape(c["formal"])}\\"؟", '
            f'"{kt_escape(c["casual"])}", "{kt_escape(",".join(options))}", '
            f'"{kt_escape(expl)}", "{kt_escape(c["cat"])}", "ID"),'
        )
    L.append("    )")
    print(f"  أسئلة بأربعة خيارات: {kept} (من {len(contrasts)} زوجاً)")
    L.append("}")

    with open(OUT_KT, "w", encoding="utf-8") as fh:
        fh.write("\n".join(L) + "\n")
    print(f"\n✓ تم توليد {OUT_KT}")
    print(f"  عبارات: {len(phrases)} · أسئلة سجل: {len(contrasts)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
