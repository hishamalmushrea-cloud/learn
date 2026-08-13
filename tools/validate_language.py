#!/usr/bin/env python3
"""
مُدقِّق نقاء اللغة في محتوى IndoLearn.

لماذا: ثبت عملياً وجود تلوّث بين اللغات في الكود المُسلَّم، مثل:
  - "Hari Pazartesi." — كلمة إندونيسية (Hari) داخل مثال تركي.
  - "Bu ucuz bir كتاب." — كلمة عربية داخل جملة تركية.
  - "kardeşem" — لاحقة ملكية خاطئة (الصحيح kardeşim).

هذه أخطاء لا يلتقطها أي مترجم؛ يلتقطها فقط فحص المحتوى.

الاستخدام: python3 tools/validate_language.py
"""

import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = os.path.join(
    ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn",
    "data", "repository", "LearnRepository.kt",
)

src = open(REPO, encoding="utf-8").read()

ARABIC = re.compile(r"[\u0600-\u06FF]")
TURKISH_ONLY = set("çğışöüÇĞİŞÖÜ")

# كلمات إندونيسية شائعة لا يمكن أن تظهر في جملة تركية سليمة
INDONESIAN_MARKERS = {
    "hari", "saya", "nasi", "aku", "kamu", "tidak", "bukan", "adalah",
    "dengan", "yang", "ini", "itu", "dan", "makan", "minum", "pergi",
    "rumah", "bagus", "murah", "mahal", "sekali", "banget", "sudah",
    "belum", "akan", "sedang", "bisa", "mau", "harus",
}

# كلمات تركية شائعة لا يمكن أن تظهر في جملة إندونيسية سليمة
TURKISH_MARKERS = {
    "ben", "sen", "biz", "siz", "onlar", "değil", "için", "çok",
    "güzel", "evet", "hayır", "teşekkür", "lütfen", "merhaba",
}

errors: list[str] = []
warnings: list[str] = []


def rows(entity: str):
    """يُعيد (رقم السطر، قائمة الوسائط النصية) لكل استدعاء للكيان."""
    out = []
    for m in re.finditer(r"\b" + entity + r"\(", src):
        i, depth, buf, ins, esc = m.end(), 1, "", False, False
        while i < len(src) and depth > 0:
            c = src[i]
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
        line = src[: m.start()].count("\n") + 1
        strings = re.findall(r'"((?:[^"\\]|\\.)*)"', buf)
        out.append((line, strings, buf))
    return out


print("فحص نقاء اللغة")
print("-" * 60)

# ---- 1. صفوف المفردات ----
for line, strings, buf in rows("VocabularyEntity"):
    is_tr = '"TR"' in buf
    if len(strings) < 6:
        continue
    word, pron, arabic, example = strings[1], strings[2], strings[3], strings[4]

    if is_tr:
        # لا يجوز وجود حروف عربية داخل المثال التركي
        if ARABIC.search(example):
            errors.append(f"سطر {line}: حروف عربية داخل مثال تركي: {example!r}")
        # لا يجوز وجود كلمات إندونيسية
        toks = set(re.findall(r"[A-Za-zçğışöüÇĞİŞÖÜ]+", example.lower()))
        bad = toks & INDONESIAN_MARKERS
        if bad:
            errors.append(f"سطر {line}: كلمات إندونيسية في مثال تركي {sorted(bad)}: {example!r}")
    else:
        # الإندونيسية لا تستخدم الحروف التركية الخاصة
        if any(ch in TURKISH_ONLY for ch in word):
            errors.append(f"سطر {line}: حروف تركية في كلمة إندونيسية: {word!r}")
        if ARABIC.search(example):
            errors.append(f"سطر {line}: حروف عربية داخل مثال إندونيسي: {example!r}")
        toks = set(re.findall(r"[A-Za-zçğışöüÇĞİŞÖÜ]+", example.lower()))
        bad = toks & TURKISH_MARKERS
        if bad:
            warnings.append(f"سطر {line}: كلمات تركية محتملة في مثال إندونيسي {sorted(bad)}")

    # النطق العربي يجب أن يكون عربياً فعلاً
    if pron and not ARABIC.search(pron):
        warnings.append(f"سطر {line}: حقل النطق ليس بالعربية: {pron!r}")

# ---- 2. التعبيرات والسيناريوهات والتدريبات ----
for entity in ("CasualExpressionEntity", "DailyScenarioEntity", "TrainingItemEntity"):
    for line, strings, buf in rows(entity):
        is_tr = '"TR"' in buf
        for st in strings:
            # نتجاهل اللواحق التركية المكتوبة بشرطة (-dan, -de, -ler ...)
            # وإلا حُسبت "-dan" خطأً على أنها الكلمة الإندونيسية "dan".
            cleaned = re.sub(r"-[A-Za-zçğışöüÇĞİŞÖÜ]+", " ", st)
            latin = re.findall(r"[A-Za-zçğışöüÇĞİŞÖÜ]+", cleaned)
            toks = {t.lower() for t in latin}
            if is_tr and (toks & INDONESIAN_MARKERS):
                errors.append(
                    f"سطر {line} ({entity}): كلمات إندونيسية في محتوى تركي "
                    f"{sorted(toks & INDONESIAN_MARKERS)}"
                )
            if not is_tr and any(ch in TURKISH_ONLY for ch in st):
                warnings.append(f"سطر {line} ({entity}): حروف تركية في محتوى إندونيسي: {st[:40]!r}")

# ---- 3. أخطاء صرفية تركية معروفة ----
BAD_TURKISH = {
    "kardeşem": "kardeşim",
    "arkadaşem": "arkadaşım",
    "evem": "evim",
    "annem çok iyi": None,  # صحيح، للتوثيق فقط
}
for wrong, right in BAD_TURKISH.items():
    if right and re.search(r"\b" + wrong + r"\b", src):
        errors.append(f"صرف تركي خاطئ: {wrong!r} — الصحيح {right!r}")

print(f"تحذيرات: {len(warnings)}")
for w in warnings[:15]:
    print("  ⚠ " + w)
if len(warnings) > 15:
    print(f"  ... و{len(warnings) - 15} تحذيراً آخر")

print(f"\nأخطاء: {len(errors)}")
for e in errors:
    print("  ✗ " + e)

print("-" * 60)
if errors:
    sys.exit(1)
print("✓ لا يوجد تلوّث بين اللغات")
