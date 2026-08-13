#!/usr/bin/env python3
"""
مستورد محتوى موسوعة turklang إلى بيانات IndoLearn (اللغة التركية).

المصدر: https://github.com/hishamalmushrea-cloud/turklang (فرع arena/019ff9c0-turklang)
6 ملفات Markdown عربية: موسوعة إتقان التركية، بنك حوارات، لغة السوق،
بطاقات البائع، بنك جمل، وبرنامج 12 أسبوعاً.

نفس استراتيجية indolang (طبقتان، بلا فقدان محتوى):
  الطبقة 1 — النص الكامل في assets/library/tr/ يُعرض في المكتبة المرجعية.
  الطبقة 2 — الصفوف المنتظمة فقط تتحول إلى كيانات قابلة للتعلم والاختبار.

فروق جوهرية عن الإندونيسية اقتضت مستورداً منفصلاً (لا نسخ منهج):
  * التركية لها **سلّم سجل رباعي** صريح في المصدر
    (رسمي · محايد · يومي · عامي) بينما الإندونيسية ثنائية غالباً.
  * جداول «مدرسي مقابل طبيعي» تعالج مشكلة خاصة بالتركية:
    ما تعلّمه الكتب ≠ ما يقوله الناس.
  * المصدر يحمل تحذيرات استخدام صريحة (عمود «تحذير» و«خطر») —
    وهي قيمة تعليمية عالية لأن الخطأ الاجتماعي في التركية مكلف
    (مثال: moruk قد تُهين، lan غير مهني).

الاستخدام:
    python3 tools/import_turklang.py            # تقرير فقط
    python3 tools/import_turklang.py --emit     # يولّد ملف Kotlin
"""

import os
import re
import sys
import glob

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "IndoLearn", "app", "src", "main", "assets", "library", "tr")
REPO_KT = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "LearnRepository.kt",
)
OUT_KT = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "TurkLangContent.kt",
)

ARABIC = re.compile(r"[\u0600-\u06FF]")
TURKISH_CHARS = "abcçdefgğhıijklmnoöprsştuüvyzABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ"

# نطاقات معرّفات مستقلة تماماً عن الإندونيسية (5000) والبذر اليدوي
EXPR_BASE = 7000
QUIZ_BASE = 7000
SCENARIO_BASE = 7000

# ---------------------------------------------------------------- utilities


def strip_md(s):
    s = re.sub(r"\*\*|\*|`", "", s)
    return " ".join(s.replace("★", "").split()).strip()


def has_comma(*vals):
    """
    حقل `options` في TrainingItemEntity مفصول بفواصل،
    فأي خيار يحتوي فاصلة يُقسَّم خطأً ولا تُطابق الإجابة أي خيار
    (سؤال يستحيل حله). نستبعد هذه الحالات بدل إنتاج سؤال مكسور.
    """
    return any("," in (v or "") for v in vals)


def is_turkish(s):
    """جملة تركية صالحة: لاتينية، بلا حروف عربية، طول معقول."""
    if not s or ARABIC.search(s):
        return False
    if not any(c in TURKISH_CHARS for c in s):
        return False
    if len(s) > 90:
        return False
    if any(t in s for t in ("http", ".md", "{", "<", "|")):
        return False
    return True


def is_arabic(s):
    return bool(s) and bool(ARABIC.search(s)) and len(s) <= 140


def normalize_register(raw):
    """
    يحوّل وصف الأسلوب في المصدر إلى تصنيف التطبيق.
    ترتيب الفحص مهم: «عامي» قبل «يومي» لأن المصدر يكتب «يومي–عامي».
    """
    t = (raw or "").strip()
    if not t:
        return "🔵 يومي"
    if "لا تقُلها" in t or "لا تقلها" in t:
        return "🔴 تجنّبها"
    if "بازار" in t or "سوقي" in t:
        return "🟠 عامي"
    if "عامي" in t:
        return "🟠 عامي"
    if "رسمي" in t:
        return "🟢 رسمي"
    if "محايد" in t:
        return "🟢 رسمي"
    return "🔵 يومي"


FILE_CATEGORY = {
    "Turkish_Mastery_Encyclopedia": "أساسيات وسجل",
    "pazar_dukkan_musteri": "سوق ودكان",
    "satici_kartlar_ve_cevaplar": "بطاقات البائع",
    "bank_diyaloglar": "حوارات",
    "bank_cumleler_ve_almak": "بنك الجمل",
    "12_hafta_A0_A1": "خطة 12 أسبوعاً",
}


def category_for(path):
    stem = os.path.basename(path)[:-3]
    return FILE_CATEGORY.get(stem, "عام")


def parse_tables(text):
    lines = text.split("\n")
    out, i = [], 0
    while i < len(lines):
        line = lines[i].strip()
        nxt = lines[i + 1].strip() if i + 1 < len(lines) else ""
        if line.startswith("|") and nxt and set(nxt) <= set("|-: ") and "-" in nxt:
            header = tuple(c.strip() for c in line.strip("|").split("|"))
            j, rows = i + 2, []
            while j < len(lines) and lines[j].strip().startswith("|"):
                rows.append([c.strip() for c in lines[j].strip().strip("|").split("|")])
                j += 1
            out.append((header, rows))
            i = j
        else:
            i += 1
    return out


# ------------------------------------------------- table schemas we trust
# (فهرس التركية, فهرس النطق أو None, فهرس المعنى, فهرس الأسلوب أو None,
#  فهرس التحذير أو None)
PHRASE_TABLES = {
    ("تركية", "نطق تقريبي", "معنى", "أسلوب"): (0, 1, 2, 3, None),
    ("تركية", "نطق", "لمن", "أسلوب", "ملاحظة"): (0, 1, None, 3, 4),
    ("تركية", "ترجمة طبيعية", "أسلوب"): (0, None, 1, 2, None),
    ("تركية", "معنى", "ملاحظة"): (0, None, 1, None, 2),
    ("تركية", "معنى", "خطر"): (0, None, 1, None, 2),
    ("عبارة", "معنى", "أسلوب", "تحذير"): (0, None, 1, 2, 3),
    ("تركية", "نطق", "معنى طبيعي", "متى"): (0, 1, 2, 3, None),
    # كلمات متعددة المعنى حسب السياق (tamam, peki, yani...) — شائعة جداً
    ("تركية", "معانٍ حسب السياق", "أسلوب"): (0, None, 1, 2, None),
    # عبارات المزاح والود: العمودان "مع من" و"خطر" تحذير اجتماعي مهم
    ("عبارة", "طبيعي؟", "مع من", "خطر"): (0, None, None, None, 3),
    # تراكيب الأفعال (عائلة almak) — العمود الأخير مثال تركي
    ("تركيب", "معنى", "مستوى", "أسلوب", "مثال"): (0, None, 1, 3, None),
}

# تعبيرات اصطلاحية: الحرفي ≠ الحقيقي — قيمة تعليمية عالية
IDIOM_TABLE = ("تعبير", "حرفي", "حقيقي", "شيوع", "سجل")
# أصدقاء كاذبون: ما يظنه العربي مقابل المعنى التركي
FALSE_FRIENDS = ("تركية", "يظنها العربي", "المعنى التركي الشائع")

# «مدرسي مقابل طبيعي» — جوهر تعليم التركية الواقعية
SCHOOL_VS_REAL = ("مدرسي", "طبيعي", "ملاحظة")
# سلّم السجل الرباعي
REGISTER_LADDER = ("موقف", "رسمي", "محايد", "يومي", "عامي")
# ردود انعكاسية (إن سمعت ⇒ قل)
HEAR_SAY = ("إن سمعت", "قل")
# اختصارات الكتابة الرقمية
ABBREV = ("اختصار", "أصل", "معنى")


DIALOG_TITLE = re.compile(r"^##\s+\d+\.\s+(.+?)\s*$")


def parse_dialogues(text):
    """
    يستخرج حوارات bank_diyaloglar.md.

    الشكل: عنوان `## رقم. وصف` ثم أسطر تبدأ بشرطة em-dash للحوار التركي،
    ثم أسطر شرح موسومة (**ترجمة:** / **كلمات:** / **أسلوب:** ...).

    ⚠️ حقيقة مهمة عن المصدر: **3 حوارات فقط من 50** تحمل سطر «ترجمة».
    البقية يشرحها المؤلف بالمفردات والأسلوب لا بترجمة كاملة.
    لذلك **لا نخترع ترجمة**: نحفظ الحوار التركي كما هو، ونجمع الشروح
    العربية المتاحة في حقل الترجمة/الملاحظات. الحوار بلا أي شرح عربي
    يبقى مفيداً للاستماع والقراءة، ويُعرض نصه التركي مع عنوانه العربي.
    """
    out = []
    cur_title, turkish, notes = None, [], []

    def flush():
        nonlocal cur_title, turkish, notes
        if cur_title and len(turkish) >= 2:
            out.append((cur_title, "\n".join(turkish), "\n".join(notes)))
        cur_title, turkish, notes = None, [], []

    for raw in text.split("\n"):
        line = raw.strip()
        m = DIALOG_TITLE.match(line)
        if m:
            flush()
            cur_title = strip_md(m.group(1))
            continue
        if cur_title is None:
            continue
        if line[:1] in ("\u2014", "\u2013") and not ARABIC.search(line):
            t = strip_md(line[1:].strip())
            if t:
                turkish.append(t)
            continue
        # أسطر الشرح الموسومة تحمل القيمة التعليمية العربية
        if line.startswith("**") and ARABIC.search(line):
            notes.append(strip_md(line))
    flush()
    return out


# تصنيف الحوار من عنوانه — أدق من وضع الجميع في فئة واحدة
# الترتيب مقصود: الأكثر تحديداً أولاً.
# مثال: «تعارف في الشارع» يجب أن يُصنَّف تعارفاً لا سفراً،
# لذلك تأتي كلمة «تعارف» قبل «شارع».
DIALOG_CATEGORIES = [
    ("تعارف", "تعارف"), ("صديق", "تعارف"), ("جيران", "تعارف"),
    ("مطعم", "مطعم"), ("مقهى", "مطعم"), ("قهوة", "مطعم"),
    ("بازار", "سوق"), ("مساومة", "سوق"), ("سوق", "سوق"),
    ("محل", "سوق"), ("بائع", "سوق"), ("زبون", "سوق"), ("شكوى", "سوق"),
    ("تاكسي", "مواصلات"), ("باص", "مواصلات"), ("دلموش", "مواصلات"),
    ("مطار", "سفر"), ("فندق", "سفر"), ("ضياع", "سفر"), ("شارع", "سفر"),
    ("صيدلية", "صحة"), ("مستشفى", "صحة"), ("طبيب", "صحة"),
    ("هاتف", "اتصال"), ("واتساب", "اتصال"), ("بريد", "اتصال"),
    ("مقابلة", "عمل"), ("مدير", "عمل"), ("عمل", "عمل"),
]


# عناوين تركية للفئات — تُعرض كعنوان لاتيني للسيناريو
SCENARIO_TITLE_TR = {
    "مطعم": "Restoran", "سوق": "Pazar", "مواصلات": "Ulaşım",
    "سفر": "Seyahat", "صحة": "Sağlık", "تعارف": "Tanışma",
    "اتصال": "İletişim", "عمل": "İş", "عام": "Günlük",
}


def dialogue_category(title):
    for key, cat in DIALOG_CATEGORIES:
        if key in title:
            return cat
    return "عام"


def kt(s):
    return (s.replace("\\", "\\\\").replace('"', '\\"')
             .replace("\n", "\\n").replace("$", "\\$"))


def existing_turkish_expressions():
    """العبارات التركية المكتوبة يدوياً في البذر — لا نكررها."""
    if not os.path.exists(REPO_KT):
        return set()
    src = open(REPO_KT, encoding="utf-8").read()
    out = set()
    for m in re.finditer(
        r'CasualExpressionEntity\(\d+,\s*"((?:[^"\\]|\\.)*)"[^)]*?"TR"\)', src
    ):
        out.add(m.group(1).lower())
    return out


# ---------------------------------------------------------------- main


def main():
    if not os.path.isdir(ASSETS):
        print(f"✗ مجلد المصدر غير موجود: {ASSETS}")
        return 1

    files = sorted(glob.glob(os.path.join(ASSETS, "*.md")))
    already = existing_turkish_expressions()
    merged = {}
    register_rows = []   # (موقف, رسمي, محايد, يومي, عامي)
    school_rows = []     # (مدرسي, طبيعي, ملاحظة)
    reply_rows = []      # (إن سمعت, قل)
    idiom_rows = []      # (تعبير, حرفي, حقيقي)
    false_friend_rows = []  # (كلمة, ما يظنه العربي, المعنى الحقيقي)
    dialogues = []       # (عنوان, نص تركي, ترجمة)
    stats = {"tables": 0, "rows": 0, "rejected": 0, "merged": 0, "dup_seed": 0}

    def add(rec):
        key = rec["tr"].lower()
        if key in already:
            stats["dup_seed"] += 1
            return
        old = merged.get(key)
        if old is None:
            merged[key] = rec
            return
        stats["merged"] += 1
        for fld in ("pron", "ar", "note"):
            if not old.get(fld) and rec.get(fld):
                old[fld] = rec[fld]
        if old.get("reg") == "🔵 يومي" and rec.get("reg") not in ("", None, "🔵 يومي"):
            old["reg"] = rec["reg"]

    for f in files:
        text = open(f, encoding="utf-8").read()
        cat = category_for(f)

        if os.path.basename(f).startswith("bank_diyaloglar"):
            for title, tr_text, ar_text in parse_dialogues(text):
                dialogues.append((title, tr_text, ar_text))

        for header, rows in parse_tables(text):
            stats["tables"] += 1
            stats["rows"] += len(rows)

            if header in PHRASE_TABLES:
                i_tr, i_pr, i_ar, i_st, i_warn = PHRASE_TABLES[header]
                for cells in rows:
                    if len(cells) <= i_tr:
                        continue
                    tr = strip_md(cells[i_tr])
                    if not is_turkish(tr):
                        stats["rejected"] += 1
                        continue
                    ar = strip_md(cells[i_ar]) if i_ar is not None and len(cells) > i_ar else ""
                    if not is_arabic(ar):
                        # بلا معنى عربي لا فائدة تعليمية — نرفض بدل الاختراع
                        stats["rejected"] += 1
                        continue
                    pron = strip_md(cells[i_pr]) if i_pr is not None and len(cells) > i_pr else ""
                    style = strip_md(cells[i_st]) if i_st is not None and len(cells) > i_st else ""
                    warn = strip_md(cells[i_warn]) if i_warn is not None and len(cells) > i_warn else ""
                    if warn and not ARABIC.search(warn):
                        warn = ""
                    add({
                        "tr": tr, "ar": ar, "pron": pron,
                        "reg": normalize_register(style),
                        "note": warn, "cat": cat, "src": os.path.basename(f),
                    })

            elif header == REGISTER_LADDER:
                for cells in rows:
                    if len(cells) >= 5:
                        register_rows.append([strip_md(c) for c in cells[:5]])

            elif header == SCHOOL_VS_REAL:
                for cells in rows:
                    if len(cells) >= 2:
                        school_rows.append([strip_md(c) for c in cells[:3]]
                                           + [""] * (3 - min(len(cells), 3)))

            elif header == IDIOM_TABLE:
                for cells in rows:
                    if len(cells) < 3:
                        continue
                    expr, literal, real = (strip_md(c) for c in cells[:3])
                    style = strip_md(cells[4]) if len(cells) > 4 else ""
                    if not (is_turkish(expr) and is_arabic(real)):
                        stats["rejected"] += 1
                        continue
                    note = f"حرفياً: {literal}" if is_arabic(literal) else ""
                    add({
                        "tr": expr, "ar": real, "pron": "",
                        "reg": normalize_register(style),
                        "note": note, "cat": "تعبيرات اصطلاحية",
                        "src": os.path.basename(f),
                    })
                    if is_arabic(literal) and literal != real:
                        idiom_rows.append((expr, literal, real))

            elif header == FALSE_FRIENDS:
                for cells in rows:
                    if len(cells) >= 3:
                        w, thinks, real = (strip_md(c) for c in cells[:3])
                        if is_turkish(w) and is_arabic(thinks) and is_arabic(real):
                            false_friend_rows.append((w, thinks, real))

            elif header == HEAR_SAY:
                for cells in rows:
                    if len(cells) >= 2:
                        reply_rows.append([strip_md(c) for c in cells[:2]])

    phrases = sorted(merged.values(), key=lambda r: (r["cat"], r["tr"].lower()))

    print("=" * 64)
    print("استيراد محتوى turklang (التركية)")
    print("=" * 64)
    print(f"ملفات المصدر            : {len(files)}")
    print(f"جداول مفحوصة            : {stats['tables']}")
    print(f"صفوف مفحوصة             : {stats['rows']}")
    print(f"عبارات مقبولة           : {len(phrases)}")
    print(f"  منها تحمل نطقاً       : {sum(1 for p in phrases if p.get('pron'))}")
    print(f"  منها تحمل تحذيراً     : {sum(1 for p in phrases if p.get('note'))}")
    print(f"صفوف سلّم السجل         : {len(register_rows)}")
    print(f"صفوف مدرسي/طبيعي        : {len(school_rows)}")
    print(f"ردود انعكاسية           : {len(reply_rows)}")
    print(f"تعبيرات اصطلاحية        : {len(idiom_rows)}")
    print(f"أصدقاء كاذبون           : {len(false_friend_rows)}")
    print(f"حوارات                  : {len(dialogues)}")
    print(f"مرفوضة (بلا معنى عربي)  : {stats['rejected']}")
    print(f"مدموجة (إثراء)          : {stats['merged']}")
    print(f"موجودة في البذر         : {stats['dup_seed']}")
    print()
    by_cat = {}
    for p in phrases:
        by_cat[p["cat"]] = by_cat.get(p["cat"], 0) + 1
    for k, v in sorted(by_cat.items(), key=lambda x: -x[1]):
        print(f"   {k:22s} {v}")

    if "--emit" not in sys.argv:
        print("\n(تشغيل تجريبي — استخدم --emit لتوليد ملف Kotlin)")
        return 0

    # ------------------------------------------------------------ quizzes
    quizzes = []

    # 1) سلّم السجل: رسمي ⇄ عامي — يعلّم *متى* لا *ماذا* فقط
    for row in register_rows:
        situation, formal, neutral, daily, slang = row
        opts = [o for o in (formal, neutral, daily, slang) if is_turkish(o)]
        if len(set(o.lower() for o in opts)) < 4 or not is_arabic(situation):
            continue
        if has_comma(*opts):
            continue
        quizzes.append((
            f"في موقف «{situation}»: ما الصيغة الرسمية؟",
            formal, opts,
            f"رسمي: {formal} • محايد: {neutral} • يومي: {daily} • عامي: {slang}",
            "سجل لغوي",
        ))
        quizzes.append((
            f"في موقف «{situation}»: ما الصيغة العامية بين الأصدقاء؟",
            slang, opts,
            f"عامي: {slang} — تجنّبها في العمل والمواقف الرسمية.",
            "سجل لغوي",
        ))

    # 2) مدرسي مقابل طبيعي — أهم فجوة عند متعلم التركية
    school_pool = [r[1] for r in school_rows
                   if is_turkish(r[1]) and len(r[1].split()) <= 3 and "," not in r[1]]
    for n, (schoolish, natural, note) in enumerate(school_rows):
        if not (is_turkish(schoolish) and is_turkish(natural)):
            continue
        if schoolish.lower() == natural.lower():
            continue
        distract = [d for d in school_pool
                    if d.lower() not in (natural.lower(), schoolish.lower())]
        if len(distract) < 2:
            continue
        picks = [distract[(n * 5 + 2) % len(distract)],
                 distract[(n * 11 + 7) % len(distract)]]
        if picks[0] == picks[1]:
            continue
        opts = sorted({natural, schoolish, *picks}, key=str.lower)
        if len(opts) < 4 or has_comma(*opts):
            continue
        expl = note if is_arabic(note) else f"الكتب تعلّم «{schoolish}» لكن الأتراك يقولون «{natural}»."
        quizzes.append((
            f"ماذا يقول الأتراك فعلاً بدل «{schoolish}»؟",
            natural, opts, expl, "مدرسي مقابل طبيعي",
        ))

    # 3) ردود انعكاسية — يُختبر الرد الصحيح لا الترجمة
    reply_pool = [r[1] for r in reply_rows if is_turkish(r[1]) and "," not in r[1]]
    for n, (heard, reply) in enumerate(reply_rows):
        if not (is_turkish(heard) and is_turkish(reply)):
            continue
        distract = [d for d in reply_pool if d.lower() != reply.lower()]
        if len(distract) < 3:
            continue
        picks = [distract[(n * 3 + 1) % len(distract)],
                 distract[(n * 7 + 4) % len(distract)],
                 distract[(n * 13 + 6) % len(distract)]]
        opts = sorted({reply, *picks}, key=str.lower)
        if len(opts) < 4 or has_comma(*opts):
            continue
        quizzes.append((
            f"سمعت «{heard}» — بماذا تردّ؟",
            reply, opts,
            f"الرد الطبيعي على «{heard}» هو «{reply}».",
            "ردود سريعة",
        ))

    # ------------------------------------------------------------ emit
    L = []
    L.append("package com.indolearn.data.repository")
    L.append("")
    L.append("import com.indolearn.data.local.entity.CasualExpressionEntity")
    L.append("import com.indolearn.data.local.entity.DailyScenarioEntity")
    L.append("import com.indolearn.data.local.entity.TrainingItemEntity")
    L.append("")
    L.append("/**")
    L.append(" * محتوى تركي مستورد من موسوعة turklang.")
    L.append(" *")
    L.append(" * المصدر: github.com/hishamalmushrea-cloud/turklang")
    L.append(" * (فرع arena/019ff9c0-turklang)")
    L.append(" *")
    L.append(" * ⚠️ هذا الملف **مُولَّد آلياً** بواسطة tools/import_turklang.py")
    L.append(" *    لا تحرره يدوياً؛ عدّل المصدر في assets/library/tr/ ثم أعد التوليد.")
    L.append(" *")
    L.append(" * النص الكامل محفوظ في assets/library/tr/ ويُعرض في المكتبة المرجعية.")
    L.append(" * هنا فقط الصفوف المنتظمة القابلة للتعلم والاختبار.")
    L.append(" */")
    L.append("object TurkLangContent {")
    L.append("")
    L.append(f"    /** تعبيرات تركية يومية ({len(phrases)} تعبيراً). */")
    L.append("    val expressions: List<CasualExpressionEntity> = listOf(")
    for n, p in enumerate(phrases):
        usage = p["note"] or f"من: {p['src']}"
        L.append(
            f'        CasualExpressionEntity({EXPR_BASE + n}, "{kt(p["tr"])}", '
            f'"{kt(p.get("pron", ""))}", "{kt(p["ar"])}", "{p["reg"]}", '
            f'"{kt(usage)}", null, "{kt(p["cat"])}", 0, "TR"),'
        )
    L.append("    )")
    L.append("")
    L.append(f"    /** أسئلة تدريب تركية ({len(quizzes)} سؤالاً). */")
    L.append("    val quizzes: List<TrainingItemEntity> = listOf(")
    for n, (q, ans, opts, expl, cat) in enumerate(quizzes):
        L.append(
            f'        TrainingItemEntity({QUIZ_BASE + n}, "MULTIPLE_CHOICE", '
            f'"{kt(q)}", "{kt(ans)}", "{kt(",".join(opts))}", '
            f'"{kt(expl)}", "{kt(cat)}", "TR"),'
        )
    L.append("    )")
    L.append("")
    L.append(f"    /** حوارات تركية واقعية ({len(dialogues)} حواراً). */")
    L.append("    val scenarios: List<DailyScenarioEntity> = listOf(")
    for n, (title, tr_text, notes) in enumerate(dialogues):
        # العنوان في المصدر عربي ويحمل المستوى، مثل: «مطعم (A1–A2)»
        # عنوان المصدر عربي ويحمل المستوى فقط بالحروف اللاتينية (A1/B1)،
        # فاستخراج اللاتيني منه ينتج "A1" وهو بلا معنى كعنوان.
        # نبني عنواناً تركياً من فئة الحوار بدل ذلك.
        cat_tr = SCENARIO_TITLE_TR.get(dialogue_category(title), "Günlük Konuşma")
        latin_title = f"{cat_tr} {n + 1}"
        body = notes if notes else "حوار للاستماع والقراءة. راجع المكتبة المرجعية للشرح الكامل."
        L.append(
            f'        DailyScenarioEntity({SCENARIO_BASE + n}, "{kt(latin_title)}", '
            f'"{kt(title)}", "{kt(tr_text)}", "{kt(body)}", 0, '
            f'"{kt(dialogue_category(title))}", "TR"),'
        )
    L.append("    )")
    L.append("}")

    with open(OUT_KT, "w", encoding="utf-8") as fh:
        fh.write("\n".join(L) + "\n")
    print(f"\n✓ تم توليد {OUT_KT}")
    print(f"  تعبيرات: {len(phrases)} · أسئلة: {len(quizzes)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
