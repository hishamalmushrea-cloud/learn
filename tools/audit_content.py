#!/usr/bin/env python3
"""
تدقيق المحتوى التعليمي نفسه — لا البنية ولا الكود.

يجيب عن الأسئلة التي لا يستطيع أي فحص بنيوي الإجابة عنها:
  • هل قسم "الأرقام" يحتوي أرقاماً فعلاً؟ هل "الأفعال" أفعال؟
  • هل توجد كلمة مكررة بمعنيين مختلفين، أو معنى مكرر بكلمتين؟
  • هل المثال يحتوي الكلمة التي يفترض أنه يمثّلها؟
  • هل الحقول فارغة أو مطابقة لبعضها (نطق = الكلمة نفسها)؟
  • هل الترجمة العربية تحتوي حروفاً لاتينية (تسرّب ترجمة)؟
  • هل التصنيفات متسقة أم فيها تهجئات متعددة للمفهوم نفسه؟

يعيد استخدام محلل Kotlin الموجود في simulate_db.py بدل تكرار المنطق.

الاستخدام: python3 tools/audit_content.py [--strict]
  بلا --strict: تقرير فقط (خروج 0) — للاستكشاف.
  مع --strict:  يفشل عند وجود عيوب من فئة BLOCKER.
"""

import os
import re
import sys
import unicodedata

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from simulate_db import SOURCES, extract, kotlin_value, strip_comments  # noqa: E402

ARABIC = re.compile(r"[\u0600-\u06FF]")
LATIN = re.compile(r"[A-Za-z]")

# تعريفات الفئات: ما الذي يجب أن يكون داخل كل تصنيف مفردات.
# القاعدة تُعبَّر عنها كدالة على الصف، حتى لا نخمّن من الاسم.
ARABIC_DIGIT_WORDS = {
    "صفر", "واحد", "اثنان", "اثنين", "ثلاثة", "أربعة", "خمسة", "ستة",
    "سبعة", "ثمانية", "تسعة", "عشرة", "مئة", "مائة", "ألف", "مليون",
    "عشرون", "ثلاثون", "أربعون", "خمسون", "أحد عشر", "اثنا عشر",
}


def is_number_word(ar):
    ar = ar.strip()
    if re.search(r"\d", ar):
        return True
    return any(w in ar for w in ARABIC_DIGIT_WORDS)


def is_verb_gloss(ar):
    """الترجمة العربية لفعل تُكتب في هذا المشروع بصيغة المضارع: 'يأكل'."""
    ar = ar.strip()
    # قد تحتوي على بدائل: "يحب / يهتم بـ"
    parts = [p.strip() for p in re.split(r"[/،,]", ar) if p.strip()]
    return any(p.startswith(("ي", "ت")) for p in parts)


# التركية إلصاقية: المصدر ينتهي بـ -mak/-mek وتُحذف اللاحقة عند التصريف،
# وقد يلين الحرف الأخير للجذر (t→d): gitmek → gidiyorum.
# لذلك المطابقة الحرفية للكلمة داخل المثال تعطي إنذارات كاذبة.
def _norm(text):
    """توحيد النص قبل المطابقة.

    فخّ يونيكودي حقيقي: الحرف التركي 'İ' (I بنقطة) عند تحويله لحروف صغيرة
    في بايثون يصبح 'i' + نقطة عائمة (U+0307)، فتفشل مطابقة 'iki' داخل
    'İki ekmek'. نحذف العلامات المركّبة بعد التفكيك لتصحيح ذلك.
    """
    t = unicodedata.normalize("NFD", text.lower())
    return "".join(ch for ch in t if not unicodedata.combining(ch))


SOFTEN = {"t": "d", "k": "ğ", "p": "b", "ç": "c"}


def _stems(word, lang):
    """يولّد الجذور المحتملة لكلمة، حسب صرف اللغة."""
    w = _norm(word.strip())
    out = {w}
    if lang == "TR":
        for suf in ("mak", "mek"):
            if w.endswith(suf) and len(w) > len(suf) + 1:
                stem = w[: -len(suf)]
                out.add(stem)
                if stem and stem[-1] in SOFTEN:
                    out.add(stem[:-1] + SOFTEN[stem[-1]])
    else:
        # الإندونيسية: بادئات me-/ber-/di-/ter- ولواحق -kan/-i
        for pre in ("meng", "meny", "mem", "men", "ber", "ter", "di", "me"):
            if w.startswith(pre) and len(w) > len(pre) + 2:
                out.add(w[len(pre):])
        for suf in ("kan", "an", "i"):
            if w.endswith(suf) and len(w) > len(suf) + 2:
                out.add(w[: -len(suf)])
    # الكلمات القصيرة (ev, su) مشروعة — لا تُستبعد.
    return {s for s in out if len(s) >= 2}


def _example_contains(word, example, lang):
    """هل يظهر الجذر داخل المثال؟ يقبل التصريف واللواحق."""
    example = _norm(example)
    word = _norm(word)
    words_in_ex = re.findall(r"[\w\u00c0-\u024f]+", example)
    # الكلمات المكوّنة من حرف واحد (الضمير التركي "o" = هو/هي) تحتاج
    # مطابقة ككلمة كاملة، وإلا لن تُطابق أبداً بشرط الطول.
    if len(word) == 1:
        return word in words_in_ex

    for stem in _stems(word, lang):
        if stem in example:
            return True
        # التركية تحذف حرف العلة الأخير قبل -yor: olmak→oluyor, istemek→istiyor
        if lang == "TR" and len(stem) >= 3:
            trunc = stem[:-1]
            if len(trunc) >= 2 and any(t.startswith(trunc) for t in words_in_ex):
                return True
    # الكلمات المركبة: تكفي مطابقة أول جزء ذي معنى
    parts = [p for p in re.split(r"\s+", word.strip().lower()) if len(p) >= 2]
    return bool(parts) and all(
        any(st in example for st in _stems(p, lang)) for p in parts)


def load():
    src = strip_comments("\n".join(
        open(p, encoding="utf-8").read() for p in SOURCES if os.path.exists(p)))
    return src


def rows_of(src, name, fields, defaults):
    out = []
    for raw in extract(src, name):
        vals = [kotlin_value(v) for v in raw]
        row = dict(defaults)
        for i, v in enumerate(vals):
            if i < len(fields):
                row[fields[i]] = v
        # تجاهل الاستدعاءات المشوّهة (وسائط أقل من الحقول الإلزامية)
        if len(vals) < 3:
            continue
        out.append(row)
    return out


VOCAB_F = ["id", "wordId", "indonesian", "pronunciation", "arabic", "example",
           "exampleTranslation", "category", "level", "isFormal", "favorite",
           "languageCode"]
CASUAL_F = ["id", "expression", "pronunciation", "meaning", "formality",
            "usage", "formalEquivalent", "category", "level", "languageCode"]
GRAMMAR_F = ["id", "titleAr", "titleId", "explanation", "rule", "examples",
             "level", "languageCode"]


def main():
    strict = "--strict" in sys.argv
    src = load()
    vocab = rows_of(src, "VocabularyEntity", VOCAB_F,
                    {"isFormal": 1, "favorite": 0, "languageCode": "ID"})
    casual = rows_of(src, "CasualExpressionEntity", CASUAL_F,
                     {"level": 0, "languageCode": "ID"})
    grammar = rows_of(src, "GrammarEntity", GRAMMAR_F, {"languageCode": "ID"})

    blockers, warnings = [], []

    print("=" * 70)
    print("تدقيق المحتوى التعليمي")
    print("=" * 70)
    print(f"\nمفردات: {len(vocab)} | لغة يومية: {len(casual)} | قواعد: {len(grammar)}")

    # ---------- 1) صحة الحقول ----------
    print("\n[1] سلامة الحقول")
    empty = [v for v in vocab if not str(v.get("arabic", "")).strip()
             or not str(v.get("indonesian", "")).strip()]
    same_pron = [v for v in vocab
                 if str(v.get("pronunciation", "")).strip() ==
                 str(v.get("indonesian", "")).strip()]
    latin_in_ar = [v for v in vocab if LATIN.search(str(v.get("arabic", "")))]
    no_arabic = [v for v in vocab if not ARABIC.search(str(v.get("arabic", "")))]

    def rep(label, items, blocker=False, show=4):
        mark = "✗" if items else "✓"
        print(f"   {mark} {label}: {len(items)}")
        for it in items[:show]:
            ident = it.get("indonesian") or it.get("expression") or it.get("titleAr")
            print(f"        - [{it.get('languageCode')}#{it.get('id')}] {ident}")
        if items:
            (blockers if blocker else warnings).append(f"{label}: {len(items)}")

    rep("حقول فارغة", empty, blocker=True)
    rep("ترجمة عربية بلا حرف عربي", no_arabic, blocker=True)
    rep("حروف لاتينية داخل الترجمة العربية", latin_in_ar)
    rep("النطق مطابق للكلمة (بلا فائدة)", same_pron)

    # ---------- 2) التصنيف: هل المحتوى يطابق اسم قسمه؟ ----------
    print("\n[2] مطابقة المحتوى لتصنيفه")
    miscat = []
    for v in vocab:
        cat = str(v.get("category", "")).strip()
        ar = str(v.get("arabic", ""))
        if cat == "أرقام" and not is_number_word(ar):
            miscat.append((v, "مصنّف 'أرقام' لكنه ليس عدداً"))
        if cat == "أفعال" and not is_verb_gloss(ar):
            miscat.append((v, "مصنّف 'أفعال' لكن الترجمة ليست فعلاً مضارعاً"))
    print(f"   {'✗' if miscat else '✓'} كلمات في التصنيف الخطأ: {len(miscat)}")
    for v, why in miscat[:10]:
        print(f"        - [{v['languageCode']}#{v['id']}] {v['indonesian']} = "
              f"{v['arabic']} → {why}")
    if miscat:
        blockers.append(f"تصنيف خاطئ: {len(miscat)}")

    # ---------- 3) الجملة المثال ----------
    print("\n[3] الأمثلة")
    noex = [v for v in vocab if not str(v.get("example", "")).strip()]
    notrans = [v for v in vocab
               if str(v.get("example", "")).strip()
               and not str(v.get("exampleTranslation", "")).strip()]
    # هل المثال يحتوي الكلمة نفسها؟ (مطابقة على أساس الجذر لأن اللغة إلصاقية)
    orphan_ex = []
    for v in vocab:
        w = str(v.get("indonesian", "")).strip().lower()
        ex = str(v.get("example", "")).lower()
        if not w or not ex:
            continue
        if not _example_contains(w, ex, v.get("languageCode")):
            orphan_ex.append(v)
    rep("كلمات بلا مثال", noex, blocker=True)
    rep("مثال بلا ترجمة", notrans, blocker=True)
    rep("المثال لا يحتوي الكلمة", orphan_ex, show=6)

    # ---------- 4) التكرار ----------
    print("\n[4] التكرار")
    by_word = {}
    for v in vocab:
        key = (v.get("languageCode"), str(v.get("indonesian", "")).strip().lower())
        by_word.setdefault(key, []).append(v)
    dup_word = {k: g for k, g in by_word.items() if len(g) > 1}
    print(f"   {'✗' if dup_word else '✓'} كلمة مكررة: {len(dup_word)}")
    for (lang, w), g in list(dup_word.items())[:8]:
        glosses = {str(x.get("arabic", "")).strip() for x in g}
        cats = {str(x.get("category", "")).strip() for x in g}
        flag = "  ⚠ بمعانٍ مختلفة" if len(glosses) > 1 else ""
        print(f"        - [{lang}] {w} ×{len(g)} "
              f"ids={[x['id'] for x in g]} تصنيف={sorted(cats)}{flag}")
    if dup_word:
        warnings.append(f"كلمات مكررة: {len(dup_word)}")

    dup_ids = {}
    for v in vocab:
        dup_ids.setdefault(v.get("id"), []).append(v)
    clash = {k: g for k, g in dup_ids.items() if len(g) > 1}
    print(f"   {'✗' if clash else '✓'} تصادم معرّفات (PRIMARY KEY): {len(clash)}")
    if clash:
        for k, g in list(clash.items())[:6]:
            print(f"        - id={k}: {[x.get('indonesian') for x in g]}")
        blockers.append(f"تصادم معرّفات مفردات: {len(clash)}")

    # ---------- 5) اتساق التصنيفات ----------
    print("\n[5] التصنيفات المستخدمة فعلياً")
    for lang in ("ID", "TR"):
        cats = {}
        for v in vocab:
            if v.get("languageCode") == lang:
                cats[str(v.get("category", "")).strip()] = \
                    cats.get(str(v.get("category", "")).strip(), 0) + 1
        print(f"   [{lang}] {len(cats)} تصنيف:")
        for c, n in sorted(cats.items(), key=lambda x: -x[1]):
            print(f"        {n:>4}  {c}")

    # ---------- 6) تغطية الفئات الأساسية ----------
    print("\n[6] تغطية الفئات الأساسية للمتعلم المبتدئ")
    # الفئات التي لا يستطيع مبتدئ الاستغناء عنها.
    # "تعارف" ليست فئة مفردات: التعارف جُمَل كاملة (ما اسمك؟ من أين أنت؟)
    # ومكانها الصحيح جدول العبارات اليومية — ويُفحص هناك في القسم [7].
    ESSENTIAL = ["تحيات", "ضمائر", "أرقام", "سؤال", "نفي",
                 "أفعال", "صفات", "عائلة", "طعام", "أماكن", "وقت",
                 "ألوان", "أيام", "اتجاهات", "جسم", "مال"]
    for lang in ("ID", "TR"):
        have = {str(v.get("category", "")).strip()
                for v in vocab if v.get("languageCode") == lang}
        missing = [c for c in ESSENTIAL if c not in have]
        print(f"   [{lang}] ناقص: {', '.join(missing) if missing else 'لا شيء'}")
        if missing:
            warnings.append(f"[{lang}] فئات أساسية ناقصة: {len(missing)}")

    # ---------- 7) اللغة اليومية: درجة الرسمية ----------
    print("\n[7] اللغة اليومية")
    formal_vals = {}
    for c in casual:
        formal_vals[str(c.get("formality", "")).strip()] = \
            formal_vals.get(str(c.get("formality", "")).strip(), 0) + 1
    print(f"   قيم درجة الرسمية المستخدمة: {len(formal_vals)}")
    for k, n in sorted(formal_vals.items(), key=lambda x: -x[1]):
        print(f"        {n:>4}  {k!r}")
    no_usage = [c for c in casual if not str(c.get("usage", "")).strip()]
    rep("تعبير بلا شرح استخدام", no_usage)

    # التعارف: يجب أن يوجد كجُمَل في كل لغة (لا ككلمات مفردة).
    for lang in ("ID", "TR"):
        intro = [c for c in casual
                 if c.get("languageCode") == lang
                 and "تعارف" in str(c.get("category", ""))]
        mark = "✓" if intro else "✗"
        print(f"   {mark} [{lang}] عبارات تعارف: {len(intro)}")
        if not intro:
            warnings.append(f"[{lang}] لا توجد عبارات تعارف")

    dup_expr = {}
    for c in casual:
        k = (c.get("languageCode"), str(c.get("expression", "")).strip().lower())
        dup_expr.setdefault(k, []).append(c)
    dcasual = {k: g for k, g in dup_expr.items() if len(g) > 1}
    print(f"   {'✗' if dcasual else '✓'} تعبير مكرر: {len(dcasual)}")
    for (lang, e), g in list(dcasual.items())[:6]:
        print(f"        - [{lang}] {e} ×{len(g)} ids={[x['id'] for x in g]}")
    if dcasual:
        warnings.append(f"تعبيرات مكررة: {len(dcasual)}")

    # ---------- 7b) التمارين: تكرار وتصنيف ----------
    print("\n[7b] التمارين")
    TRAIN_F = ["id", "type", "question", "correctAnswer", "options",
               "explanation", "category", "languageCode"]
    training = rows_of(src, "TrainingItemEntity", TRAIN_F, {"languageCode": "ID"})
    print(f"   إجمالي التمارين: {len(training)}")

    # تكرار تام: نفس السؤال ونفس الإجابة داخل نفس التصنيف = حشو بلا فائدة.
    # (التكرار بين تصنيف الموضوع و«امتحان» مقصود: المراجعة تعيد السؤال.)
    same_cat = {}
    for t in training:
        k = (t.get("languageCode"), str(t.get("question", "")).strip(),
             str(t.get("correctAnswer", "")).strip(),
             str(t.get("category", "")).strip())
        same_cat.setdefault(k, []).append(t)
    pure_dup = {k: v for k, v in same_cat.items() if len(v) > 1}
    print(f"   {'✗' if pure_dup else '✓'} تمرين مكرر داخل نفس التصنيف: {len(pure_dup)}")
    for k, v in list(pure_dup.items())[:6]:
        print(f"        - [{k[0]}] ids={[x['id'] for x in v]} «{k[1][:44]}»")
    if pure_dup:
        blockers.append(f"تمارين مكررة داخل نفس التصنيف: {len(pure_dup)}")

    # سؤال بلا خيارات في نوع يتطلب خيارات
    needs_opts = [t for t in training
                  if str(t.get("type", "")) in ("MULTIPLE_CHOICE", "SITUATION")
                  and not str(t.get("options", "")).strip()]
    rep("تمرين اختيار بلا خيارات", needs_opts, blocker=True)

    # الإجابة الصحيحة يجب أن تكون ضمن الخيارات المعروضة
    answer_missing = []
    for t in training:
        opts = str(t.get("options", "")).strip()
        ans = str(t.get("correctAnswer", "")).strip()
        if not opts or not ans:
            continue
        choices = [o.strip() for o in opts.split(",")]
        if ans not in choices:
            answer_missing.append(t)
    print(f"   {'✗' if answer_missing else '✓'} الإجابة الصحيحة ليست ضمن الخيارات: "
          f"{len(answer_missing)}")
    for t in answer_missing[:6]:
        print(f"        - [{t.get('languageCode')}#{t.get('id')}] "
              f"«{str(t.get('question'))[:40]}» ⇒ {t.get('correctAnswer')}")
    if answer_missing:
        blockers.append(f"إجابة صحيحة خارج الخيارات: {len(answer_missing)}")

    # ---------- 8) القواعد ----------
    print("\n[8] القواعد")
    for lang in ("ID", "TR"):
        gl = [g for g in grammar if g.get("languageCode") == lang]
        print(f"   [{lang}] {len(gl)} قاعدة")
        for g in gl:
            ex = str(g.get("examples", ""))
            has_ex = bool(ex.strip())
            mark = "✓" if has_ex else "✗"
            print(f"        {mark} L{g.get('level')} {g.get('titleAr')}")
            if not has_ex:
                blockers.append(f"قاعدة بلا أمثلة: {g.get('titleAr')}")
    noex_g = [g for g in grammar if not str(g.get("examples", "")).strip()]
    if noex_g:
        print(f"   ✗ قواعد بلا أمثلة: {len(noex_g)}")

    # ---------- الخلاصة ----------
    print("\n" + "=" * 70)
    if blockers:
        print("عيوب حاجزة:")
        for b in dict.fromkeys(blockers):
            print("   ✗ " + b)
    if warnings:
        print("ملاحظات:")
        for w in dict.fromkeys(warnings):
            print("   ⚠ " + w)
    if not blockers and not warnings:
        print("✓ لا عيوب")
    print("=" * 70)
    return 1 if (strict and blockers) else 0


if __name__ == "__main__":
    sys.exit(main())
