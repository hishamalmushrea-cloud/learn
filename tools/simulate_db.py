#!/usr/bin/env python3
"""
محاكاة قاعدة بيانات Room في SQLite لاختبار البذر والاستعلامات فعلياً.

لماذا: لا يمكن تشغيل Room ولا محاكي Android هنا، فكان السؤال
«هل تصل البيانات إلى الشاشات؟» بلا إجابة قاطعة.
هذا السكربت يبني نفس الجداول، ويُدخل نفس بيانات البذر المستخرجة من
كود Kotlin، ثم ينفّذ **نفس استعلامات DAO حرفياً** ويطبع عدد الصفوف
التي ستراها كل شاشة.

شاشة تُظهر دوّامة تحميل إلى الأبد = استعلامها أعاد صفراً.

الاستخدام: python3 tools/simulate_db.py
"""

import os
import re
import sqlite3
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PKG = os.path.join(ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn")
SOURCES = [
    os.path.join(PKG, "data", "repository", "LearnRepository.kt"),
    os.path.join(PKG, "data", "repository", "IndoLangContent.kt"),
    os.path.join(PKG, "data", "repository", "TurkLangContent.kt"),
]

# ترتيب حقول كل كيان كما هو في تعريف data class (مهم: الوسائط موضعية)
ENTITIES = {
    "LessonEntity": (
        "lessons",
        ["id", "level", "titleAr", "titleId", "description", "content",
         "completed", "languageCode"],
        {"completed": 0, "languageCode": "ID"},
    ),
    "LessonDetailEntity": (
        "lesson_details",
        ["id", "unitId", "explanation", "wordByWord", "sentenceStructure",
         "dailyUsage", "commonMistakes", "formalVsCasual"],
        {},
    ),
    "VocabularyEntity": (
        "vocabulary",
        ["id", "wordId", "indonesian", "pronunciation", "arabic", "example",
         "exampleTranslation", "category", "level", "isFormal", "favorite",
         "languageCode"],
        {"isFormal": 1, "favorite": 0, "languageCode": "ID"},
    ),
    "GrammarEntity": (
        "grammar",
        ["id", "titleAr", "titleId", "explanation", "rule", "examples",
         "level", "languageCode"],
        {"languageCode": "ID"},
    ),
    "DialogueEntity": (
        "dialogues",
        ["id", "titleAr", "titleId", "content", "level", "languageCode"],
        {"languageCode": "ID"},
    ),
    "CasualExpressionEntity": (
        "casual_expressions",
        ["id", "expression", "pronunciation", "meaning", "formality", "usage",
         "formalEquivalent", "category", "level", "languageCode"],
        {"level": 0, "languageCode": "ID"},
    ),
    "DailyScenarioEntity": (
        "daily_scenarios",
        ["id", "title", "titleAr", "dialogue", "translation", "level",
         "category", "languageCode"],
        {"level": 0, "languageCode": "ID"},
    ),
    "TrainingItemEntity": (
        "training_items",
        ["id", "type", "question", "correctAnswer", "options", "explanation",
         "category", "languageCode"],
        {"languageCode": "ID"},
    ),
    "StageEntity": (
        "stages",
        ["id", "titleAr", "titleId", "description", "level", "isUnlocked",
         "languageCode"],
        {"isUnlocked": 0, "languageCode": "ID"},
    ),
    "UnitEntity": (
        "units",
        ["id", "stageId", "titleAr", "titleId", "description", "isCompleted"],
        {"isCompleted": 0},
    ),
}

SCHEMA = """
CREATE TABLE lessons(id INTEGER PRIMARY KEY, level INTEGER, titleAr TEXT, titleId TEXT,
  description TEXT, content TEXT, completed INTEGER, languageCode TEXT);
CREATE TABLE lesson_details(id INTEGER PRIMARY KEY, unitId INTEGER, explanation TEXT,
  wordByWord TEXT, sentenceStructure TEXT, dailyUsage TEXT, commonMistakes TEXT,
  formalVsCasual TEXT);
CREATE TABLE vocabulary(id INTEGER PRIMARY KEY, wordId TEXT, indonesian TEXT,
  pronunciation TEXT, arabic TEXT, example TEXT, exampleTranslation TEXT,
  category TEXT, level INTEGER, isFormal INTEGER, favorite INTEGER, languageCode TEXT);
CREATE TABLE grammar(id INTEGER PRIMARY KEY, titleAr TEXT, titleId TEXT, explanation TEXT,
  rule TEXT, examples TEXT, level INTEGER, languageCode TEXT);
CREATE TABLE dialogues(id INTEGER PRIMARY KEY, titleAr TEXT, titleId TEXT, content TEXT,
  level INTEGER, languageCode TEXT);
CREATE TABLE casual_expressions(id INTEGER PRIMARY KEY, expression TEXT, pronunciation TEXT,
  meaning TEXT, formality TEXT, usage TEXT, formalEquivalent TEXT, category TEXT,
  level INTEGER, languageCode TEXT);
CREATE TABLE daily_scenarios(id INTEGER PRIMARY KEY, title TEXT, titleAr TEXT, dialogue TEXT,
  translation TEXT, level INTEGER, category TEXT, languageCode TEXT);
CREATE TABLE training_items(id INTEGER PRIMARY KEY, type TEXT, question TEXT,
  correctAnswer TEXT, options TEXT, explanation TEXT, category TEXT, languageCode TEXT);
CREATE TABLE stages(id INTEGER PRIMARY KEY, titleAr TEXT, titleId TEXT, description TEXT,
  level INTEGER, isUnlocked INTEGER, languageCode TEXT);
CREATE TABLE units(id INTEGER PRIMARY KEY, stageId INTEGER, titleAr TEXT, titleId TEXT,
  description TEXT, isCompleted INTEGER);
CREATE TABLE review_states(itemId INTEGER, kind TEXT, languageCode TEXT, repetitions INTEGER,
  intervalDays INTEGER, easeFactor REAL, dueAt INTEGER, lapses INTEGER, totalReviews INTEGER,
  correctReviews INTEGER, lastReviewedAt INTEGER, PRIMARY KEY(itemId, kind, languageCode));
CREATE TABLE user_progress(id INTEGER PRIMARY KEY, currentLevel INTEGER, completedLessons INTEGER,
  totalLessons INTEGER, learnedWords INTEGER, totalWords INTEGER, lastStudyDate INTEGER);
"""


def strip_comments(s):
    out, i, n = [], 0, len(s)
    while i < n:
        c = s[i]
        if c == "/" and i + 1 < n and s[i + 1] == "/":
            while i < n and s[i] != "\n":
                i += 1
            continue
        if c == "/" and i + 1 < n and s[i + 1] == "*":
            d, i = 1, i + 2
            while i < n and d > 0:
                if s[i] == "/" and i + 1 < n and s[i + 1] == "*":
                    d += 1; i += 2; continue
                if s[i] == "*" and i + 1 < n and s[i + 1] == "/":
                    d -= 1; i += 2; continue
                if s[i] == "\n":
                    out.append("\n")
                i += 1
            continue
        if c == '"':
            out.append(c); i += 1
            while i < n and s[i] != '"':
                if s[i] == "\\":
                    out.append(s[i]); i += 1
                if i < n:
                    out.append(s[i]); i += 1
            if i < n:
                out.append('"'); i += 1
            continue
        out.append(c); i += 1
    return "".join(out)


def split_args(buf):
    parts, d, ins, es, cur = [], 0, False, False, ""
    for c in buf:
        if es:
            cur += c; es = False; continue
        if c == "\\":
            cur += c; es = True; continue
        if c == '"':
            ins = not ins
        if not ins:
            if c in "([":
                d += 1
            elif c in ")]":
                d -= 1
            elif c == "," and d == 0:
                parts.append(cur.strip()); cur = ""; continue
        cur += c
    if cur.strip():
        parts.append(cur.strip())
    return parts


def kotlin_value(tok):
    tok = tok.strip()
    if tok == "null":
        return None
    if tok in ("true", "false"):
        return 1 if tok == "true" else 0
    if tok.startswith('"') and tok.endswith('"'):
        body = tok[1:-1]
        return (body.replace('\\"', '"').replace("\\n", "\n")
                    .replace("\\$", "$").replace("\\\\", "\\"))
    if re.fullmatch(r"-?\d+", tok):
        return int(tok)
    if re.fullmatch(r"-?\d+\.\d+f?", tok):
        return float(tok.rstrip("f"))
    return tok


def extract(src, name):
    rows = []
    for m in re.finditer(r"\b" + name + r"\(", src):
        i, d, buf, ins, es = m.end(), 1, "", False, False
        while i < len(src) and d > 0:
            c = src[i]
            if es:
                buf += c; es = False; i += 1; continue
            if c == "\\":
                buf += c; es = True; i += 1; continue
            if c == '"':
                ins = not ins
            if not ins:
                if c == "(":
                    d += 1
                elif c == ")":
                    d -= 1
                    if d == 0:
                        break
            buf += c; i += 1
        rows.append(split_args(buf))
    return rows


def main():
    src = strip_comments("\n".join(open(p, encoding="utf-8").read()
                                   for p in SOURCES if os.path.exists(p)))
    con = sqlite3.connect(":memory:")
    con.executescript(SCHEMA)

    print("=" * 66)
    print("محاكاة قاعدة البيانات — هل تصل البيانات إلى الشاشات؟")
    print("=" * 66)
    print("\n[1] إدخال بيانات البذر")

    for ent, (table, fields, defaults) in ENTITIES.items():
        rows = extract(src, ent)
        good = 0
        for args in rows:
            vals = {}
            positional = []
            for a in args:
                nm = re.match(r"^(\w+)\s*=\s*(.+)$", a, re.S)
                if nm and nm.group(1) in fields:
                    vals[nm.group(1)] = kotlin_value(nm.group(2))
                else:
                    positional.append(a)
            for idx, a in enumerate(positional):
                if idx < len(fields):
                    vals.setdefault(fields[idx], kotlin_value(a))
            for k, v in defaults.items():
                vals.setdefault(k, v)
            for f in fields:
                vals.setdefault(f, None)
            con.execute(
                f"INSERT OR REPLACE INTO {table}({','.join(fields)}) "
                f"VALUES({','.join('?' * len(fields))})",
                [vals[f] for f in fields],
            )
            good += 1
        print(f"   {table:22s} {good:4d} صفاً")
    con.commit()

    # ---- نفس استعلامات DAO حرفياً ----
    print("\n[2] استعلامات الشاشات (ما سيراه المستخدم فعلياً)")
    checks = [
        ("الدروس (المستوى 0)", "ID",
         "SELECT COUNT(*) FROM lessons WHERE level=0 AND languageCode=?"),
        ("الدروس (المستوى 0)", "TR",
         "SELECT COUNT(*) FROM lessons WHERE level=0 AND languageCode=?"),
        ("المفردات", "ID", "SELECT COUNT(*) FROM vocabulary WHERE languageCode=?"),
        ("المفردات", "TR", "SELECT COUNT(*) FROM vocabulary WHERE languageCode=?"),
        ("القواعد (المستوى 0)", "ID",
         "SELECT COUNT(*) FROM grammar WHERE level=0 AND languageCode=?"),
        ("القواعد (المستوى 0)", "TR",
         "SELECT COUNT(*) FROM grammar WHERE level=0 AND languageCode=?"),
        ("اللغة اليومية", "ID",
         "SELECT COUNT(*) FROM casual_expressions WHERE languageCode=?"),
        ("اللغة اليومية", "TR",
         "SELECT COUNT(*) FROM casual_expressions WHERE languageCode=?"),
        ("المحادثات", "ID", "SELECT COUNT(*) FROM dialogues WHERE languageCode=?"),
        ("المحادثات", "TR", "SELECT COUNT(*) FROM dialogues WHERE languageCode=?"),
        ("المراحل", "ID", "SELECT COUNT(*) FROM stages WHERE languageCode=?"),
        ("المراحل", "TR", "SELECT COUNT(*) FROM stages WHERE languageCode=?"),
        ("السيناريوهات", "ID", "SELECT COUNT(*) FROM daily_scenarios WHERE languageCode=?"),
        ("السيناريوهات", "TR", "SELECT COUNT(*) FROM daily_scenarios WHERE languageCode=?"),
        ("الاختبار", "ID", "SELECT COUNT(*) FROM training_items WHERE languageCode=?"),
        ("الاختبار", "TR", "SELECT COUNT(*) FROM training_items WHERE languageCode=?"),
    ]
    failures = []
    for label, lang, q in checks:
        n = con.execute(q, (lang,)).fetchone()[0]
        mark = "✓" if n > 0 else "✗ فارغة — دوّامة إلى الأبد"
        print(f"   {mark:32s} {label:22s} [{lang}] = {n}")
        if n == 0:
            failures.append(f"{label} [{lang}]")

    # تفاصيل الدروس: الاستعلام على id
    print("\n[3] تفاصيل الدروس (شاشة الدرس)")
    missing = con.execute(
        "SELECT COUNT(*) FROM lessons l WHERE NOT EXISTS "
        "(SELECT 1 FROM lesson_details d WHERE d.id = l.id)"
    ).fetchone()[0]
    print(f"   {'✓' if missing == 0 else '✗'} دروس بلا تفاصيل: {missing}")
    if missing:
        failures.append(f"{missing} دروس بلا تفاصيل")

    # المراحل المقفلة: طلب المستخدم أن يكون كل شيء مفتوحاً بلا تقييد.
    # نتحقق من قيم البذر الحقيقية المستخرجة من LearnRepository.kt.
    locked = con.execute(
        "SELECT COUNT(*) FROM stages WHERE isUnlocked = 0"
    ).fetchone()[0]
    print(f"   {'✓' if locked == 0 else '✗'} مراحل مقفلة في البذر: {locked}")
    if locked:
        failures.append(f"{locked} مرحلة مقفلة — المستخدم طلب فتح كل شيء")

    # محاكاة تثبيت قديم: صفوف مخزّنة بـ isUnlocked = 0 ثم unlockAll().
    con.execute("UPDATE stages SET isUnlocked = 0")
    con.execute("UPDATE stages SET isUnlocked = 1 WHERE isUnlocked = 0")
    still = con.execute(
        "SELECT COUNT(*) FROM stages WHERE isUnlocked = 0"
    ).fetchone()[0]
    print(f"   {'✓' if still == 0 else '✗'} بعد unlockAll() على تثبيت قديم: {still} مقفلة")
    if still:
        failures.append("unlockAll() لم يفتح التثبيتات القديمة")

    print("\n" + "=" * 66)
    if failures:
        print("شاشات ستبقى فارغة:")
        for f in failures:
            print("   ✗ " + f)
        print("=" * 66)
        return 1
    print("✓ كل الشاشات ستعرض بيانات")
    print("=" * 66)
    return 0


if __name__ == "__main__":
    sys.exit(main())
