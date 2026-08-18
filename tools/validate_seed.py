#!/usr/bin/env python3
"""
مُدقِّق سلامة بيانات البذر في IndoLearn.

لماذا يوجد هذا الملف:
بيانات المحتوى مكتوبة يدوياً داخل `LearnRepository.seedInitialData()` بآلاف الأسطر،
ولا يمكن تشغيل Room أو Gradle في بيئة التطوير هذه للتحقق منها.
وقد ثبت عملياً أن أخطاء حقيقية تسللت إلى الكود المُسلَّم:
  - صفّان يمرران عدد وسائط خاطئاً (خطأ تجميع فعلي).
  - 27 صفاً من تفاصيل الدروس مربوطة بمفتاح خاطئ.

هذا المدقِّق يمنع تكرار ذلك: يفحص التفرّد، وسلامة المفاتيح الأجنبية،
وتطابق عدد الوسائط مع تعريف الكيان، ووسم اللغة، وقابلية وصول المحتوى.

الاستخدام:
    python3 tools/validate_seed.py
الخروج بقيمة غير صفرية عند وجود أي خطأ (صالح للاستخدام في CI).
"""

import os
import re
import sys
from collections import Counter, defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn")
REPO = os.path.join(SRC, "data", "repository", "LearnRepository.kt")
ENTITY_DIR = os.path.join(SRC, "data", "local", "entity")

errors: list[str] = []
warnings: list[str] = []


def strip_comments(s: str) -> str:
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
                    d += 1
                    i += 2
                    continue
                if s[i] == "*" and i + 1 < n and s[i + 1] == "/":
                    d -= 1
                    i += 2
                    continue
                if s[i] == "\n":
                    out.append("\n")
                i += 1
            continue
        if c == '"':
            out.append(c)
            i += 1
            while i < n and s[i] != '"':
                if s[i] == "\\":
                    out.append(s[i])
                    i += 1
                if i < n:
                    out.append(s[i])
                    i += 1
            if i < n:
                out.append('"')
                i += 1
            continue
        out.append(c)
        i += 1
    return "".join(out)


def split_top(buf: str) -> list[str]:
    parts, d, ins, es, cur = [], 0, False, False, ""
    for c in buf:
        if es:
            cur += c
            es = False
            continue
        if c == "\\":
            cur += c
            es = True
            continue
        if c == '"':
            ins = not ins
        if not ins:
            if c in "([<":
                d += 1
            elif c in ")]>":
                d -= 1
            elif c == "," and d == 0:
                parts.append(cur)
                cur = ""
                continue
        cur += c
    if cur.strip():
        parts.append(cur)
    return parts


def entity_signature(name: str):
    path = os.path.join(ENTITY_DIR, name + ".kt")
    if not os.path.exists(path):
        return None
    s = strip_comments(open(path, encoding="utf-8").read())
    m = re.search(r"data class \w+\s*\(", s)
    if not m:
        return None
    d, buf = 0, ""
    for ch in s[m.end() - 1:]:
        if ch == "(":
            d += 1
            if d == 1:
                continue
        if ch == ")":
            d -= 1
            if d == 0:
                break
        buf += ch
    parts = split_top(buf)
    names = []
    for p in parts:
        mm = re.search(r"(?:val|var)\s+(\w+)", p)
        names.append(mm.group(1) if mm else "?")
    return {
        "total": len(parts),
        "required": sum(1 for x in parts if "=" not in x),
        "names": names,
    }


GENERATED = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "IndoLangContent.kt",
)

# نفحص المحتوى المُولَّد من موسوعة indolang مع المحتوى المكتوب يدوياً معاً،
# وإلا مرّت أخطاء المستورد (مفاتيح مكررة، إجابة خارج الخيارات) بلا رقابة.
_GEN_TR = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "TurkLangContent.kt",
)
_A0 = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "A0PronunciationContent.kt",
)
_SC = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "IndoScenariosA1.kt",
)
_sources = [open(REPO, encoding="utf-8").read()]
for _g in (GENERATED, _GEN_TR, _A0, _SC):
    if os.path.exists(_g):
        _sources.append(open(_g, encoding="utf-8").read())
REPO_SRC = strip_comments("\n".join(_sources))


def calls(name: str):
    out = []
    for m in re.finditer(r"\b" + re.escape(name) + r"\(", REPO_SRC):
        i, depth, buf, ins, esc = m.end(), 1, "", False, False
        while i < len(REPO_SRC) and depth > 0:
            c = REPO_SRC[i]
            if esc:
                buf += c
                esc = False
                i += 1
                continue
            if c == "\\":
                buf += c
                esc = True
                i += 1
                continue
            if c == '"':
                ins = not ins
            if not ins:
                if c == "(":
                    depth += 1
                elif c == ")":
                    depth -= 1
                    if depth == 0:
                        break
            buf += c
            i += 1
        line = REPO_SRC[: m.start()].count("\n") + 1
        out.append({"line": line, "args": [a.strip() for a in split_top(buf)]})
    return out


ENTITIES = [
    "LessonEntity", "LessonDetailEntity", "VocabularyEntity", "GrammarEntity",
    "DialogueEntity", "CasualExpressionEntity", "DailyScenarioEntity",
    "TrainingItemEntity", "StageEntity", "UnitEntity",
]

sig = {e: entity_signature(e) for e in ENTITIES}
data = {e: calls(e) for e in ENTITIES}


def unquote(v: str) -> str:
    v = v.strip()
    return v[1:-1] if len(v) >= 2 and v[0] == '"' and v[-1] == '"' else v


def field(entity: str, call: dict, fname: str):
    """قيمة حقل بالاسم، مع دعم الوسائط الموضعية والمُسمّاة."""
    names = sig[entity]["names"]
    for a in call["args"]:
        m = re.match(r"^(\w+)\s*=\s*(.+)$", a, re.S)
        if m and m.group(1) == fname:
            return unquote(m.group(2))
    if fname in names:
        idx = names.index(fname)
        if idx < len(call["args"]):
            a = call["args"][idx]
            if not re.match(r"^\w+\s*=", a):
                return unquote(a)
    return None


# ---------- 1. arity ----------
print("1) عدد الوسائط في بواني الكيانات")
arity_bad = 0
for e in ENTITIES:
    if not sig[e]:
        continue
    for c in data[e]:
        n = len(c["args"])
        if n < sig[e]["required"] or n > sig[e]["total"]:
            errors.append(
                f"{e} سطر {c['line']}: {n} وسيطاً "
                f"(المطلوب {sig[e]['required']}..{sig[e]['total']})"
            )
            arity_bad += 1
print(f"   {'✓' if arity_bad == 0 else '✗'} أخطاء عدد الوسائط: {arity_bad}")

# ---------- 2. primary keys ----------
print("2) تفرّد المفاتيح الأساسية")
pk_bad = 0
for e in ENTITIES:
    if not sig[e] or not data[e]:
        continue
    ids = [field(e, c, "id") for c in data[e]]
    ids = [i for i in ids if i is not None]
    dup = [k for k, v in Counter(ids).items() if v > 1]
    if dup:
        errors.append(f"{e}: مفاتيح مكررة {dup[:10]}")
        pk_bad += 1
print(f"   {'✓' if pk_bad == 0 else '✗'} كيانات بمفاتيح مكررة: {pk_bad}")

# ---------- 3. lesson <-> detail integrity (the P0 bug) ----------
print("3) ربط الدروس بتفاصيلها")
lesson_ids = {field("LessonEntity", c, "id") for c in data["LessonEntity"]}
detail_ids = [field("LessonDetailEntity", c, "id") for c in data["LessonDetailEntity"]]
missing = sorted(lesson_ids - set(detail_ids), key=lambda x: int(x))
orphan = sorted(set(detail_ids) - lesson_ids, key=lambda x: int(x))
if missing:
    errors.append(f"دروس بلا تفاصيل (ستعرض 'لم يتم العثور على الدرس'): {missing}")
if orphan:
    errors.append(f"تفاصيل بلا درس مقابل: {orphan}")
print(f"   {'✓' if not (missing or orphan) else '✗'} "
      f"دروس={len(lesson_ids)} تفاصيل={len(detail_ids)} "
      f"مفقودة={len(missing)} يتيمة={len(orphan)}")

# unitId must point at a real unit
unit_ids = {field("UnitEntity", c, "id") for c in data["UnitEntity"]}
bad_units = sorted(
    {field("LessonDetailEntity", c, "unitId") for c in data["LessonDetailEntity"]} - unit_ids,
    key=lambda x: int(x) if x and x.isdigit() else 0,
)
# التركية تستخدم unitId = id لعدم وجود جدول وحدات تركي بعد
bad_units = [u for u in bad_units if u and int(u) < 200]
if bad_units:
    warnings.append(f"unitId لا يطابق أي وحدة: {bad_units}")

# ---------- 4. language tagging ----------
print("4) وسم اللغة")
lang_bad = 0
for e in ["LessonEntity", "VocabularyEntity", "GrammarEntity", "DialogueEntity",
          "StageEntity", "TrainingItemEntity", "CasualExpressionEntity",
          "DailyScenarioEntity"]:
    if not sig[e] or "languageCode" not in sig[e]["names"]:
        errors.append(f"{e}: لا يحتوي على حقل languageCode")
        lang_bad += 1
        continue
    langs = Counter(field(e, c, "languageCode") or "ID" for c in data[e])
    unknown = [k for k in langs if k not in ("ID", "TR")]
    if unknown:
        errors.append(f"{e}: رموز لغة غير معروفة {unknown}")
        lang_bad += 1
    print(f"   {e:26s} {dict(langs)}")
print(f"   {'✓' if lang_bad == 0 else '✗'} مشاكل وسم اللغة: {lang_bad}")

# ---------- 5. per-language completeness ----------
print("5) اكتمال المحتوى لكل لغة")
for lang in ("ID", "TR"):
    counts = {}
    for e in ["LessonEntity", "VocabularyEntity", "GrammarEntity", "TrainingItemEntity",
              "CasualExpressionEntity", "DailyScenarioEntity", "DialogueEntity"]:
        if not sig[e] or "languageCode" not in sig[e]["names"]:
            continue
        counts[e.replace("Entity", "")] = sum(
            1 for c in data[e] if (field(e, c, "languageCode") or "ID") == lang
        )
    print(f"   {lang}: {counts}")
    for k, v in counts.items():
        if v == 0:
            errors.append(f"اللغة {lang} لا تحتوي على أي {k} — الشاشة ستظهر فارغة")

# ---------- 6. duplicate surface forms ----------
print("6) تكرار المفردات")
by_lang = defaultdict(list)
for c in data["VocabularyEntity"]:
    by_lang[field("VocabularyEntity", c, "languageCode") or "ID"].append(
        field("VocabularyEntity", c, "indonesian")
    )
for lang, words in by_lang.items():
    dup = {k: v for k, v in Counter(words).items() if v > 1}
    if dup:
        top = sorted(dup.items(), key=lambda x: -x[1])[:6]
        warnings.append(
            f"{lang}: {len(dup)} كلمة مكررة من أصل {len(set(words))} فريدة "
            f"(الأكثر: {top})"
        )
    print(f"   {lang}: {len(words)} مدخلاً، {len(set(words))} فريدة، {len(dup)} مكررة")

# ---------- 7. quiz answer sanity ----------
print("7) سلامة أسئلة الاختبار")
q_bad = 0
for c in data["TrainingItemEntity"]:
    opts = field("TrainingItemEntity", c, "options") or ""
    ans = field("TrainingItemEntity", c, "correctAnswer") or ""
    if opts.strip():
        choices = [o.strip() for o in opts.split(",") if o.strip()]
        if ans not in choices:
            errors.append(
                f"TrainingItem سطر {c['line']}: الإجابة الصحيحة ليست ضمن الخيارات "
                f"(answer={ans!r})"
            )
            q_bad += 1
        if len(choices) != len(set(choices)):
            warnings.append(f"TrainingItem سطر {c['line']}: خيارات مكررة")
print(f"   {'✓' if q_bad == 0 else '✗'} أسئلة إجابتها خارج الخيارات: {q_bad}")

# ---------- 8. stage reachability ----------
print("8) قابلية الوصول للمراحل")
for lang in ("ID", "TR"):
    stages = [c for c in data["StageEntity"]
              if (field("StageEntity", c, "languageCode") or "ID") == lang]
    levels = {field("StageEntity", c, "level") for c in stages}
    lesson_levels = {
        field("LessonEntity", c, "level")
        for c in data["LessonEntity"]
        if (field("LessonEntity", c, "languageCode") or "ID") == lang
    }
    empty = sorted(levels - lesson_levels)
    if empty:
        warnings.append(f"{lang}: مراحل بلا دروس (level={empty}) ستفتح على شاشة فارغة")
    print(f"   {lang}: مراحل={len(stages)} مستويات_المراحل={sorted(levels)} "
          f"مستويات_الدروس={sorted(lesson_levels)}")

# ---------- report ----------
print("\n" + "=" * 62)
if warnings:
    print(f"تحذيرات ({len(warnings)}):")
    for w in warnings:
        print("  ⚠ " + w)
if errors:
    print(f"\nأخطاء ({len(errors)}):")
    for e in errors:
        print("  ✗ " + e)
    print("=" * 62)
    sys.exit(1)
print("✓ اجتازت جميع فحوص سلامة البيانات")
print("=" * 62)
