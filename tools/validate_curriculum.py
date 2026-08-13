#!/usr/bin/env python3
"""
تدقيق المنهج: هل عنوان الدرس يطابق ما يُدرّسه فعلاً؟

هذا الفحص وُلد من عيب حقيقي: الدرس رقم 3 عنوانه «الأرقام 1-10»
بينما تفاصيله تشرح ترتيب الجملة (فاعل + فعل + مفعول)، والدرس 4
عنوانه «الأيام» بينما تفاصيله تشرح الأفعال. المتعلم يفتح درساً
باسم معيّن فيجد موضوعاً آخر تماماً — وهذا يهدم الثقة بالمنهج كله.

الطريقة: لكل موضوع، مجموعة من الشواهد النصية التي لا تظهر إلا فيه
(أرقام إندونيسية، أدوات النفي، أدوات السؤال...). نستخرج موضوع
العنوان، ثم نفحص هل شواهد ذلك الموضوع موجودة فعلاً في التفاصيل.

الاستخدام: python3 tools/validate_curriculum.py
"""

import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from audit_content import load, rows_of  # noqa: E402

LESSON_F = ["id", "level", "titleAr", "titleId", "description", "content",
            "completed", "languageCode"]
DETAIL_F = ["id", "unitId", "explanation", "wordByWord", "sentenceStructure",
            "dailyUsage", "commonMistakes", "formalVsCasual"]

# موضوع → (كلمات العنوان الدالة، شواهد يجب أن تظهر في التفاصيل)
# الشواهد لغوية ملموسة لا عامة، حتى لا يمر درس خاطئ بالصدفة.
TOPICS = {
    "أرقام": (
        ["أرقام", "الأرقام", "عدد", "أعداد", "sayılar"],
        ["satu", "dua", "tiga", "sepuluh", "seratus",
         "bir", "iki", "üç", "dört", "beş"],
    ),
    "أيام": (
        ["الأيام", "أيام الأسبوع", "الأسبوع"],
        ["senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "minggu",
         "pazartesi", "salı", "çarşamba", "perşembe", "cuma"],
    ),
    "ضمائر": (
        ["الضمائر", "ضمائر"],
        ["saya", "aku", "kamu", "anda", "dia", "kami", "kita",
         "ben", "sen", "biz", "siz", "onlar"],
    ),
    "نفي": (
        ["النفي", "نفي"],
        # التركية تنفي بلاحقة داخل الفعل (gelmiyorum) لا بكلمة مستقلة،
        # فالشواهد تشمل الأنماط الملتصقة لا كلمات النفي فقط.
        ["tidak", "bukan", "belum", "jangan", "değil", "yok",
         "نفي", "miyor", "mıyor", "muyor", "müyor", "-me", "-ma"],
    ),
    "سؤال": (
        ["السؤال", "الاستفهام", "أدوات السؤال", "سؤال"],
        ["apa", "siapa", "mana", "berapa", "kenapa", "bagaimana",
         "mı", "mi", "mu", "mü", "kim", "ne"],
    ),
    "أفعال": (
        ["الأفعال", "أفعال", "الفعل"],
        ["makan", "minum", "pergi", "kerja", "mau", "suka",
         "mak", "mek", "fiil", "الفعل"],
    ),
    "صفات": (
        ["الصفات", "صفات"],
        ["besar", "kecil", "bagus", "mahal", "murah", "tinggi",
         "iyi", "büyük", "ucuz", "pahalı"],
    ),
    "ملكية": (
        ["الملكية", "ملكية"],
        ["rumah saya", "buku kamu", "bukuku", "-ku", "-mu", "-nya",
         "benim", "senin", "evim", "ملكية"],
    ),
    "وقت": (
        ["الوقت", "التاريخ", "الساعة"],
        ["jam", "hari ini", "besok", "kemarin", "pagi", "siang",
         "sore", "malam", "saat", "gün"],
    ),
    "ألوان": (
        ["الألوان", "ألوان"],
        ["merah", "biru", "hijau", "kuning", "hitam", "putih",
         "mavi", "kırmızı", "yeşil", "sarı", "siyah", "beyaz"],
    ),
    "تحيات": (
        ["التحيات", "تحية", "التحية"],
        ["halo", "selamat pagi", "selamat siang", "apa kabar",
         "merhaba", "günaydın", "nasılsın"],
    ),
    "أسرة": (
        ["الأسرة", "العائلة", "أسرة"],
        ["ayah", "ibu", "kakak", "adik", "bapak",
         "anne", "baba", "kardeş"],
    ),
    "حروف الجر": (
        ["حروف الجر", "الجر"],
        ["di", "ke", "dari", "-de", "-da", "-e", "-a", "حالات"],
    ),
    "أبجدية": (
        ["الأبجدية", "الحروف", "النطق"],
        ["حرف", "حروف", "alfabe", "صوتية", "ساكنة"],
    ),
}


def topic_of(title):
    """يستخرج موضوع الدرس من عنوانه، أو None إن لم يكن موضوعاً مفهرساً."""
    t = title.strip()
    best = None
    for topic, (keys, _) in TOPICS.items():
        for k in keys:
            if k in t:
                # نفضّل أطول مطابقة (الأرقام المتقدمة قبل الأرقام)
                if best is None or len(k) > best[1]:
                    best = (topic, len(k))
    return best[0] if best else None


def main():
    src = load()
    lessons = rows_of(src, "LessonEntity", LESSON_F,
                      {"completed": 0, "languageCode": "ID"})
    details = {d["id"]: d for d in rows_of(src, "LessonDetailEntity",
                                           DETAIL_F, {})}

    print("=" * 70)
    print("تدقيق المنهج — هل العنوان يطابق المحتوى؟")
    print("=" * 70)

    failures = []
    unmatched = []
    nodetail = []
    placeholder = []

    for r in sorted(lessons, key=lambda x: (x["languageCode"], x["level"], x["id"])):
        lid = r["id"]
        title = str(r.get("titleAr", ""))
        d = details.get(lid)
        if d is None:
            nodetail.append(lid)
            continue
        if re.fullmatch(r"content\d+", str(r.get("content", "")).strip()):
            placeholder.append(lid)

        topic = topic_of(title)
        if topic is None:
            unmatched.append((lid, title))
            continue
        _, evidence = TOPICS[topic]
        blob = " ".join(str(d.get(k, "")) for k in
                        ("explanation", "wordByWord", "sentenceStructure",
                         "dailyUsage")).lower()
        hit = [e for e in evidence if e.lower() in blob]
        if not hit:
            failures.append((r["languageCode"], lid, title, topic))

    print(f"\n[1] الدروس: {len(lessons)} | لها تفاصيل: {len(lessons) - len(nodetail)}")
    if nodetail:
        print(f"   ✗ دروس بلا تفاصيل: {nodetail}")

    print(f"\n[2] مطابقة العنوان للمحتوى")
    print(f"   دروس ذات موضوع مفهرس: {len(lessons) - len(unmatched) - len(nodetail)}")
    if failures:
        print(f"   ✗ عنوان لا يطابق محتواه: {len(failures)}")
        for lang, lid, title, topic in failures:
            print(f"        [{lang}] #{lid:<4} «{title}» — "
                  f"لا يوجد أي شاهد على موضوع «{topic}» في التفاصيل")
    else:
        print("   ✓ كل درس ذي موضوع مفهرس يطابق محتواه")

    print(f"\n[3] حقل content")
    if placeholder:
        print(f"   ⚠ دروس قيمتها نائبة (contentN): {len(placeholder)}")
        print("        الحقل غير معروض في أي شاشة — بيانات ميتة.")
    else:
        print("   ✓ لا قيم نائبة")

    print("\n" + "=" * 70)
    if failures:
        print(f"✗ فشل: {len(failures)} درساً عنوانه يخالف محتواه")
        print("=" * 70)
        return 1
    print("✓ المنهج متسق: لا درس يَعِد بموضوع ويعرض غيره")
    print("=" * 70)
    return 0


if __name__ == "__main__":
    sys.exit(main())
